package link.omny.website;

import static org.junit.Assert.fail;

import java.io.IOException;

import org.activiti.bdd.ActivitiSpec;
import org.activiti.engine.IdentityService;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.knowprocess.bpm.test.actions.DumpAuditTrail;
import com.knowprocess.resource.spi.RestDelete;
import com.knowprocess.test.activiti.ExtendedRule;
import com.knowprocess.test.mailserver.TestMailServer;

public class RegisterOmnyContactTest {

    private static final String MSG_NAMESPACE = "omny";

    private static final String USERNAME = "tim";

    private static final String TENANT_ID = MSG_NAMESPACE;

    private static final String REGISTER_SELF_MSG = MSG_NAMESPACE
            + ".registration";

    @Rule
    public ExtendedRule activitiRule = new ExtendedRule("test-activiti.cfg.xml");

    @Rule
    public TestMailServer mailServer = new TestMailServer();

    @Before
    public void setUp() {
        IdentityService idSvc = activitiRule.getIdentityService();
        idSvc.saveUser(idSvc.newUser(USERNAME));

        TestCredentials.initBot(idSvc, TENANT_ID);
    }

    @After
    public void tearDown() {
        IdentityService idSvc = activitiRule.getIdentityService();
        idSvc.deleteUser(USERNAME);

        TestCredentials.removeBot(idSvc, TENANT_ID);
    }

    @Test
    @org.activiti.engine.test.Deployment(resources = {
            "processes/link/omny/website/RegisterOmnyContact.bpmn",
            "processes/link/omny/custmgmt/CreateContactAndAccount.bpmn",
            "processes/link/omny/custmgmt/AddNoteToContact.bpmn",
            "processes/link/omny/mail/ConfirmEmail.bpmn",
            "processes/link/omny/mail/SendAddressConfirmationEmail.bpmn",
            "processes/link/omny/mail/SendWelcomeEmail.bpmn" }, tenantId = TENANT_ID)
    public void testMinimalRegistration() {
        try {
            ActivitiSpec spec = new ActivitiSpec(activitiRule,
                    "testMinimalRegistration")
                    .startByMsg(REGISTER_SELF_MSG, "/omny.registration.json",
                            TENANT_ID)
                    .executeJobsForTime(5000)
                    // TODO This does not work due to the Activiti ReceiveTask
                    // implementation
                    // Consider intermediate msgEvent but then cannot use
                    // timeout
//                    .receiveMessage(
//                            "awaitConfirmation",
//                            ActivitiSpec.buildMap(ActivitiSpec.newPair(
//                                    "contactEmail", REGISTRANT_EMAIL)))
                     .receiveSignal("awaitConfirmation")
                    // Note: sub-proc only returns id after msg received
                    .collectVar("contactId")
                    .executeAllJobs(2000)
                     .assertProcessEndedAndInExclusiveEndEvent(
                     "endEvent")
                    .external(new DumpAuditTrail(activitiRule));

            // Note, this is a hard delete
            delete((String) spec.getVar("contactId"));

        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    private void delete(String contactId) {
        try {
            new RestDelete().delete(contactId, TestCredentials.BOT_USERNAME,
                    TestCredentials.BOT_PWD, null, null);
        } catch (IOException e) {
            e.printStackTrace();
            fail(e.getClass().getName() + ":" + e.getMessage());
        }
    }

}