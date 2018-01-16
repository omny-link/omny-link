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
package link.omny.acctmgmt;

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

public class CreateTenancyTest {

    private static final String TENANT_ID = "acme";

    private static final String USER_EMAIL = "tim@knowprocess.com";

    private static final String SYSTEM_EMAIL = "info@omny.link";

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
            "processes/link/omny/acctmgmt/CreateTenancy.bpmn",
            "processes/link/omny/alerts/SendAlertEmail.bpmn", }, tenantId = TENANT_ID)
    public void testCreateTenancyWithoutWebsite()
            throws Exception {
        new ActivitiSpec(activitiRule,
                "testCreateTenancyWithoutWebsite")
                .whenMsgReceived("New tenancy requested", "omny.tenantSpec", 
                        "/omny.tenantSpecWithoutWebsite.json", TENANT_ID)
                .thenUserTask("createRepo", ActivitiSpec.emptySet(),
                        ActivitiSpec.buildMap())
                .thenUserTask("createTenantJson", ActivitiSpec.emptySet(),
                        ActivitiSpec.buildMap())
                .thenUserTask("createTenantRecord", ActivitiSpec.emptySet(),
                        ActivitiSpec.buildMap())
                .thenSubProcessCalled("SendAlert") // task escalation
                .thenUserTask("verifyTenancy", ActivitiSpec.emptySet(),
                        ActivitiSpec.buildMap())
                .thenUserTask("createTenantAdmin", ActivitiSpec.emptySet(),
                        ActivitiSpec.buildMap())
                .thenSubProcessCalled("SendAlert")
                .thenProcessEndedAndInExclusiveEndEvent("endEvent")
                .thenExtension(new DumpAuditTrail(activitiRule));
        
        mailServer.assertEmailSend(0, true, 
                String.format("Omny Link: New tenant %1$s is available", TENANT_ID),
                "https://api.omny.link", SYSTEM_EMAIL,
                Collections.singletonList(USER_EMAIL));
        mailServer.assertEmailSend(1, true, 
                String.format("Omny Link: New tenant %1$s is live", TENANT_ID),
                "https://api.omny.link", SYSTEM_EMAIL,
                Collections.singletonList(USER_EMAIL));
        mailServer.dumpMailSent();
    }

    @SuppressWarnings("unchecked")
    @Test
    @org.activiti.engine.test.Deployment(resources = {
            "processes/link/omny/acctmgmt/CreateTenancy.bpmn",
            "processes/link/omny/alerts/SendAlertEmail.bpmn", }, tenantId = TENANT_ID)
    public void testCreateTenancyWithWebsite()
            throws Exception {
        new ActivitiSpec(activitiRule,
                "testCreateTenancyWithWebsite")
                .whenMsgReceived("New tenancy requested", "omny.tenantSpec", 
                        "/omny.tenantSpecWithWebsite.json", TENANT_ID)
                .thenUserTask("createRepo", ActivitiSpec.emptySet(),
                        ActivitiSpec.buildMap())
                .thenUserTask("provisionWebsite", ActivitiSpec.emptySet(),
                        ActivitiSpec.buildMap())
                .thenUserTask("connectGitium", ActivitiSpec.emptySet(),
                        ActivitiSpec.buildMap())
                .thenUserTask("createTenantJson", ActivitiSpec.emptySet(),
                        ActivitiSpec.buildMap())
                .thenUserTask("createTenantRecord", ActivitiSpec.emptySet(),
                        ActivitiSpec.buildMap())
                .thenSubProcessCalled("SendAlert") // task escalation
                .thenUserTask("verifyTenancy", ActivitiSpec.emptySet(),
                        ActivitiSpec.buildMap())
                .thenUserTask("createTenantAdmin", ActivitiSpec.emptySet(),
                        ActivitiSpec.buildMap())
                .thenSubProcessCalled("SendAlert")
                .thenProcessEndedAndInExclusiveEndEvent("endEvent")
                .thenExtension(new DumpAuditTrail(activitiRule));
        
        mailServer.assertEmailSend(0, true, 
                String.format("Omny Link: New tenant %1$s is available", TENANT_ID),
                "https://api.omny.link", SYSTEM_EMAIL,
                Collections.singletonList(USER_EMAIL));
        mailServer.assertEmailSend(1, true, 
                String.format("Omny Link: New tenant %1$s is live", TENANT_ID),
                "https://api.omny.link", SYSTEM_EMAIL,
                Collections.singletonList(USER_EMAIL));
        mailServer.dumpMailSent();
    }
}
