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
package com.knowprocess.bpm.web;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.test.ActivitiRule;
import org.activiti.engine.test.Deployment;
import org.junit.Rule;
import org.junit.Test;

import com.knowprocess.bpm.impl.JsonManager;
import com.knowprocess.bpm.impl.MessageRegistry;
import com.knowprocess.bpm.model.ProcessInstance;

public class SpamMsgDetectionTest {

    @Rule
    public ActivitiRule activitiRule = new ActivitiRule("test-activiti.cfg.xml");

    protected MessageController svc = new MessageController();

    @Test
    public void isEmptyJsonTest() {
        String json = "{ \"id\": \"\" }";
        assertTrue(svc.isEmptyJson(json));
    }

    @Test
    public void isNotEmptyJsonTest() {
        String json = "{ \"id\": \"abc123\" }";
        assertTrue(!svc.isEmptyJson(json));
    }

    @Test
    public void isEmptyJsonArrayTest() {
        String json = "[]";
        assertTrue(svc.isEmptyJson(json));
    }

    @Test
    public void isNotEmptyJsonArrayTest() {
        String json = "[{ \"id\": \"abc123\" }]";
        assertTrue(!svc.isEmptyJson(json));
    }

    @Test
    public void issue50Test() {
        String json = "{\"customFields\":{\"ebitda\":\"78000\",\"accountName\":\"Billington Travel\",\"surplus\":\"348000\",\"borrowing\":\"0\"},\"stage\":\"Enquiry\",\"accountType\":\"Customer\",\"enquiryType\":\"Valuation\",\"useEbitda\":\"ebitda\",\"firstName\":\"david\",\"lastName\":\"carter\",\"email\":\"dchome@live.co.uk\",\"tenantId\":\"client1\",\"admin_email\":\"john@unloq.co.uk\"}";
        assertTrue(!svc.isEmptyJson(json));
    }

    @Test
    public void tcgIssueTest() {
        String json = "{\"to\": {\"lat\":51.432916,\"long\":-2.190617 },\"from\": {\"lat\":51.469675,\"long\":-0.491393 }}";
        assertTrue(!svc.isEmptyJson(json));
    }

    @Test
    @Deployment(resources = { "processes/NoOp.bpmn" }, tenantId = "omny")
    public void issue51NoPayloadTest() {
        new ProcessInstance().setProcessEngine(activitiRule.getProcessEngine());
        svc.setProcessEngine(activitiRule.getProcessEngine());
        svc.messageRegistry = new MessageRegistry();
        svc.jsonManager = new JsonManager();
        Map<String, Object> vars = new HashMap<String, Object>();
        ProcessInstance instance = svc.handleMep("omny", "omny.noOp",
                "biz key", null, vars, 0);

        assertNotNull(instance);
    }
}
