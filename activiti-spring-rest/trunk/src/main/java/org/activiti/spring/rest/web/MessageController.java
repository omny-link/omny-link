package org.activiti.spring.rest.web;


import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.history.HistoricVariableInstance;
import org.activiti.engine.impl.identity.Authentication;
import org.activiti.engine.runtime.ProcessInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Handle REST requests sending BPMN message events to start or modify process
 * instances.
 * 
 * @author Tim Stephenson
 * 
 */
// @RooWebJson(jsonObject = Deployment.class)
@Controller
@RequestMapping("/msg")
// @RooWebScaffold(path = "msg", formBackingObject = Deployment.class)
public class MessageController {

	protected static final Logger LOGGER = LoggerFactory
			.getLogger(MessageController.class);

	protected static ProcessEngine processEngine;

	@Autowired
	public void setProcessEngine(ProcessEngine pe) {
		MessageController.processEngine = pe;
	}

	@RequestMapping(method = RequestMethod.GET, value = "/{msgId}", headers = "Accept=application/json")
	@ResponseBody
	public final ResponseEntity<String> doInOutMep(
			UriComponentsBuilder uriBuilder,
			@PathVariable("msgId") String msgId,
			@RequestParam("query") String jsonBody) {
		// TODO need to look into why but the PathVariable is truncated at the
		// last . so 'kp.foo' arrives as 'kp'. To work around this clients
		// should send kp.foo.json or similar/
		LOGGER.info("getMessage: " + msgId + ", json:" + jsonBody);
		String msg = "";
		Map<String, Object> vars = new HashMap<String, Object>();
		vars.put("query", msgId);
		vars.put(msgId, jsonBody);
		ResponseEntity<String> response = handleMep(uriBuilder, msgId,
				jsonBody, vars);
		String locationHeader = response.getHeaders().get("Location").get(0);
		LOGGER.debug("location: " + locationHeader);
		String id = locationHeader
				.substring(locationHeader.lastIndexOf('/') + 1);
		LOGGER.debug("id: " + id);
		try {
			msg = (String) processEngine.getRuntimeService().getVariable(id,
					msgId);
		} catch (ActivitiObjectNotFoundException e) {
			// assume process is closed
			HistoricVariableInstance instance = processEngine
					.getHistoryService().createHistoricVariableInstanceQuery()
					.processInstanceId(id).variableName(msgId).singleResult();
			msg = (String) instance.getValue();
		}
		LOGGER.debug("msg: " + msg);
		return new ResponseEntity(msg, response.getHeaders(), HttpStatus.OK);
	}

	@RequestMapping(method = RequestMethod.POST, value = "/{msgId}", headers = "Accept=application/json")
	@ResponseBody
	public ResponseEntity<String> doInOnlyMep(UriComponentsBuilder uriBuilder,
			@PathVariable("msgId") String msgId, @RequestParam String jsonBody) {
		long start = System.currentTimeMillis();
		LOGGER.info("doInOnlyMep: " + msgId + ", json:" + jsonBody);

		Map<String, Object> vars = new HashMap<String, Object>();
		vars.put("message", msgId);
		vars.put(msgId, jsonBody);

		ResponseEntity<String> response = handleMep(uriBuilder, msgId,
				jsonBody, vars);

		LOGGER.info(String.format("doInOnlyMep took: %1$s ms",
				(System.currentTimeMillis() - start)));
		return response;
	}

	protected ResponseEntity<String> handleMep(UriComponentsBuilder uriBuilder,
			String msgId, String jsonBody, Map<String, Object> vars) {
		if (SecurityContextHolder.getContext().getAuthentication()
				.getPrincipal() == null) {
			return new ResponseEntity(HttpStatus.UNAUTHORIZED);
		}
		String username = SecurityContextHolder.getContext()
				.getAuthentication().getName();
		LOGGER.info(" ... from " + username);

		HttpHeaders headers = new HttpHeaders();
		headers.add("Content-Type", "application/json");

		try {
			String bizKey = msgId + " - " + new Date().getTime();
			Authentication.setAuthenticatedUserId(username);
			vars.put("initiator", username);

			ProcessInstance instance = processEngine.getRuntimeService()
					.startProcessInstanceByMessage(msgId, bizKey, vars);

			List<ProcessInstance> list = processEngine.getRuntimeService()
					.createProcessInstanceQuery()
					.processInstanceId(instance.getId()).list();
			RequestMapping a = ProcessInstanceController.class
					.getAnnotation(RequestMapping.class);
			if (list.size() == 0) {
				// already ended
				// a =
				// HistoryController.class.getAnnotation(RequestMapping.class);
			}
			headers.add("Location",
					uriBuilder.path(a.value()[0] + "/" + instance.getId())
					.build().toUriString());
			return new ResponseEntity<String>(headers, HttpStatus.CREATED);
		} catch (ActivitiException e) {
			LOGGER.error(e.getClass().getName() + ": " + e.getMessage());
			if (LOGGER.isDebugEnabled()) {
				e.printStackTrace(System.err);
			}
			return new ResponseEntity<String>(headers,
					HttpStatus.SERVICE_UNAVAILABLE);
		} catch (Exception e) {
			LOGGER.error(e.getClass().getName() + ": " + e.getMessage());
			return new ResponseEntity<String>(headers, HttpStatus.BAD_REQUEST);
		} finally {
			Authentication.setAuthenticatedUserId(null);
		}
	}

}