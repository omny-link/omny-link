package link.omny.support;

import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import link.omny.website.TestCredentials;

import org.activiti.bdd.ActivitiSpec;
import org.activiti.bdd.ext.DumpAuditTrail;
import org.activiti.bdd.test.activiti.ExtendedRule;
import org.activiti.bdd.test.mailserver.TestMailServer;
import org.activiti.engine.IdentityService;
import org.activiti.engine.identity.User;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.knowprocess.resource.spi.RestDelete;
import com.knowprocess.resource.spi.RestPost;

public class ResetPasswordIT {

    private static final String MSG_NAMESPACE = "omny";

    private static final String USERNAME = "tim";

    private static final String USER_EMAIL = "tim@omny.link";

    private static final String TENANT_ID = MSG_NAMESPACE;

    private static final String MSG_RESET = MSG_NAMESPACE
            + ".passwordResetRequest";

    private static final String MSG_NEW_PASSWORD = MSG_NAMESPACE
            + ".newPassword";

    private static final String BASE_URI = "http://localhost:8082";

    private static final String NEW_PWD = "new pwd";

    @Rule
    public ExtendedRule activitiRule = new ExtendedRule("test-activiti.cfg.xml");

    @Rule
    public TestMailServer mailServer = new TestMailServer();

    private String contactId;

    @Before
    public void setUp() {
        IdentityService idSvc = activitiRule.getIdentityService();
        User newUser = idSvc.newUser(USERNAME);
        newUser.setEmail(USER_EMAIL);
        idSvc.saveUser(newUser);

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
            "processes/link/omny/support/ResetPassword.bpmn",
            "processes/link/omny/mail/SendMemo.bpmn" }, tenantId = TENANT_ID)
    public void testResetPasswordOfNonExistantUser() {
        try {
            new ActivitiSpec(activitiRule, "testResetPasswordOfNonExistantUser")
                    .whenMsgReceived("Password reset request", MSG_RESET,
                            "/omny.passwordResetRequest.json", TENANT_ID)
                    .whenExecuteJobsForTime(2000)
                    .thenProcessEndedAndInExclusiveEndEvent("endUnknownUser")
                    .thenExtension(new DumpAuditTrail(activitiRule));
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    @org.activiti.engine.test.Deployment(resources = {
            "processes/link/omny/support/ResetPassword.bpmn",
            "processes/link/omny/custmgmt/AddActivityToContact.bpmn",
            "processes/link/omny/mail/SendMemo.bpmn" }, tenantId = TENANT_ID)
    public void testResetPasswordSuccessfully() {
        try {
            createContact();
            String json = String.format(
                    "{\"password\":\"%1$s\", \"password2\":\"%2$s\",\"tenantId\":\"%3$s\"}",
                    NEW_PWD, NEW_PWD, TENANT_ID);

            ActivitiSpec spec = new ActivitiSpec(activitiRule,
                    "testResetPasswordSuccessfully")
                    .whenMsgReceived("Password reset request", MSG_RESET,
                            "/omny.passwordResetRequest.knownUser.json",
                            TENANT_ID)
                    .collectVar("uuid")
                    .thenSubProcessCalled("SendMemo")
                    .collectVar("contactId")
                    .thenExtension(new DumpAuditTrail(activitiRule))
                    .whenExecuteJobsForTime(5000)
                    .whenFollowUpMsgReceived("User sets new password",
                            MSG_NEW_PASSWORD, json, TENANT_ID)
                    // .thenProcessEndedAndInExclusiveEndEvent("endEvent")
                    .thenExtension(new DumpAuditTrail(activitiRule));

            System.out.println("  UUID: " + spec.getVar("uuid"));

            setContactId(spec);

        } catch (Exception e) {
            e.printStackTrace();
            fail();
        } finally {
            deleteContact();
        }
    }

    private void setContactId(ActivitiSpec spec) {
        if (((String) spec.getVar("contactId")).startsWith("http")) {
            contactId = (String) spec.getVar("contactId");
        } else {
            contactId = BASE_URI + spec.getVar("contactId");
        }
    }

    private void createContact() throws Exception {
        Map<String, String> requestHeaders = new HashMap<String, String>();
        requestHeaders.put("Content-Type", "application/json");
        String payload = String
                .format("{\"firstName\":\"%1$s\", \"lastName\":\"Stephenson\",\"email\":\"$2$s\",\"tenantId\":\"$3$s\"}",
                        USERNAME, USER_EMAIL, TENANT_ID);

        new RestPost().post(TestCredentials.BOT_USERNAME,
                TestCredentials.BOT_PWD, BASE_URI + "/contacts/",
                requestHeaders, new String[0], payload);

    }

    private void deleteContact() {
        try {
            new RestDelete().delete(contactId, TestCredentials.BOT_USERNAME,
                    TestCredentials.BOT_PWD, "Content-Type:application/json",
                    null);
        } catch (IOException e) {
            e.printStackTrace();
            fail();
        }
    }
}