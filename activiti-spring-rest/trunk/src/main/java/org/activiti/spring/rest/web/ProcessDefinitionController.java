package org.activiti.spring.rest.web;
import org.activiti.spring.rest.model.ProcessDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.roo.addon.web.mvc.controller.json.RooWebJson;
import org.springframework.roo.addon.web.mvc.controller.scaffold.RooWebScaffold;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/process-definitions")
@Controller
@RooWebScaffold(path = "process-definitions", formBackingObject = ProcessDefinition.class)
@RooWebJson(jsonObject = ProcessDefinition.class)
public class ProcessDefinitionController {

	protected static final Logger LOGGER = LoggerFactory
			.getLogger(UserRecordController.class);

}
