package org.activiti.spring.rest.web;
import org.activiti.spring.rest.model.Execution;
import org.springframework.roo.addon.web.mvc.controller.json.RooWebJson;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.roo.addon.web.mvc.controller.scaffold.RooWebScaffold;

@RooWebJson(jsonObject = Execution.class)
@Controller
@RequestMapping("/executions")
@RooWebScaffold(path = "executions", formBackingObject = Execution.class)
public class ExecutionController {
}
