package link.omny.website;

import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.Collections;

import org.activiti.bdd.ActivitiSpec;
import org.activiti.bdd.ext.DumpAuditTrail;
import org.activiti.bdd.test.activiti.ExtendedRule;
import org.activiti.bdd.test.mailserver.TestMailServer;
import org.activiti.engine.ActivitiException;
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

    private String contactId;

    @Before
    public void setUp() {
        IdentityService idSvc = activitiRule.getIdentityService();
        idSvc.saveUser(idSvc.newUser(USERNAME));

        TestCredentials.initBot(idSvc, TENANT_ID);
    }

    @After
    public void tearDown() {

        // Note, this is a hard delete
        try {
            delete(contactId);
        } catch (Throwable e) {
            System.err.println(e.getMessage());
        }

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
            "processes/link/omny/mail/SelectDefaultEnquiryResponse.bpmn",
            "processes/link/omny/mail/SendMemo.bpmn",
            "processes/link/omny/notifications/SendNotificationMemo.bpmn" }, tenantId = TENANT_ID)
    public void testEnquiryFromNewContact() {
        try {
            ActivitiSpec spec = new ActivitiSpec(activitiRule,
                    "testEnquiryFromNewContact")
                    .whenMsgReceived("", ENQUIRY_MSG, "/omny.enquiry.json",
                            TENANT_ID)
                    .whenExecuteJobsForTime(10000)
                    .collectVar("contactId")
                    .thenSubProcessCalled("CreateContactAndAccount")
                    .thenSubProcessCalled("AddActivityToContact")
                    .thenSubProcessCalled("SendNotification")
                    .thenSubProcessCalled("SendMemo")
                    .thenProcessEndedAndInEndEvents("endInternal",
                            "endExternal")
                    .thenExtension(new DumpAuditTrail(activitiRule));

            setContactId(spec);

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
            "processes/link/omny/mail/SelectDefaultEnquiryResponse.bpmn",
            "processes/link/omny/mail/SendMemo.bpmn",
            "processes/link/omny/notifications/SendNotificationNoOp.bpmn" }, tenantId = TENANT_ID)
    public void testEnquiryFromNewContactNoMessage() {
        try {
            ActivitiSpec spec = new ActivitiSpec(activitiRule,
                    "testEnquiryFromNewContact")
                    .whenMsgReceived("", ENQUIRY_MSG,
                            "/omny.enquiry-no-msg.json", TENANT_ID)
                    .whenExecuteJobsForTime(5000).collectVar("contactId")
                    .thenProcessEndedAndInEndEvents("endExternal",
                            "endInternal")
                    .thenExtension(new DumpAuditTrail(activitiRule));

            setContactId(spec);
            fail();
        } catch (ActivitiException e) {
            System.err.println("Unfortunately message is mandatory for now.");
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    private void setContactId(ActivitiSpec spec) {
        if (((String) spec.getVar("contactId")).startsWith("http")) {
            contactId = (String) spec.getVar("contactId");
        } else {
            contactId = BASE_URI + spec.getVar("contactId");
        }
    }

    @Test
    @org.activiti.engine.test.Deployment(resources = {
            "processes/link/omny/website/RecordEnquiry.bpmn",
            "processes/link/omny/custmgmt/CreateContactAndAccount.bpmn",
            "processes/link/omny/custmgmt/AddActivityToContact.bpmn",
            "processes/link/omny/custmgmt/AddNoteToContact.bpmn",
            "processes/link/omny/mail/SelectDefaultEnquiryResponse.bpmn",
            "processes/link/omny/mail/SendMemo.bpmn",
            "processes/link/omny/notifications/SendNotificationNoOp.bpmn" }, tenantId = TENANT_ID)
    public void testEnquiryFromExistingContact() {
        try {
            preCreateContact();

            ActivitiSpec spec = new ActivitiSpec(activitiRule,
                    "testEnquiryFromNewContact")
                    .whenMsgReceived("", ENQUIRY_MSG, "/omny.enquiry.json",
                            TENANT_ID).whenExecuteJobsForTime(5000)
                    .collectVar("contactId")
                    .thenProcessEndedAndInEndEvents("endInternal",
                            "endExternal")
                    .thenExtension(new DumpAuditTrail(activitiRule));

            setContactId(spec);

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
                            "{\"firstName\",\"Homer\",\"lastName\":\"Simpson\",\"email\":\"homer@springfieldpower.com\"}");
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