package org.activiti.spring.rest.web;

import java.util.List;

import org.activiti.spring.rest.model.ProcessInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.roo.addon.web.mvc.controller.json.RooWebJson;
import org.springframework.roo.addon.web.mvc.controller.scaffold.RooWebScaffold;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@RequestMapping("/process-instances")
@Controller
@RooWebScaffold(path = "process-instances", formBackingObject = ProcessInstance.class)
@RooWebJson(jsonObject = ProcessInstance.class)
public class ProcessInstanceController {

    protected static final Logger LOGGER = LoggerFactory
            .getLogger(ProcessInstanceController.class);
    
    @RequestMapping(headers = "Accept=application/json")
    @ResponseBody
    public ResponseEntity<String> listJson() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json; charset=utf-8");
        try {
            List<ProcessInstance> result = ProcessInstance.findAllProcessInstances();

            return new ResponseEntity<String>(ProcessInstance.toJsonArray(result), headers, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<String>("{\"ERROR\":"+e.getMessage()+"\"}", headers, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
