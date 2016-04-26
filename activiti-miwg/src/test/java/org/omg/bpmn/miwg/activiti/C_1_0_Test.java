package org.omg.bpmn.miwg.activiti;

import java.util.HashMap;
import java.util.Map;

import org.activiti.bdd.ActivitiSpec;
import org.activiti.bdd.ext.DumpAuditTrail;
import org.activiti.engine.identity.User;
import org.activiti.engine.test.ActivitiRule;
import org.activiti.engine.test.Deployment;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.toxos.activiti.assertion.DefaultProcessAssertConfiguration;
import org.toxos.activiti.assertion.ProcessAssert;

public class C_1_0_Test {

    private static final String VAR_INITIATOR = "initiator";

    private static final String VAR_APPROVER = "approver";

    private static final String VAR_APPROVED = "approved";

    private static final String USERNAME = "teamAssistant";

    private static final String PROCESS_KEY = "handle-invoice";

    @Rule
    public ActivitiRule activitiRule = new ActivitiRule("test-activiti.cfg.xml");

    private Map<String, Object> vars = new HashMap<String, Object>();

    @Before
    public void setUp() {
        ProcessAssert.setConfiguration(new DefaultProcessAssertConfiguration(
                activitiRule));

        User user = activitiRule.getIdentityService().newUser(USERNAME);
        activitiRule.getIdentityService().saveUser(user);
    }

    @After
    public void tearDown() {
        activitiRule.getIdentityService().deleteUser(USERNAME);
    }

    @Test
    @Deployment(resources = { "processes/C_1_0-handle-invoice.bpmn" })
    public void testC_1_0_SuccessfullyApprovedInvoice() throws Exception {
        new ActivitiSpec(activitiRule, "test")
                .whenEventOccurs(
                        "Manual start event occurs",
                        PROCESS_KEY,
                        ActivitiSpec.buildSet(),
                        ActivitiSpec
                                .buildMap(new ImmutablePair<String, Object>(
                                        VAR_INITIATOR, USERNAME)))
                .thenUserTask(
                        "assignApprover",
                        ActivitiSpec.emptySet(),
                        ActivitiSpec
                                .buildMap(new ImmutablePair<String, Object>(
                                        VAR_APPROVER, USERNAME)))
//                .thenUserTask(
//                        "approveInvoice",
//                        ActivitiSpec.emptySet(),
//                        ActivitiSpec
//                                .buildMap(new ImmutablePair<String, Object>(
//                                        VAR_APPROVED, "true")))
//                .thenUserTask("prepareBankTransfer", ActivitiSpec.emptySet(),
//                        ActivitiSpec.buildMap())
                // .thenProcessIsComplete()
                .thenExtension(new DumpAuditTrail(activitiRule));
    }

}
