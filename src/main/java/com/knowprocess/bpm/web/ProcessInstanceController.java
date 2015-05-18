package com.knowprocess.bpm.web;

import java.util.List;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletResponse;

import org.activiti.engine.ProcessEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.knowprocess.bpm.model.ProcessInstance;

@RequestMapping("/{tenantId}/process-instances")
@Controller
public class ProcessInstanceController {

    protected static final Logger LOGGER = LoggerFactory
            .getLogger(ProcessInstanceController.class);

    @Autowired
    public ProcessEngine processEngine;

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
    public @ResponseBody ProcessInstance getAuditTrail(
            @PathVariable("instanceId") String instanceId) {
        LOGGER.info("getAuditTrail");
        ProcessInstance result = ProcessInstance
                .findProcessInstance(instanceId);

        result.addToAuditTrail(processEngine.getHistoryService()
                .createHistoricActivityInstanceQuery()
                .processInstanceId(instanceId)
                .orderByHistoricActivityInstanceEndTime().desc().list());

        // result.setAuditTrail(processEngine.getHistoryService()
        // .createHistoricDetailQuery().processInstanceId(instanceId)
        // .orderByTime().desc().list());

        return result;
    }

    @RequestMapping(value = "/", method = RequestMethod.POST, headers = "Accept=application/json")
    @ResponseStatus(value = HttpStatus.CREATED)
    public @ResponseBody ProcessInstance startNewInstance(
            HttpServletResponse resp,
            @PathVariable("tenantId") String tenantId,
            @RequestBody ProcessInstance instanceToStart) {
        LOGGER.info(String.format("Start process %1$s for tenant %2$s",
                instanceToStart.getProcessDefinitionId(), tenantId));

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("  vars: ");
            for (Entry<String, Object> entry : instanceToStart
                    .getProcessVariables().entrySet()) {
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

}
