package org.activiti.spring.rest.web;
import org.activiti.spring.rest.model.Execution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.roo.addon.web.mvc.controller.json.RooWebJson;
import org.springframework.roo.addon.web.mvc.controller.scaffold.RooWebScaffold;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@RooWebJson(jsonObject = Execution.class)
@Controller
@RequestMapping("/executions")
@RooWebScaffold(path = "executions", formBackingObject = Execution.class)
public class ExecutionController {

    protected static final Logger LOGGER = LoggerFactory.getLogger(ExecutionController.class);
}
