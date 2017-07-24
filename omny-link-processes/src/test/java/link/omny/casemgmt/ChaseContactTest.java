package link.omny.casemgmt;

import java.util.Calendar;

import org.activiti.bdd.ActivitiSpec;
import org.activiti.bdd.ext.DumpAuditTrail;
import org.activiti.bdd.test.activiti.ExtendedRule;
import org.activiti.engine.IdentityService;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import link.omny.website.TestCredentials;

public class ChaseContactTest {

    private static final String MSG_NAMESPACE = "omny";

    private static final String USERNAME = "homer@springfield.com";

    private static final String DELEGATE = "marge@springfield.com";

    private static final String TENANT_ID = MSG_NAMESPACE;

    private static final String PROCESS_KEY = "ChaseContact";

    private static final String CONTACT_ID = "1";

    @Rule
    public ExtendedRule activitiRule = new ExtendedRule("test-activiti.cfg.xml");

    @Before
    public void setUp() {
        IdentityService idSvc = activitiRule.getIdentityService();
        idSvc.saveUser(idSvc.newUser(USERNAME));
        idSvc.saveUser(idSvc.newUser(DELEGATE));

        TestCredentials.initBot(idSvc, TENANT_ID);
    }

    @After
    public void tearDown() {
        IdentityService idSvc = activitiRule.getIdentityService();
        idSvc.deleteUser(USERNAME);
        idSvc.deleteUser(DELEGATE);

        TestCredentials.removeBot(idSvc, TENANT_ID);
    }

    @SuppressWarnings("unchecked")
    @Test
    @Ignore
    @org.activiti.engine.test.Deployment(resources = {
            "processes/link/omny/casemgmt/ChaseContact.bpmn",
            "processes/link/omny/alerts/SendAlertNoOp.bpmn",
            "processes/link/omny/mail/SendMemoNoOp.bpmn",
            "processes/link/omny/custmgmt/UpdateContactNoOp.bpmn" },
            tenantId = TENANT_ID)
    public void testChaseContactResponseAfter1Chase() throws Exception {
        new ActivitiSpec(activitiRule, "testChaseContactResponseAfter1Chase")
                .whenEventOccurs(
                        "Contract proposed",
                        PROCESS_KEY,
                        ActivitiSpec.emptySet(),
                        ActivitiSpec.buildMap(
                                ActivitiSpec.newPair("tenantId", TENANT_ID),
                                ActivitiSpec.newPair("contactId", CONTACT_ID),
                                ActivitiSpec.newPair("memoName", USERNAME)),
                        TENANT_ID)
                .whenExecuteJobsForTime(2000)
                .thenSubProcessCalled("SendMemo")
                // .thenWaitingAt("waitForRequiredInfo",
                // ActivitiSpec.emptySet())
                /* NB Activiti expects id of receiveTask not message name */
                .whenFollowUpMsgReceived("Contact replies with required info",
                        "waitForRequiredInfo",
                        "/omny.contactRequiredInfo.json", TENANT_ID)
                .whenExecuteJobsForTime(2000)
                .thenSubProcessCalled("UpdateContact")
                .thenProcessEndedAndInExclusiveEndEvent("endEvent")
                .thenExtension(new DumpAuditTrail(activitiRule));
    }

    @SuppressWarnings("unchecked")
    @Ignore
    @Test
    @org.activiti.engine.test.Deployment(resources = {
            "processes/link/omny/casemgmt/ChaseContact.bpmn",
            "processes/link/omny/alerts/SendAlertNoOp.bpmn",
            "processes/link/omny/mail/SendMemoNoOp.bpmn",
            "processes/link/omny/custmgmt/UpdateContactNoOp.bpmn" }, tenantId = TENANT_ID)
    public void testChaseContactResponseAfter2Chases() throws Exception {
        new ActivitiSpec(activitiRule, "testChaseContactResponseAfter2Chases")
                .whenEventOccurs(
                        "Contract proposed",
                        PROCESS_KEY,
                        ActivitiSpec.emptySet(),
                        ActivitiSpec.buildMap(
                                ActivitiSpec.newPair("tenantId", TENANT_ID),
                                ActivitiSpec.newPair("contactId", CONTACT_ID),
                                ActivitiSpec.newPair("memoName", USERNAME)),
                        TENANT_ID)
                // .thenVariableEquals("waitDuration", "P1D")
                .whenExecuteJobsForTime(2000)
                .thenSubProcessCalled("SendMemo")
                // .thenWaitingAt("waitForRequiredInfo",
                // ActivitiSpec.emptySet())
                .whenProcessTimePassed(Calendar.DATE, 2)
                .thenExtension(new DumpAuditTrail(activitiRule))
                // .thenTimerExpired("waitForRequiredInfoTimer")
                /* NB Activiti expects id of receiveTask not message name */
                // .whenFollowUpMsgReceived("Contact replies with required info",
                // "waitForRequiredInfo",
                // "/omny.contactRequiredInfo.json", TENANT_ID)
                // .whenExecuteJobsForTime(2000)
                .thenSubProcessCalled("SendAlert")
                .thenProcessEndedAndInExclusiveEndEvent("endEvent")
                .thenExtension(new DumpAuditTrail(activitiRule));
    }
}