package org.activiti.spring.auth;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.IdentityService;
import org.activiti.engine.identity.User;
import org.activiti.spring.rest.web.UserRecordController;

public class ActivitiExternalAuthService extends ExternalAuthenticationService{ 

	private static final String PROC_FULFIL_ACCOUNT = "FulfilAccount";

	protected static final Logger LOGGER = LoggerFactory
			.getLogger(UserRecordController.class);

	@Autowired 
	protected ProcessEngine processEngine ; 
	
	public void updateLocalUser(ExternalUserDetails userDetails) {
		IdentityService idSvc = processEngine.getIdentityService(); 
		long count = idSvc.createUserQuery().userEmail(userDetails.getEmail()).count(); 
		if (count == 0) { 
			LOGGER.info(String.format("Authenticated unknown user '%1$s' starting process $2$s",userDetails.getEmail(), PROC_FULFIL_ACCOUNT));
			User user = idSvc.newUser(userDetails.getEmail()); 
			user.setId(userDetails.getEmail());
			user.setEmail(userDetails.getEmail());
			user.setFirstName(userDetails.getForename());
			user.setLastName(userDetails.getSurname());
			
//			idSvc.saveUser(user); 
			try { 
			
				Map<String, Object> vars = new HashMap<String, Object>();
				vars.put("user", user);
				processEngine.getRuntimeService().startProcessInstanceByKey(PROC_FULFIL_ACCOUNT, vars);
			} catch (Exception e) {
				LOGGER.error(String.format("Unable to start fulfilment process for new user %1$s",user.getEmail()), e);
			}
		} else { 
			LOGGER.debug("Authenticated user: "+ userDetails.getEmail());
		}
	}
	
}