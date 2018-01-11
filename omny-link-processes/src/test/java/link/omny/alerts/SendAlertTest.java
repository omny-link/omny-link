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
package link.omny.alerts;

import java.util.Collections;

import org.activiti.bdd.ActivitiSpec;
import org.activiti.bdd.ext.DumpAuditTrail;
import org.activiti.bdd.test.activiti.ExtendedRule;
import org.activiti.bdd.test.mailserver.TestMailServer;
import org.activiti.engine.IdentityService;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import link.omny.website.TestCredentials;

public class SendAlertTest {

    private static final String ALERT_BODY = "This email is a test";

    private static final String ALERT_SUBJECT = "Test alert";

    private static final String TENANT_ID = "acme";

    private static final String USER_EMAIL = "tim@omny.link";

    private static final String SYSTEM_EMAIL = "info@omny.link";

    private static final String SEND_ALERT_KEY = "SendAlert";

    private static final String SEND_ALERT_TO_OWNER_KEY = "SendAlertToOwner";
    
    @Rule
    public ExtendedRule activitiRule = new ExtendedRule("test-activiti.cfg.xml");

    @Rule
    public TestMailServer mailServer = new TestMailServer();

    @Before
    public void setUp() {
        IdentityService idSvc = activitiRule.getIdentityService();
        TestCredentials.initBot(idSvc, TENANT_ID);
    }

    @After
    public void tearDown() {
        IdentityService idSvc = activitiRule.getIdentityService();
        TestCredentials.removeBot(idSvc, TENANT_ID);
    }

    @SuppressWarnings("unchecked")
    @Test
    @org.activiti.engine.test.Deployment(resources = {
            "processes/link/omny/alerts/SendAlertEmail.bpmn", }, tenantId = TENANT_ID)
    public void testSendAlertEmail()
            throws Exception {
        new ActivitiSpec(activitiRule, "testSendAlertEmail")
                .whenEventOccurs(
                        "Task created",
                        SEND_ALERT_KEY,
                        ActivitiSpec.emptySet(),
                        ActivitiSpec.buildMap(
                                ActivitiSpec.newPair("tenantId", TENANT_ID),
                                ActivitiSpec.newPair("addressee", USER_EMAIL),
                                ActivitiSpec.newPair("subject", ALERT_SUBJECT),
                                ActivitiSpec.newPair("message", ALERT_BODY)),
                        TENANT_ID)        
                .thenProcessEndedAndInExclusiveEndEvent("endEvent")
                .thenExtension(new DumpAuditTrail(activitiRule));
        
        mailServer.assertEmailSend(0, true, ALERT_SUBJECT, ALERT_BODY, 
                SYSTEM_EMAIL, Collections.singletonList(USER_EMAIL));
        mailServer.dumpMailSent();
    }

    @SuppressWarnings("unchecked")
    @Test
    @org.activiti.engine.test.Deployment(resources = {
            "processes/link/omny/alerts/SendAlertNoOp.bpmn", }, tenantId = TENANT_ID)
    public void testSendAlertNoOp()
            throws Exception {
        new ActivitiSpec(activitiRule, "testSendAlertNoOp")
                .whenEventOccurs(
                        "Task created",
                        SEND_ALERT_KEY,
                        ActivitiSpec.emptySet(),
                        ActivitiSpec.buildMap(
                                ActivitiSpec.newPair("tenantId", TENANT_ID),
                                ActivitiSpec.newPair("addressee", USER_EMAIL),
                                ActivitiSpec.newPair("subject", ALERT_SUBJECT),
                                ActivitiSpec.newPair("message", ALERT_BODY)),
                        TENANT_ID)        
                .thenProcessEndedAndInExclusiveEndEvent("endEvent")
                .thenExtension(new DumpAuditTrail(activitiRule));
    }

}
