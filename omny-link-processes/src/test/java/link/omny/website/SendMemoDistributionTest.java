package link.omny.website;

import static org.junit.Assert.fail;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.activiti.bdd.ActivitiSpec;
import org.activiti.engine.IdentityService;
import org.activiti.engine.identity.User;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.knowprocess.bpm.test.actions.DumpAuditTrail;
import com.knowprocess.test.activiti.ExtendedRule;
import com.knowprocess.test.mailserver.TestMailServer;

public class SendMemoDistributionTest {
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
    public void testMemoDistribution() {
        try {
            Set<String> collectVars = new HashSet<String>();
            Map<String, Object> putVars = new HashMap<String, Object>();
            putVars.put("tenantId", TENANT_ID);
            putVars.put("distributionId",
                    "http://localhost:8082/memo-distributions/1");
            new ActivitiSpec(activitiRule,
                    "testMemoDistribution")
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