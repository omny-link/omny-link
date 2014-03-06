package org.activiti.spring.rest.web;

import javax.servlet.http.HttpServletRequest;

import org.activiti.engine.IdentityService;
import org.activiti.engine.ProcessEngine;
import org.activiti.spring.rest.model.UserInfo;
import org.activiti.spring.rest.model.UserRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.roo.addon.web.mvc.controller.json.RooWebJson;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@RooWebJson(jsonObject = UserRecord.class)
@Controller
@RequestMapping("/users")
public class UserRecordController {

	protected static final String PATH = "/users";

	protected static final Logger LOGGER = LoggerFactory
			.getLogger(UserRecordController.class);

	@Autowired(required = true)
	ProcessEngine processEngine;

	@RequestMapping(value = "/{id}", method = RequestMethod.GET, headers = "Accept=application/json")
	@ResponseBody
	public ResponseEntity<String> showJson(@PathVariable("id") String id,
			HttpServletRequest request) {
		LOGGER.info(String
				.format("%1$s %2$s/%3$s", RequestMethod.GET, PATH, id));
		// Since id will end .com (or similar TLE) need to get id from request 
		// directly as Spring will truncate the extension. 
		id = request.getServletPath().substring(
				request.getServletPath().lastIndexOf('/') + 1);
		System.out.println("Find user with id: " + id);
		UserRecord userRecord = UserRecord.findUserRecord(id);
		HttpHeaders headers = new HttpHeaders();
		headers.add("Content-Type", "application/json; charset=utf-8");
		if (userRecord == null) {
			return new ResponseEntity<String>(headers, HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<String>(userRecord.toJson(), headers,
				HttpStatus.OK);
	}

	@RequestMapping(value = "/{id}", method = RequestMethod.PUT, headers = "Accept=application/json")
	public ResponseEntity<String> updateFromJson(@RequestBody String json,
			@PathVariable("id") String id) {
		LOGGER.info(String.format("Updating profile of %1$s", id));
		HttpHeaders headers = new HttpHeaders();
		headers.add("Content-Type", "application/json");
		UserRecord userRecord = UserRecord.fromJsonToUserRecord(json);

		UserRecord user = UserRecord.findUserRecord(id);
		if (userRecord.getEmail() != null
				&& userRecord.getEmail().trim().length() > 0) {
			user.setEmail(userRecord.getEmail());
		}
		if (userRecord.getFirstName() != null
				&& userRecord.getFirstName().trim().length() > 0) {
			user.setFirstName(userRecord.getFirstName());
		}
		if (userRecord.getLastName() != null
				&& userRecord.getLastName().trim().length() > 0) {
			user.setLastName(userRecord.getLastName());
		}
		IdentityService idSvc = processEngine.getIdentityService();
		LOGGER.debug(String.format("Updating user record for %1$s...", id));
		idSvc.saveUser(user);
		LOGGER.debug("... done");
		for (UserInfo info : userRecord.getInfo()) {
			String userInfo = idSvc.getUserInfo(id, info.getKey());
			System.out.println("Found user info: " + userInfo + " for key: "
					+ info.getKey());
			if (userInfo == null || !userInfo.equals(info.getValue())) {
				LOGGER.debug(String.format(
						"Updating user info record %2$s for %1$s...", id,
						info.getKey()));
				idSvc.setUserInfo(id, info.getKey(), info.getValue());
				LOGGER.debug("... done");
			}
		}

		return new ResponseEntity<String>(headers, HttpStatus.OK);
	}

}
