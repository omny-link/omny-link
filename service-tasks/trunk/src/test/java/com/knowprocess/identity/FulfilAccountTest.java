package com.knowprocess.identity;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.mail.MessagingException;

import org.activiti.engine.IdentityService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.identity.User;
import org.activiti.engine.impl.identity.Authentication;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.runtime.ProcessInstance;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.knowprocess.test.activiti.ExtendedRule;
import com.knowprocess.test.mailserver.TestMailServer;

public class FulfilAccountTest {

	private static final String ADMIN_EMAIL = "donotreply@knowprocess.com";

	private static final String PROCESS_NAME = "FulfilAccount";

	private static final String ADMIN_USERNAME = "Tim";

	private static final String USER_LNAME = "Titmus";

	private static final String USER_FNAME = "Fred";

	private static final String USER_EMAIL = "fred@acme.com";

	@Rule
	public ExtendedRule activitiRule = new ExtendedRule("test-activiti.cfg.xml");

	@Rule
	public TestMailServer mailServer = new TestMailServer() ; 

	private Map<String, Object> variableMap;

	@Before
	public void setUp() {
		variableMap = new HashMap<String, Object>();

		IdentityService idSvc = activitiRule.getIdentityService();
		idSvc.saveUser(idSvc.newUser(ADMIN_USERNAME));
	}

	@After
	public void tearDown() {
		IdentityService idSvc = activitiRule.getIdentityService();
		idSvc.deleteUser(ADMIN_USERNAME);
	}

	@Test
	public void testSimpleAccountFulfilment() {
		ProcessInstance processInstance = setupFulfilAccount();
		
		assertEquals(1,activitiRule.getIdentityService().createUserQuery().userId(USER_EMAIL).count());
		assertEquals(1,activitiRule.getIdentityService().createUserQuery().userEmail(USER_EMAIL).count());
		
		activitiRule.assertComplete(processInstance);
		
		try {
			mailServer.dumpMailSent();
			
			mailServer.assertEmailSend(0, true, USER_FNAME, "The Know Process team", ADMIN_EMAIL, Collections.singletonList(USER_EMAIL));
		} catch (MessagingException e) {
			e.printStackTrace();
			fail();
		} catch (IOException e) {
			e.printStackTrace();
			fail(); 
		}
		
		activitiRule.dumpAuditTrail(processInstance.getId());
		activitiRule.dumpVariables(processInstance.getId());
	}

	private ProcessInstance setupFulfilAccount() {
		Deployment deployment = activitiRule
				.getRepositoryService()
				.createDeployment()
				.name(PROCESS_NAME)
				.addClasspathResource(
						"process/com/knowprocess/usermgmt/" + PROCESS_NAME
								+ ".bpmn")
				.deploy();
		assertNotNull(deployment);

		User user = activitiRule.getIdentityService().newUser(USER_EMAIL); 
		user.setId(USER_EMAIL);
		user.setEmail(USER_EMAIL);
		user.setFirstName(USER_FNAME);
		user.setLastName(USER_LNAME);
				variableMap.put("user", user);

		Authentication.setAuthenticatedUserId(ADMIN_USERNAME);
		variableMap.put("initiator", ADMIN_USERNAME);
		RuntimeService runtimeService = activitiRule.getRuntimeService();
		ProcessInstance processInstance = runtimeService
				.startProcessInstanceByKey(PROCESS_NAME, variableMap);
		assertNotNull(processInstance.getId());
		System.out.println("id " + processInstance.getId() + " "
				+ processInstance.getProcessDefinitionId());
		return processInstance;
	}

}