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
package link.omny.mail;

import static org.junit.Assert.fail;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import link.omny.website.TestCredentials;

import org.activiti.bdd.ActivitiSpec;
import org.activiti.bdd.ext.DumpAuditTrail;
import org.activiti.bdd.test.activiti.ExtendedRule;
import org.activiti.bdd.test.mailserver.TestMailServer;
import org.activiti.engine.IdentityService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.identity.User;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class SendMemoDistributionIT {
    private static final String USER_EMAIL = "tim@knowprocess.com";

    private static final String SYSTEM_EMAIL = "info@omny.link";

    private static final String MSG_NAMESPACE = "omny";

    private static final String USERNAME = "tim";

    private static final String TENANT_ID = MSG_NAMESPACE;

    private static final String DISTRIBUTE_MEMO_KEY = "DistributeMemo";

    @Rule
    public ExtendedRule activitiRule = new ExtendedRule("test-activiti.cfg.xml");

    @Rule
    public TestMailServer mailServer = new TestMailServer();

    @Before
    public void setUp() {
        IdentityService idSvc = activitiRule.getIdentityService();
        User newUser = idSvc.newUser(USERNAME);
        newUser.setEmail(SYSTEM_EMAIL);
        idSvc.saveUser(newUser);

        TestCredentials.initBot(idSvc, TENANT_ID);

        ProcessEngine processEngine = activitiRule.getProcessEngine();
        System.out.println("" + processEngine);
    }

    @After
    public void tearDown() {
        IdentityService idSvc = activitiRule.getIdentityService();
        idSvc.deleteUser(USERNAME);

        TestCredentials.removeBot(idSvc, TENANT_ID);
    }

    @Test
    @org.activiti.engine.test.Deployment(resources = {
            "processes/link/omny/mail/DistributeMemo.bpmn",
            "processes/link/omny/custmgmt/AddActivityToContact.bpmn" }, tenantId = TENANT_ID)
    public void testMemoDistributionToIndividuals() {
        try {
            Set<String> collectVars = new HashSet<String>();
            Map<String, Object> putVars = new HashMap<String, Object>();
            putVars.put("tenantId", TENANT_ID);
            putVars.put("distributionId", "1");
            new ActivitiSpec(activitiRule, "testMemoDistributionToIndividuals")
                    .whenEventOccurs("", DISTRIBUTE_MEMO_KEY, collectVars,
                            putVars, TENANT_ID).whenExecuteJobsForTime(3000)
                    .thenProcessIsComplete()
                    .thenExtension(new DumpAuditTrail(activitiRule));

            // TODO assert activity added

            mailServer.assertEmailSend(0, true, "A test email",
                    "<h1><b>Title</b></h1>", SYSTEM_EMAIL,
                    Collections.singletonList(USER_EMAIL));
            mailServer.dumpMailSent();
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    @org.activiti.engine.test.Deployment(resources = {
            "processes/link/omny/mail/DistributeMemo.bpmn",
            "processes/link/omny/custmgmt/AddActivityToContact.bpmn" }, tenantId = TENANT_ID)
    public void testMemoDistributionToList() {
        try {
            Set<String> collectVars = new HashSet<String>();
            Map<String, Object> putVars = new HashMap<String, Object>();
            putVars.put("tenantId", TENANT_ID);
            putVars.put("distributionId", "2");
            new ActivitiSpec(activitiRule, "testMemoDistributionToList")
                    .whenEventOccurs("", DISTRIBUTE_MEMO_KEY, collectVars,
                            putVars, TENANT_ID)
                    .whenExecuteJobsForTime(3000)
                    .thenProcessIsComplete()
                    .thenExtension(new DumpAuditTrail(activitiRule));

            // TODO assert activity added

            mailServer.assertEmailSend(0, true, "A test email",
                    "<h1><b>Title</b></h1>", SYSTEM_EMAIL,
                    Collections.singletonList(USER_EMAIL));
            mailServer.dumpMailSent();
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

}
