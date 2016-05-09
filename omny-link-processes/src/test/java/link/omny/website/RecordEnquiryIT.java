package link.omny.website;

import static org.junit.Assert.fail;

import java.io.IOException;
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

import com.knowprocess.resource.spi.RestDelete;
import com.knowprocess.resource.spi.RestPost;

public class RecordEnquiryIT {

    private static final String MSG_NAMESPACE = "omny";

    private static final String USERNAME = "tim";

    private static final String TENANT_ID = MSG_NAMESPACE;

    private static final String ENQUIRY_MSG = MSG_NAMESPACE + ".enquiry";

    private static final String BASE_URI = "http://localhost:8082";

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
            "processes/link/omny/website/RecordEnquiry.bpmn",
            "processes/link/omny/custmgmt/CreateContactAndAccount.bpmn",
            "processes/link/omny/custmgmt/AddActivityToContact.bpmn",
            "processes/link/omny/custmgmt/AddNoteToContact.bpmn",
            "processes/link/omny/mail/SendMemo.bpmn" }, tenantId = TENANT_ID)
    public void testEnquiryFromNewContact() {
        try {
            ActivitiSpec spec = new ActivitiSpec(activitiRule,
                    "testEnquiryFromNewContact")
                    .whenMsgReceived("", ENQUIRY_MSG, "/omny.enquiry.json",
                            TENANT_ID).whenExecuteJobsForTime(5000)
                    .collectVar("contactId")
                    .thenProcessEndedAndInExclusiveEndEvent("endEvent")
                    .thenExtension(new DumpAuditTrail(activitiRule));

            // Note, this is a hard delete
            delete(BASE_URI + spec.getVar("contactId"));

        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    @org.activiti.engine.test.Deployment(resources = {
            "processes/link/omny/website/RecordEnquiry.bpmn",
            "processes/link/omny/custmgmt/CreateContactAndAccount.bpmn",
            "processes/link/omny/custmgmt/AddActivityToContact.bpmn",
            "processes/link/omny/custmgmt/AddNoteToContact.bpmn",
            "processes/link/omny/mail/SendMemo.bpmn" }, tenantId = TENANT_ID)
    public void testEnquiryFromExistingContact() {
        try {
            preCreateContact();

            ActivitiSpec spec = new ActivitiSpec(activitiRule,
                    "testEnquiryFromNewContact")
                    .whenMsgReceived("", ENQUIRY_MSG, "/omny.enquiry.json",
                            TENANT_ID).whenExecuteJobsForTime(5000)
                    .collectVar("contactId")
                    .thenProcessEndedAndInExclusiveEndEvent("endEvent")
                    .thenExtension(new DumpAuditTrail(activitiRule));

            // Note, this is a hard delete
            delete(BASE_URI + spec.getVar("contactId"));

        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    private void preCreateContact() {
        try {
            new RestPost().post(TestCredentials.BOT_USERNAME,
                    TestCredentials.BOT_PWD, BASE_URI + "/contacts",
                    Collections
                            .singletonMap("Content-Type", "application/json"),
                            new String[0],
                            "{\"firstName\",\"Montgomery Charles\",\"lastName\":\"Burns\",\"email\":\"monty@springfieldpower.com\"}");
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getClass().getName() + ":" + e.getMessage());
        }
    }

    private void delete(String contactId) {
        try {
            new RestDelete().delete(contactId, TestCredentials.BOT_USERNAME,
                    TestCredentials.BOT_PWD, "Content-Type:application/json",
                    null);
        } catch (IOException e) {
            e.printStackTrace();
            fail(e.getClass().getName() + ":" + e.getMessage());
        }
    }

}