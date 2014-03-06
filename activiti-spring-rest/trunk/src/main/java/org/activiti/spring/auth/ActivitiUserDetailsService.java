package org.activiti.spring.auth;

import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.IdentityService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.identity.User;
import org.activiti.spring.rest.web.UserRecordController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class ActivitiUserDetailsService extends ExternalUserDetailsService{ 

	private static final String PROC_FULFIL_ACCOUNT = "FulfilAccount";

	protected static final Logger LOGGER = LoggerFactory
			.getLogger(UserRecordController.class);

	@Autowired 
	protected ProcessEngine processEngine ; 
	
	public void updateLocalUser(User userDetails) {
		IdentityService idSvc = processEngine.getIdentityService(); 
		long count = idSvc.createUserQuery().userEmail(userDetails.getEmail()).count(); 
		if (count == 0) { 
			LOGGER.info(String.format("Authenticated unknown user '%1$s' starting process $2$s",userDetails.getEmail(), PROC_FULFIL_ACCOUNT));
			User user = idSvc.newUser(userDetails.getEmail()); 
			user.setId(userDetails.getEmail());
			user.setEmail(userDetails.getEmail());
			user.setFirstName(userDetails.getFirstName());
			user.setLastName(userDetails.getLastName());
			
//			idSvc.saveUser(user); 
			// TODO This will only work when the process engine has been
			// injected need to add an alternative using REST invocation for
			// other cases.
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