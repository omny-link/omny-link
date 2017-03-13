package com.knowprocess.bpm.web;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricProcessInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.tool.xml.Pipeline;
import com.itextpdf.tool.xml.XMLWorker;
import com.itextpdf.tool.xml.XMLWorkerHelper;
import com.itextpdf.tool.xml.exceptions.CssResolverException;
import com.itextpdf.tool.xml.html.Tags;
import com.itextpdf.tool.xml.parser.XMLParser;
import com.itextpdf.tool.xml.pipeline.css.CSSResolver;
import com.itextpdf.tool.xml.pipeline.css.CssResolverPipeline;
import com.itextpdf.tool.xml.pipeline.end.PdfWriterPipeline;
import com.itextpdf.tool.xml.pipeline.html.HtmlPipeline;
import com.itextpdf.tool.xml.pipeline.html.HtmlPipelineContext;
import com.itextpdf.tool.xml.pipeline.html.LinkProvider;
import com.knowprocess.bpm.impl.UriHelper;
import com.knowprocess.bpm.model.ProcessInstance;

@RequestMapping("/{tenantId}/process-instances")
@Controller
public class ProcessInstanceController {

    protected static final Logger LOGGER = LoggerFactory
            .getLogger(ProcessInstanceController.class);

    private DateFormat isoFormatter = new SimpleDateFormat("yyyy-MM-dd");

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    public ProcessEngine processEngine;

    @Value("${spring.data.rest.baseUri:https://api.omny.link}")
    private String baseUrl;

    private String bootstrapCss;

    private String omnyCss;

    @RequestMapping(value = "/", method = RequestMethod.GET, headers = "Accept=application/json")
    public @ResponseBody List<ProcessInstance> listJson() {
        LOGGER.info("listJson");
        try {
            List<ProcessInstance> result = ProcessInstance
                    .findAllProcessInstances();

            return result;
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw e;
        }
    }

    @RequestMapping(value = "/{instanceId}", method = RequestMethod.GET, headers = "Accept=application/json")
    public @ResponseBody ProcessInstance getInstanceIncAuditTrail(
            @PathVariable("instanceId") String instanceId) {
        LOGGER.info("getAuditTrail");
        ProcessInstance result = ProcessInstance
                .findProcessInstance(instanceId);

        result.addToAuditTrail(processEngine.getHistoryService()
                .createHistoricActivityInstanceQuery()
                .processInstanceId(instanceId)
                .orderByHistoricActivityInstanceEndTime().desc().list());

        addCalledProcessAuditTrail(result, instanceId);

        return result;
    }

    @RequestMapping(value = "/{instanceId}/variables/{varName}", method = RequestMethod.GET, headers = "Accept=text/*")
    public @ResponseBody String getInstanceVar(
            @PathVariable("instanceId") String instanceId, 
            @PathVariable("varName") String varName) {
        LOGGER.info(String.format("getInstanceVar(%1$s, %2$s)", instanceId, varName));
        
        try {
            return processEngine.getRuntimeService()
                    .getVariable(instanceId, varName).toString();
        } catch (NullPointerException e) {
            return "Still working...";
        } catch (ActivitiObjectNotFoundException e) {
            try {
                return processEngine.getHistoryService()
                        .createHistoricVariableInstanceQuery()
                        .processInstanceId(instanceId)
                        .variableName(varName)
                        .singleResult().getValue().toString();
            } catch (NullPointerException e2) {
                return "Still working...";
            }
        }
    }
    
    @RequestMapping(value = "/{instanceId}/variables/{varName}", method = RequestMethod.GET, headers = "Accept=application/pdf", produces = "application/pdf")
    public void getInstanceVarAsPdf(
            HttpServletResponse response,
            @PathVariable("tenantId") String tenantId,
            @PathVariable("instanceId") String instanceId, 
            @PathVariable("varName") String varName) {
        LOGGER.info("getInstanceVar");
        String var = getInstanceVar(instanceId, varName);

        StringBuilder sb = new StringBuilder()
                .append("<html><head>")
                .append("</head><body>")
                .append(var.replaceAll("<br>", "<br/>"))
                .append("</body></html>");

        try {
            Document document = new Document();
            response.setContentType("application/pdf");
            PdfWriter writer = PdfWriter.getInstance(document, response.getOutputStream());
            writer.setInitialLeading(12.5f);
            document.open();

            HtmlPipelineContext htmlContext = new HtmlPipelineContext(null);
            htmlContext.setTagFactory(Tags.getHtmlTagProcessorFactory());
//            htmlContext.setImageProvider(new AbstractImageProvider() {
//                public String getImageRootPath() {
//                    return "images/";
//                }
//            });

            htmlContext.setLinkProvider(new LinkProvider() {
                public String getLinkRoot() {
                    return baseUrl;
                }
            });

            CSSResolver cssResolver = 
                    XMLWorkerHelper.getInstance().getDefaultCssResolver(false);
            try {
                cssResolver.addCss(getBootstrapCss("/META-INF/resources/webjars/bootstrap/3.3.5/css/bootstrap.min.css"), true);
                cssResolver.addCss(getOmnyCss("/static/css/omny-1.0.0.css"), true);
                cssResolver.addCss("ol { list-style: decimal !important; } ul { list-style: disc !important; }", true);
            } catch (CssResolverException e) {
                LOGGER.warn("Cannot add CSS to PDF pipeline", e);
            }
            Pipeline<?> pipeline = new CssResolverPipeline(cssResolver,
                    new HtmlPipeline(htmlContext,
                            new PdfWriterPipeline(document, writer)));
            XMLWorker worker = new XMLWorker(pipeline, true);

            XMLParser p = new XMLParser(worker);
            p.parse(new StringReader(sb.toString()));

            document.close();
        
            LOGGER.debug(String.format("PDF Created for var %1$s of process instance %2$s", varName, instanceId));
        } catch (NoClassDefFoundError e) {
            throw new IllegalStateException("PDF generation not currently enabled.");
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
            throw new IllegalStateException(e);
        } catch (DocumentException e) {
            LOGGER.error(e.getMessage(), e);
            throw new IllegalStateException("Probably a template problem.");
        }
    }

    private String getBootstrapCss(String resource) {
        if (bootstrapCss == null) {
            bootstrapCss = getClasspathResource(resource);
        }
        return bootstrapCss;
    }

    private String getOmnyCss(String resource) {
        if (omnyCss == null) {
            omnyCss = getClasspathResource(resource);
        }
        return omnyCss;
    }

    @SuppressWarnings("resource")
    private String getClasspathResource(String resource) {
        InputStream is = null;
        try {
            is = getClass().getResourceAsStream(resource);
            return new Scanner(is).useDelimiter("\\A").next();
        } catch (Exception e) {
            LOGGER.warn(String.format("Unable to read CSS from %1$s", resource));
        } finally {
            try {
                is.close();
            } catch (Exception e) {
                ;
            }
        }
        return "";
    }

    @RequestMapping(value = "/findByVar/{varName}/{varValue}", method = RequestMethod.GET, headers = "Accept=application/json")
    public @ResponseBody List<ProcessInstance> listInstancesForVar(
            @PathVariable("varName") String varName,
            @PathVariable("varValue") String varValue) {
        LOGGER.info(String.format("listInstancesForVar %1$s %2$s ", varName,
                varValue));

        varValue = UriHelper.expandUri(getClass(), varName, varValue).replace(
                "/process-instances", "");

        List<ProcessInstance> results = ProcessInstance.wrap(processEngine
                .getRuntimeService().createProcessInstanceQuery()
                .variableValueEquals(varName, varValue).list());

        for (ProcessInstance result : results) {
            result.addToAuditTrail(processEngine.getHistoryService()
                    .createHistoricActivityInstanceQuery()
                    .processInstanceId(varName)
                    .orderByHistoricActivityInstanceEndTime().desc().list());

            addCalledProcessAuditTrail(result, varName);
        }

        return results;
    }

    private void addCalledProcessAuditTrail(ProcessInstance result,
            String instanceId) {
        LOGGER.info("addCalledProcessAuditTrail: " + instanceId);
        List<org.activiti.engine.runtime.ProcessInstance> childProcessInstances = processEngine
                .getRuntimeService().createProcessInstanceQuery()
                .superProcessInstanceId(instanceId).list();

        for (org.activiti.engine.runtime.ProcessInstance childInstance : childProcessInstances) {
            List<HistoricActivityInstance> childProcEvents = processEngine
                    .getHistoryService().createHistoricActivityInstanceQuery()
                    .processInstanceId(childInstance.getId())
                    .orderByHistoricActivityInstanceEndTime().desc().list();
            result.addToAuditTrail(childProcEvents);
            for (HistoricActivityInstance historicActivityInstance : childProcEvents) {
                if (historicActivityInstance.getActivityType().equals(
                        "callActivity")) {
                    addCalledProcessAuditTrail(result,
                            historicActivityInstance
                            .getCalledProcessInstanceId());
                }
            }
        }
    }

    @RequestMapping(value = "/", method = RequestMethod.POST, headers = "Accept=application/json")
    @ResponseStatus(value = HttpStatus.CREATED)
    public @ResponseBody ProcessInstance startNewInstance(
            HttpServletRequest req,
            HttpServletResponse resp,
            @PathVariable("tenantId") String tenantId,
            @RequestBody ProcessInstance instanceToStart) {
        LOGGER.info(String.format("Start process %1$s for tenant %2$s",
                instanceToStart.getProcessDefinitionId(), tenantId));

        if (!instanceToStart.getProcessVariables().containsKey("initiator")
                && req.getUserPrincipal() != null) {
            instanceToStart.getProcessVariables().put("initiator",
                    req.getUserPrincipal().getName());
        }
        instanceToStart.getProcessVariables().put("tenantId", tenantId);
        LOGGER.debug("  vars: %1$d", instanceToStart.getProcessVariables()
                .size());
        for (Entry<String, Object> entry : instanceToStart
                .getProcessVariables().entrySet()) {
            if (entry.getValue() instanceof Map
                    || entry.getValue() instanceof List) {
                try {
                    instanceToStart.getProcessVariables().replace(
                            entry.getKey(),
                            objectMapper.writeValueAsString(entry.getValue()));
                } catch (JsonProcessingException e) {
                    LOGGER.error(String
                            .format("Unable to serialize process variable %1$s as JSON, attempting to continue",
                                    entry.getKey()));
                }
            }
            if (LOGGER.isDebugEnabled()
                    && instanceToStart.getProcessVariables() != null) {
                LOGGER.debug(entry.getKey() + " " + entry.getValue());
            }
        }

        ProcessInstance pi = new ProcessInstance(processEngine
                .getRuntimeService().startProcessInstanceByKeyAndTenantId(
                        instanceToStart.getProcessDefinitionId(),
                        instanceToStart.getBusinessKey(),
                        instanceToStart.getProcessVariables(), tenantId));
        resp.setHeader("Location",
                "/process-instances/" + pi.getProcessInstanceId());
        return pi;
    }

    @RequestMapping(value = "/archive", method = RequestMethod.GET, headers = "Accept=application/json")
    @Secured("ROLE_ADMIN")
    public @ResponseBody List<ProcessInstance> archiveInstances(
            @PathVariable("tenantId") String tenantId,
            @RequestParam(value = "before", required = false) String before) {
        Date beforeDate = null;
        if (before == null) {
            GregorianCalendar oneMonthAgo = new GregorianCalendar();
            oneMonthAgo.add(Calendar.MONTH, -1);
            LOGGER.debug(String.format(
                    "Archiving messages or %1$s older than %2$s", tenantId,
                    oneMonthAgo.getTime().toString()));
            beforeDate = oneMonthAgo.getTime();
        } else {
            try {
                beforeDate = isoFormatter.parse(before);
            } catch (ParseException e) {
                throw new IllegalArgumentException(
                        String.format(
                                "Parameter 'before' must be an ISO 8601 date, not '%1$s'",
                                before));
            }
        }

        List<HistoricProcessInstance> archivedInstances = processEngine
                .getHistoryService()
                .createHistoricProcessInstanceQuery()
                .processInstanceTenantId(tenantId).finishedBefore(beforeDate)
                .orderByProcessInstanceEndTime().asc().list();
        LOGGER.warn(String.format("Found %1$d instances to archive for %2$s",
                archivedInstances.size(), tenantId));
        int count = 0;
        for (HistoricProcessInstance hpi : archivedInstances) {
            try {
                processEngine.getHistoryService()
                        .deleteHistoricProcessInstance(hpi.getId());
            } catch (Exception e) {
                LOGGER.error(String.format(
                        "Unable to archive historic process with id %1$s",
                        hpi.getId()), e);
            }
            count++;
        }
        LOGGER.warn(String.format("  successfully archived %1$d instancess",
                count));

        return ProcessInstance.wrap(archivedInstances);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public @ResponseBody void deleteFromJson(@PathVariable("id") String id,
            @RequestParam(value = "reason", required = false) String reason) {
        LOGGER.info(String.format("deleting instance: %1$s", id));
        try {
            processEngine.getRuntimeService().deleteProcessInstance(id, reason);
        } catch (ActivitiObjectNotFoundException e) {
            // must be complete instance
            processEngine.getHistoryService().deleteHistoricProcessInstance(id);
        }
    }

}
