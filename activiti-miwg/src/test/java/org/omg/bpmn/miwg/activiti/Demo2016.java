package org.omg.bpmn.miwg.activiti;

import org.activiti.bdd.ActivitiSpec;
import org.activiti.bdd.ext.DumpAuditTrail;
import org.activiti.bdd.test.activiti.ExtendedRule;
import org.activiti.engine.IdentityService;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;


public class Demo2016 {

    private static final String PROCESS_KEY = "Bpmn2_Process_uHFuwpo2EeO1WrMpxYFn7Q";

    private static final String USERNAME = "tstephen";

    // private static final String TENANT_ID = "firmgains";

    @Rule
    public ExtendedRule activitiRule = new ExtendedRule("test-activiti.cfg.xml");

    @Before
    public void setUp() {
        IdentityService idSvc = activitiRule.getIdentityService();
        idSvc.saveUser(idSvc.newUser(USERNAME));

        // TestCredentials.initBot(idSvc, TENANT_ID);
        //
        // contactId = TestCredentials.CUST_MGMT_URL + "/contacts/1";
    }

    @After
    public void tearDown() {
        IdentityService idSvc = activitiRule.getIdentityService();
        idSvc.deleteUser(USERNAME);
    }

    @SuppressWarnings("unchecked")
    @Test
    @org.activiti.engine.test.Deployment(resources = { "processes/Demo2016.bpmn" })
    public void testExecution() throws Exception {
        new ActivitiSpec(activitiRule, "test")
                .whenEventOccurs(
                        "Manual start event occurs",
                        PROCESS_KEY,
                        ActivitiSpec.buildSet(),
                        ActivitiSpec
                                .buildMap(new ImmutablePair<String, Object>(
                                        "initiator", USERNAME)))
                .thenUserTask(
                        "Bpmn2_UserTask_GARKYZo3EeO1WrMpxYFn7Q",
                        ActivitiSpec.emptySet(),
                        ActivitiSpec
                                .buildMap(new ImmutablePair<String, Object>(
                                        "Statut", "Fournie")))
                // .thenProcessIsComplete()
                .thenExtension(new DumpAuditTrail(activitiRule))
        ;

    }

}