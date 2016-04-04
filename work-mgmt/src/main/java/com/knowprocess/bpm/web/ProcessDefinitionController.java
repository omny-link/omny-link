package com.knowprocess.bpm.web;

import java.io.IOException;
import java.util.List;

import javax.xml.transform.TransformerConfigurationException;

import org.activiti.engine.ProcessEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.knowprocess.bpm.model.ModelIssue;
import com.knowprocess.bpm.model.ProcessDefinition;
import com.knowprocess.bpm.model.ProcessInstance;
import com.knowprocess.bpm.model.ProcessModel;
import com.knowprocess.bpm.repositories.ProcessModelRepository;
import com.knowprocess.xslt.TransformTask;

@RequestMapping("/{tenantId}/process-definitions")
@Controller
public class ProcessDefinitionController {

    protected static final Logger LOGGER = LoggerFactory
            .getLogger(ProcessDefinitionController.class);

    private static final String RENDERER_RESOURCES = "/static/xslt/bpmn2svg.xslt";

    @Autowired
    protected ProcessEngine processEngine;

    @Autowired
    private ProcessModelRepository processModelRepo;

    private TransformTask renderer;

    @RequestMapping(value = "", method = RequestMethod.GET, headers = "Accept=application/json")
    public @ResponseBody List<ProcessDefinition> showAllJson(
            @PathVariable("tenantId") String tenantId) {
        LOGGER.info(String.format("showAllJson for %1$s", tenantId));

        List<ProcessDefinition> list = ProcessDefinition
                .findAllProcessDefinitions(tenantId);
        LOGGER.info("Deployed definitions: " + list.size());

        List<ProcessModel> incompleteModels = processModelRepo
                .findAllForTenant(tenantId);
        LOGGER.info("Incomplete definitions: " + incompleteModels.size());
        for (ProcessModel processModel : incompleteModels) {
            list.add(new ProcessDefinition(processModel));
        }

        LOGGER.info("Total definitions: " + list.size());
        return list;
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET, headers = "Accept=application/json")
    public @ResponseBody ProcessDefinition showJson(
            @PathVariable("tenantId") String tenantId,
            @PathVariable("id") String id) {
        LOGGER.info(String.format("%1$s definition with %2$s",
                RequestMethod.GET, id));

        ProcessDefinition pd = null;
        try {
            pd = ProcessDefinition.findProcessDefinition(id);
        } catch (NullPointerException e) {
            // assume this is an incomplete model....
            pd = new ProcessDefinition(processModelRepo.findOne(id));
        }
        return pd;
    }

    @RequestMapping(value = "/{id}/activate", method = RequestMethod.GET, headers = "Accept=application/json")
    public @ResponseBody void activate(
            @PathVariable("tenantId") String tenantId,
            @PathVariable("id") String id) {
        LOGGER.info(String.format("activate %1$s definition", id));

        processEngine.getRepositoryService().activateProcessDefinitionByKey(id,
                tenantId);
    }

    @RequestMapping(value = "/{id}/suspend", method = RequestMethod.GET, headers = "Accept=application/json")
    public @ResponseBody void suspend(
            @PathVariable("tenantId") String tenantId,
            @PathVariable("id") String id) {
        LOGGER.info(String.format("suspend %1$s definition", id));

        processEngine.getRepositoryService().suspendProcessDefinitionByKey(id,
                tenantId);
    }

    @RequestMapping(value = "/{id}/instances", method = RequestMethod.GET, headers = "Accept=application/json")
    public @ResponseBody List<ProcessInstance> showInstancesJson(
            @PathVariable("tenantId") String tenantId,
            @PathVariable("id") String id) {
        LOGGER.info(String.format("%1$s definition with %2$s",
                RequestMethod.GET, id));

        return ProcessInstance.findAllProcessInstancesForDefinition(id);
    }

    @RequestMapping(value = "/{id}/issues", method = RequestMethod.GET, headers = "Accept=application/json")
    public @ResponseBody List<ModelIssue> showIssuesJson(
            @PathVariable("tenantId") String tenantId,
            @PathVariable("id") String id) {
        LOGGER.info(String.format("%1$s definition with %2$s",
                RequestMethod.GET, id));

        return processModelRepo.findOne(id).getIssues();
    }

    @RequestMapping(value = "/{id}.bpmn", method = RequestMethod.GET, /*
                                                                       * headers
                                                                       * =
                                                                       * "Accept=application/xml"
                                                                       * ,
                                                                       */produces = "application/xml")
    public @ResponseBody String showBpmn(
            @PathVariable("tenantId") String tenantId,
            @PathVariable("id") String id) {
        LOGGER.info(String.format("%1$s BPMN with id %2$s", RequestMethod.GET,
                id));

        return ProcessDefinition.findProcessDefinitionAsBpmn(id);
    }

    @RequestMapping(value = "/{id}.png", method = RequestMethod.GET, produces = "image/png")
    public @ResponseBody byte[] showBpmnDiagram(
            @PathVariable("tenantId") String tenantId,
            @PathVariable("id") String id) throws IOException {
        LOGGER.info(String.format("%1$s BPMN with id %2$s", RequestMethod.GET,
                id));
        return ProcessDefinition.findProcessDefinitionDiagram(id);
    }

    @RequestMapping(value = "/{id}.svg", method = RequestMethod.GET, produces = "image/svg+xml")
    public @ResponseBody byte[] showBpmnDiagramAsSvg(
            @PathVariable("tenantId") String tenantId,
            @PathVariable("id") String id) throws IOException {
        LOGGER.info(String.format("%1$s BPMN with id %2$s", RequestMethod.GET,
                id));

        String bpmn = null;
        try {
            bpmn = ProcessDefinition.findProcessDefinitionAsBpmn(id);
        } catch (Exception e) {
            bpmn = processModelRepo.findOne(id).getBpmnString();
        }
        // XPathFactory factory = XPathFactory.newInstance();
        // XPath xpath = factory.newXPath();
        // DocumentBuilderFactory documentBuilderFactory =
        // DocumentBuilderFactory
        // .newInstance();
        // InputStream is = null;
        // try {
        // DocumentBuilder documentBuilder = documentBuilderFactory
        // .newDocumentBuilder();
        // is = new ByteArrayInputStream(bpmn.getBytes());
        // Document document = documentBuilder.parse(is);
        //
        // XPathExpression lastParticipantExpr = xpath
        // .compile("//bpmn:participant[position()=last()]/@id");
        // String partId = (String) lastParticipantExpr.evaluate(document,
        // XPathConstants.STRING);
        // XPathExpression expr = xpath
        // .compile(String
        // .format("sum(//bpmndi:BPMNShape[@bpmnElement=bpmn:participant/@id]/dc:Bounds/@height)",
        // partId));
        // Object evaluate = expr.evaluate(document);
        // System.out.println("  eval:" + evaluate);
        // XPathExpression expr2 = xpath
        // .compile(String
        // .format("//bpmndi:BPMNShape[@bpmnElement=//bpmn:participant/@id]/dc:Bounds/@height",
        // partId));
        // NodeList evaluate2 = (NodeList) expr2
        // .evaluate(document, XPathConstants.NODESET);
        // for (int i = 0; i < evaluate2.getLength(); i++) {
        // Node item = evaluate2.item(i);
        // System.out.println("  item: " + item);
        // }
        // } catch (XPathExpressionException | ParserConfigurationException
        // | SAXException e) {
        // // TODO Auto-generated catch block
        // e.printStackTrace();
        // } finally {
        // is.close();
        // }

        return getProcessRenderer().transform(bpmn).getBytes();
    }

    private TransformTask getProcessRenderer() {
        if (renderer == null) {
            renderer = new TransformTask();
            try {
                renderer.setXsltResources(RENDERER_RESOURCES);
            } catch (TransformerConfigurationException e) {
                LOGGER.error(String.format(
                        "Unable to location deployment pre-processors: %1$s",
                        RENDERER_RESOURCES), e);
            }
        }
        return renderer;
    }

}
