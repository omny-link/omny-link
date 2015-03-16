package com.knowprocess.bpm.web;

import java.io.IOException;
import java.util.List;

import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.identity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.knowprocess.bpm.model.ProcessDefinition;

@RequestMapping("/process-definitions")
@Controller
public class ProcessDefinitionController {

    protected static final Logger LOGGER = LoggerFactory
            .getLogger(ProcessDefinitionController.class);

    @RequestMapping(value = "/", method = RequestMethod.GET, headers = "Accept=application/json")
    public @ResponseBody List<ProcessDefinition> showAllJson() {
        LOGGER.info("showAllJson");

        try {
            List<ProcessDefinition> list = ProcessDefinition
                    .findAllProcessDefinitions();
            LOGGER.info("Definitions: " + list.size());
            return list;
        } catch (Exception e) {
            LOGGER.error(e.getClass().getName() + ":" + e.getMessage());
            e.printStackTrace(System.err);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET, headers = "Accept=application/json")
    public @ResponseBody ProcessDefinition showJson(
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

    @RequestMapping(value = "/{id}.bpmn", method = RequestMethod.GET, /*
                                                                       * headers
                                                                       * =
                                                                       * "Accept=application/xml"
                                                                       * ,
                                                                       */produces = "application/xml")
    public @ResponseBody String showBpmn(@PathVariable("id") String id) {
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
    public @ResponseBody byte[] showBpmnDiagram(@PathVariable("id") String id)
            throws IOException {
        LOGGER.info(String.format("%1$s BPMN with id %2$s", RequestMethod.GET,
                id));

        return ProcessDefinition.findProcessDefinitionDiagram(id);
    }

}
