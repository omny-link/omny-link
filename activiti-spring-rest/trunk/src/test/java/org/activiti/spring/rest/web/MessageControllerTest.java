package org.activiti.spring.rest.web;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.activiti.engine.IdentityService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.impl.identity.Authentication;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.spring.rest.cors.PreAuthenticatedAuthentication;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.util.UriComponentsBuilder;

import com.knowprocess.test.activiti.ExtendedRule;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({
		"/META-INF/spring/applicationContext-activiti-spring-rest.xml",
		"/META-INF/spring/applicationContext-test.xml" })
public class MessageControllerTest {

	private static final String BASE_URL = "http://localhost";

	private static final String MSG_NAME = "kp.testInOutMep";

	private static final String PROCESS_NAME = "MessageControllerTest";

	private static final String PROCESS_RESOURCE = "processes/" + PROCESS_NAME
			+ ".bpmn";

	private static final String USERNAME = "tim@knowprocess.com";

	@Rule
	public ExtendedRule activitiRule;

	private Map<String, Object> variableMap;

	private MessageController svc;

	@Autowired
	public ProcessEngine processEngine;

	@Before
	public void setUp() {
		activitiRule = new ExtendedRule(processEngine);
		variableMap = new HashMap<String, Object>();

		svc = new MessageController();
		svc.setProcessEngine(processEngine);

		SecurityContextHolder.getContext().setAuthentication(
				new PreAuthenticatedAuthentication(USERNAME));
	}

	@After
	public void tearDown() {
		IdentityService idSvc = processEngine.getIdentityService();
		idSvc.deleteUser(USERNAME);
	}

	@Test
	public void testInOutMep() {
		Deployment deployment = processEngine.getRepositoryService()
				.createDeployment().name(PROCESS_NAME)
				.addClasspathResource(PROCESS_RESOURCE)
				.deploy();
		assertNotNull(deployment);

		String json = "[{\"contact\":{\"firstName\":\"John\","
				+ "\"email\":\"john@knowprocess.com\"}}]";
		variableMap.put(MSG_NAME, json);

		Authentication.setAuthenticatedUserId(USERNAME);
		variableMap.put("initiator", USERNAME);
		RuntimeService runtimeService = processEngine.getRuntimeService();
		ProcessInstance processInstance = runtimeService
				.startProcessInstanceByMessage(MSG_NAME, variableMap);
		assertNotNull(processInstance.getId());
		System.out.println("id " + processInstance.getId() + " "
				+ processInstance.getProcessDefinitionId());

		ResponseEntity<String> response = svc.doInOutMep(
				UriComponentsBuilder.fromHttpUrl(BASE_URL), MSG_NAME, json);
		for (Entry<String, List<String>> entry : response.getHeaders()
				.entrySet()) {
			System.out.println("response header: " + entry.getKey() + ": "
					+ entry.getValue());
		}
		System.out.println("response body: " + response.getBody());
		assertEquals("{\"initiator\": \"" + USERNAME + "\"}",
				response.getBody());
		activitiRule.assertVariableValue(processInstance.getId(), "initiator",
				USERNAME);

		activitiRule.assertComplete(processInstance);
		activitiRule.dumpVariables(processInstance.getId());
	}

}