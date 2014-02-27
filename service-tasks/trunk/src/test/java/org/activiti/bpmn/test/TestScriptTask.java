package org.activiti.bpmn.test;

import static org.junit.Assert.assertNotNull;

import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.RuntimeService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.runtime.ProcessInstance;
import org.junit.Rule;
import org.junit.Test;

import com.knowprocess.test.activiti.ExtendedRule;

public class TestScriptTask {

	@Rule
	public ExtendedRule activitiRule = new ExtendedRule("test-activiti.cfg.xml");

	@Test
	public void testScriptTask() {
		Deployment deployment = activitiRule.getRepositoryService()
				.createDeployment()
				.addClasspathResource("process/TestScriptTask.bpmn").deploy();
		assertNotNull(deployment);

		Map<String, Object> variableMap = new HashMap<String, Object>();

		RuntimeService runtimeService = activitiRule.getRuntimeService();
		ProcessInstance processInstance = runtimeService
				.startProcessInstanceByKey("TestScriptTask", variableMap);
		assertNotNull(processInstance.getId());
		System.out.println("id " + processInstance.getId() + " "
				+ processInstance.getProcessDefinitionId());

		activitiRule.assertComplete(processInstance);
		activitiRule.dumpVariables(processInstance.getId());
	}

}