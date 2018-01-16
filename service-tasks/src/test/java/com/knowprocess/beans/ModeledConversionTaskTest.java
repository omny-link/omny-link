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
package com.knowprocess.beans;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.HashMap;
import java.util.Map;

import org.activiti.bdd.test.activiti.ExtendedRule;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.test.Deployment;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.knowprocess.beans.model.LeadActivity;
import com.knowprocess.sugarcrm.api.SugarLead;

public class ModeledConversionTaskTest {

    public static final String MSG_NAME = "kp.account";

    @Rule
    public ExtendedRule activitiRule = new ExtendedRule(
            "test-activiti.cfg.xml");

    private static final String INITIATOR = "tim@knowprocess.com";

    @Before
    public void setUp() {
    }

    @Test
    @Deployment(resources = { "processes/ModelBasedBeanConversionTask.bpmn" })
    public void testConversionTaskInProcess() {
        Map<String, Object> variableMap = new HashMap<String, Object>();
        variableMap.put("initiator", INITIATOR);
        LeadActivity lead = new LeadActivity("User read article XYZ");
        variableMap.put("source", lead);
        String msg = "{}";
        variableMap.put(MSG_NAME, msg);

        RuntimeService runtimeService = activitiRule.getRuntimeService();
        ProcessInstance processInstance = runtimeService
                .startProcessInstanceByMessage(MSG_NAME, variableMap);
        assertNotNull(processInstance.getId());
        System.out.println("id " + processInstance.getId() + " "
                + processInstance.getProcessDefinitionId());
        SugarLead sugarLead = (SugarLead) activitiRule
                .getHistoryService().createHistoricVariableInstanceQuery()
                .processInstanceId(processInstance.getId())
                .variableName("target").singleResult().getValue();
        System.out.println(" returned: " + sugarLead);
        assertNotNull(sugarLead);
        assertEquals(lead.getDateOfActivity(), sugarLead.getDateEntered());
        assertEquals(lead.getDescription(), sugarLead.getDescription());

        activitiRule.assertComplete(processInstance);
        activitiRule.dumpVariables(processInstance.getId());
    }

}
