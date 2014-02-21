package org.activiti.spring.rest.web;
import org.activiti.spring.rest.model.ProcessDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.roo.addon.web.mvc.controller.json.RooWebJson;
import org.springframework.roo.addon.web.mvc.controller.scaffold.RooWebScaffold;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@RequestMapping("/process-definitions")
@Controller
@RooWebScaffold(path = "process-definitions", formBackingObject = ProcessDefinition.class)
@RooWebJson(jsonObject = ProcessDefinition.class)
public class ProcessDefinitionController {

	protected static final Logger LOGGER = LoggerFactory
			.getLogger(UserRecordController.class);

	@RequestMapping(value = "/{id}", method = RequestMethod.GET, headers = "Accept=application/json")
	@ResponseBody
	public ResponseEntity<String> showJson(@PathVariable("id") String id) {
		ProcessDefinition processDefinition = ProcessDefinition
				.findProcessDefinition(id);
		HttpHeaders headers = new HttpHeaders();
		headers.add("Content-Type", "application/json; charset=utf-8");
		if (processDefinition == null) {
			return new ResponseEntity<String>(headers, HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<String>(processDefinition.toJson(), headers,
				HttpStatus.OK);
	}
}
