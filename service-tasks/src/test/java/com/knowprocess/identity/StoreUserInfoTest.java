/*******************************************************************************
 *Copyright 2018 Tim Stephenson and contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package com.knowprocess.identity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.HashMap;
import java.util.Map;

import org.activiti.bdd.test.activiti.ExtendedRule;
import org.activiti.engine.IdentityService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.impl.identity.Authentication;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.runtime.ProcessInstance;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;


public class StoreUserInfoTest {

	private static final String KEY = "linkedIn.secret";

	private static final String VALUE = "gv76cvzu8";

	private static final String MSG_NAME = "kp.userInfo";

	private static final String PROCESS_NAME = "StoreUserInfo";

	private static final String USERNAME = "tim";

	@Rule
	public ExtendedRule activitiRule = new ExtendedRule("test-activiti.cfg.xml");


	private Map<String, Object> variableMap;

	@Before
	public void setUp() {
		variableMap = new HashMap<String, Object>();

		IdentityService idSvc = activitiRule.getIdentityService();
		idSvc.saveUser(idSvc.newUser(USERNAME));
	}

	@After
	public void tearDown() {
		IdentityService idSvc = activitiRule.getIdentityService();
		idSvc.deleteUser(USERNAME);
	}

	@Test
	public void testStoreUserInfo() {
		ProcessInstance processInstance = setupStoreUserInfo();

		activitiRule.assertComplete(processInstance);


		String value = activitiRule.getIdentityService().getUserInfo(USERNAME,
				KEY);
		assertEquals(VALUE, value);

		activitiRule.dumpAuditTrail(processInstance.getId());
		activitiRule.dumpVariables(processInstance.getId());
	}

	private ProcessInstance setupStoreUserInfo() {
		Deployment deployment = activitiRule
				.getRepositoryService()
				.createDeployment()
				.name(PROCESS_NAME)
				.addClasspathResource(
						"process/com/knowprocess/usermgmt/" + PROCESS_NAME
								+ ".bpmn")
				.deploy();
		assertNotNull(deployment);

		String json = "{\"username\":\"" + USERNAME + "\"," + "\"key\":\""
				+ KEY + "\"," + "\"value\":\"" + VALUE + "\"}";
		variableMap.put(MSG_NAME, json);

		Authentication.setAuthenticatedUserId(USERNAME);
		variableMap.put("initiator", USERNAME);
		RuntimeService runtimeService = activitiRule.getRuntimeService();
		ProcessInstance processInstance = runtimeService
				.startProcessInstanceByMessage(MSG_NAME, variableMap);
		assertNotNull(processInstance.getId());
		System.out.println("id " + processInstance.getId() + " "
				+ processInstance.getProcessDefinitionId());
		return processInstance;
	}

}
