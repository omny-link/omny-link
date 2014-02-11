package com.knowprocess.explorer.resources;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.ws.rs.core.Response;

import org.activiti.engine.IdentityService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.impl.identity.Authentication;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.runtime.ProcessInstance;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.knowprocess.jaxrs.test.MockSecurityContext;
import com.knowprocess.jaxrs.test.MockUriInfo;
import com.knowprocess.test.activiti.ExtendedRule;

public class MessageResourceTest {

	private static final String MSG_NAME = "kp.testInOutMep";

	private static final String PROCESS_NAME = "MsgInMsgOutTest";

	private static final String USERNAME = "tim";

	@Rule
	public ExtendedRule activitiRule = new ExtendedRule("test-activiti.cfg.xml");

	private Map<String, Object> variableMap;

	private MessageResource resource;

	@Before
	public void setUp() {
		variableMap = new HashMap<String, Object>();

		resource = new MessageResource();
		resource.setUriInfo(new MockUriInfo());
	}

	@After
	public void tearDown() {
		IdentityService idSvc = activitiRule.getIdentityService();
		idSvc.deleteUser(USERNAME);
	}

	@Test
	public void testInOutMep() {
		Deployment deployment = activitiRule.getRepositoryService()
				.createDeployment().name(PROCESS_NAME)
				.addClasspathResource("process/" + PROCESS_NAME + ".bpmn")
				// .addClasspathResource("templates/" + PROCESS_NAME + ".html")
				// .addClasspathResource("templates/" + PROCESS_NAME + ".txt")
				.deploy();
		assertNotNull(deployment);

		String json = "[{\"contact\":{\"firstName\":\"John\","
				+ "\"email\":\"john@knowprocess.com\"}}]";
		variableMap.put(MSG_NAME, json);

		Authentication.setAuthenticatedUserId(USERNAME);
		variableMap.put("initiator", USERNAME);
		RuntimeService runtimeService = activitiRule.getRuntimeService();
		ProcessInstance processInstance = runtimeService
				.startProcessInstanceByMessage(MSG_NAME, variableMap);
		assertNotNull(processInstance.getId());
		System.out.println("id " + processInstance.getId() + " "
				+ processInstance.getProcessDefinitionId());

		Response response = resource.doInOutMep(new MockSecurityContext(
				USERNAME), MSG_NAME, json);
		for (Entry<String, List<Object>> entry : response.getMetadata()
				.entrySet()) {
			System.out.println("response header: " + entry.getKey() + ": "
					+ entry.getValue());
		}
		System.out.println("response body: " + response.getEntity());
		assertEquals("{\"initiator\": \"" + USERNAME + "\"}",
				response.getEntity());
		activitiRule.assertVariableValue(processInstance.getId(), "initiator",
				USERNAME);

		activitiRule.assertComplete(processInstance);
		activitiRule.dumpVariables(processInstance.getId());
	}

}