package org.activiti.spring.rest.web;
import org.activiti.spring.rest.model.ProcessInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.roo.addon.web.mvc.controller.json.RooWebJson;
import org.springframework.roo.addon.web.mvc.controller.scaffold.RooWebScaffold;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/process-instances")
@Controller
@RooWebScaffold(path = "process-instances", formBackingObject = ProcessInstance.class)
@RooWebJson(jsonObject = ProcessInstance.class)
public class ProcessInstanceController {

	protected static final Logger LOGGER = LoggerFactory
			.getLogger(UserRecordController.class);
}
