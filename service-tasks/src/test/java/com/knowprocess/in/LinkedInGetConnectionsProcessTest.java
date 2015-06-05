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