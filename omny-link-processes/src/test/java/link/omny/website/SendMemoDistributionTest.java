package link.omny.website;

import static org.junit.Assert.fail;

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

    private static final String USER_EMAIL = "tim@trakeo.com";

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
 "processes/link/omny/mail/DistributeMemo.bpmn" }, tenantId = TENANT_ID)
    public void testMemoDistribution() {
        try {
            Set<String> collectVars = new HashSet<String>();
            Map<String, Object> putVars = new HashMap<String, Object>();
            putVars.put("tenantId", TENANT_ID);
            putVars.put("distributionId", 1);
            new ActivitiSpec(activitiRule,
                    "testMinimalRegistration")
                    .startByKey(DISTRIBUTE_MEMO_KEY, collectVars, putVars,
                            TENANT_ID)
                    .executeJobsForTime(3000)
                    .assertProcessEnded()
                    .external(new DumpAuditTrail(activitiRule));

        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

}