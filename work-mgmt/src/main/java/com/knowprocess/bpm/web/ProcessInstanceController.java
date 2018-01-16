/*******************************************************************************
 *Copyright 2011-2018 Tim Stephenson and contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package com.knowprocess.bpm.web;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricProcessInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
import com.knowprocess.bpm.api.ProcessDefinitionSuspendedException;
import com.knowprocess.bpm.impl.DateUtils;
import com.knowprocess.bpm.impl.UriHelper;
import com.knowprocess.bpm.model.ProcessInstance;
import com.knowprocess.services.pdf.Html2PdfService;

@RequestMapping("/{tenantId}/process-instances")
@Controller
public class ProcessInstanceController {

    protected static final Logger LOGGER = LoggerFactory
            .getLogger(ProcessInstanceController.class);

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    public ProcessEngine processEngine;

    @Autowired
    protected Html2PdfService pdfHelper;

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

    @RequestMapping(value = "/{instanceId}/variables/{varName}", method = RequestMethod.GET, produces = "application/pdf")
    public void getInstanceVarAsPdf(
            HttpServletResponse response,
            @PathVariable("tenantId") String tenantId,
            @PathVariable("instanceId") String instanceId,
            @PathVariable("varName") String varName) throws IOException {
        LOGGER.info("getInstanceVar");
        String var = getInstanceVar(instanceId, varName);
        response.setContentType("application/pdf");

        pdfHelper.execute(var, response.getOutputStream());

        LOGGER.debug(String.format(
                "PDF Created for var %1$s of process instance %2$s", varName,
                instanceId));
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

        ProcessInstance pi;
        try {
            if (instanceToStart.getProcessDefinitionId()==null) {
                pi = new ProcessInstance(processEngine
                        .getRuntimeService().startProcessInstanceByKeyAndTenantId(
                                instanceToStart.getProcessDefinitionKey(),
                                instanceToStart.getBusinessKey(),
                                instanceToStart.getProcessVariables(), tenantId));
            } else {
                pi = new ProcessInstance(processEngine
                        .getRuntimeService().startProcessInstanceById(
                                instanceToStart.getProcessDefinitionId(),
                                instanceToStart.getBusinessKey(),
                                instanceToStart.getProcessVariables()));
            }
        } catch (ActivitiException e) {
            if (e.getMessage().endsWith("is suspended")) {
                throw new ProcessDefinitionSuspendedException(e.getMessage());
            }
            throw e;
        }
        resp.setHeader("Location",
                "/process-instances/" + pi.getProcessInstanceId());
        return pi;
    }

    @RequestMapping(value = "/archive", method = RequestMethod.GET, headers = "Accept=application/json")
    @Secured("ROLE_ADMIN")
    public @ResponseBody List<ProcessInstance> archiveInstances(
            @PathVariable("tenantId") String tenantId,
            @RequestParam(value = "before", required = false) String before) {
        Date beforeDate = before == null
                ? DateUtils.oneMonthAgo() : DateUtils.parseDate(before);
        LOGGER.debug(String.format(
                "Archiving instances of %1$s older than %2$s", tenantId,
                beforeDate.toString()));
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
