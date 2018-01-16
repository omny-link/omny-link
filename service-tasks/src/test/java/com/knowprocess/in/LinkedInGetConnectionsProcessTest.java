/*******************************************************************************
 *Copyright 2011-2018 Tim Stephenson and contributors
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
package com.knowprocess.in;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonReader;

import org.activiti.engine.RuntimeService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.runtime.ProcessInstance;
import org.junit.Before;
import org.junit.Test;

public class LinkedInGetConnectionsProcessTest extends AbstractLinkedInTest {

    public static final String MSG_NAME = "kp.linkedInConnections";

    @Before
    public void setUp() {
        super.setUp();
    }

    @Test
    public void testGetUnfilteredConnectionsTask() {
        deploy();

        JsonArray readArray = execInOutMep("");
        assertTrue(readArray.size() > 0);
    }

    @Test
    public void testGetFilteredConnectionsTask() {
        deploy();

        JsonArray readArray = execInOutMep("{\"name\":\"Stephenson\"}");
        assertEquals(4, readArray.size());
    }

    private JsonArray execInOutMep(String msg) {
        Map<String, Object> variableMap = new HashMap<String, Object>();
        variableMap.put("initiator", INITIATOR);
        variableMap.put(MSG_NAME, msg);

        RuntimeService runtimeService = activitiRule.getRuntimeService();
        ProcessInstance processInstance = runtimeService
                .startProcessInstanceByMessage(MSG_NAME, variableMap);
        assertNotNull(processInstance.getId());
        System.out.println("id " + processInstance.getId() + " "
                + processInstance.getProcessDefinitionId());
        String responseJson = (String) activitiRule
                .getHistoryService().createHistoricVariableInstanceQuery()
                .processInstanceId(processInstance.getId())
                .variableName(MSG_NAME).singleResult().getValue();
        // System.out.println(MSG_NAME + " returned: " + responseJson);
        assertNotNull(responseJson);
        JsonReader reader = Json.createReader(new StringReader(responseJson));
        JsonArray readArray = reader.readArray();
        // activitiRule.assertComplete(processInstance);
        // activitiRule.dumpVariables(processInstance.getId());

        return readArray;
    }

    private void deploy() {
        Deployment deployment = activitiRule
                .getRepositoryService()
                .createDeployment()
                .addClasspathResource(
                        "process/com/knowprocess/in/GetConnections.bpmn")
                .deploy();
        assertNotNull(deployment);
    }

}
