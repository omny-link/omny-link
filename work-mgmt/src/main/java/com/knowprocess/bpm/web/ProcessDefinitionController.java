package com.knowprocess.bpm.web;

import java.io.IOException;
import java.util.List;

import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.identity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.knowprocess.bpm.model.ProcessDefinition;
import com.knowprocess.bpm.model.ProcessInstance;

@RequestMapping("/{tenantId}/process-definitions")
@Controller
public class ProcessDefinitionController {

    protected static final Logger LOGGER = LoggerFactory
            .getLogger(ProcessDefinitionController.class);

    @Autowired
    protected ProcessEngine processEngine;

    @RequestMapping(value = "", method = RequestMethod.GET, headers = "Accept=application/json")
    public @ResponseBody List<ProcessDefinition> showAllJson(
            @PathVariable("tenantId") String tenantId) {
        LOGGER.info(String.format("showAllJson for %1$s", tenantId));

        List<ProcessDefinition> list = ProcessDefinition
                .findAllProcessDefinitions(tenantId);
        LOGGER.info("Definitions: " + list.size());
        return list;
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET, headers = "Accept=application/json")
    public @ResponseBody ProcessDefinition showJson(
            @PathVariable("tenantId") String tenantId,
            @PathVariable("id") String id) {
        LOGGER.info(String.format("%1$s definition with %2$s",
                RequestMethod.GET, id));

        ProcessDefinition pd = ProcessDefinition.findProcessDefinition(id);
        // TODO unclear if the following check is redundant in all or only
        // some cases
        if (pd == null) {
            throw new ActivitiObjectNotFoundException(User.class);
        }
        return pd;
    }

    @RequestMapping(value = "/{id}/instances", method = RequestMethod.GET, headers = "Accept=application/json")
    public @ResponseBody List<ProcessInstance> showInstancesJson(
            @PathVariable("tenantId") String tenantId,
            @PathVariable("id") String id) {
        LOGGER.info(String.format("%1$s definition with %2$s",
                RequestMethod.GET, id));

        return ProcessInstance.findAllProcessInstancesForDefinition(id);
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

    @RequestMapping(value = "/{id}.png", method = RequestMethod.GET, /*
                                                                      * headers
                                                                      * =
                                                                      * "Accept=image/png"
                                                                      * ,
                                                                      */produces = "image/png")
    public @ResponseBody byte[] showBpmnDiagram(
            @PathVariable("tenantId") String tenantId,
            @PathVariable("id") String id)
            throws IOException {
        LOGGER.info(String.format("%1$s BPMN with id %2$s", RequestMethod.GET,
                id));

        return ProcessDefinition.findProcessDefinitionDiagram(id);
    }

}
