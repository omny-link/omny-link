package com.knowprocess.in;

import static org.junit.Assert.assertNotNull;

import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.RuntimeService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.runtime.ProcessInstance;
import org.junit.Before;
import org.junit.Test;

public class LinkedInProcessTest extends AbstractLinkedInTest {

	public static final String MSG_LINKEDIN_MAILSHOT = "kp.linkedInMailshot";

	@Before
	public void setUp() {
		super.setUp();
	}

	@Test
	public void testScriptTask() {
		Deployment deployment = activitiRule.getRepositoryService()
				.createDeployment()
				.addClasspathResource("process/LinkedInProcessTest.bpmn")
				.deploy();
		assertNotNull(deployment);

		Map<String, Object> variableMap = new HashMap<String, Object>();
		variableMap.put("initiator", INITIATOR);

		RuntimeService runtimeService = activitiRule.getRuntimeService();
		ProcessInstance processInstance = runtimeService
				.startProcessInstanceByMessage(MSG_LINKEDIN_MAILSHOT,
						variableMap);
		assertNotNull(processInstance.getId());
		System.out.println("id " + processInstance.getId() + " "
				+ processInstance.getProcessDefinitionId());

		activitiRule.assertComplete(processInstance);
		activitiRule.dumpVariables(processInstance.getId());
	}

}