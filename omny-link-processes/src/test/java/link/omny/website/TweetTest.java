package link.omny.website;

import static org.junit.Assert.fail;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.activiti.bdd.ActivitiSpec;
import org.activiti.engine.IdentityService;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.knowprocess.bpm.test.actions.DumpAuditTrail;
import com.knowprocess.test.activiti.ExtendedRule;
import com.knowprocess.test.mailserver.TestMailServer;

public class TweetTest {

    private static final String USERNAME = "tim";

    private static final String TENANT_ID = "omny";

    private static final String SEND_TWEET_KEY = "SendTweet";

    @Rule
    public ExtendedRule activitiRule = new ExtendedRule("test-activiti.cfg.xml");

    @Rule
    public TestMailServer mailServer = new TestMailServer();

    @Before
    public void setUp() {
        IdentityService idSvc = activitiRule.getIdentityService();
        idSvc.saveUser(idSvc.newUser(USERNAME));

        TestCredentials.initBotWithTwitterIntegration(idSvc, TENANT_ID);
    }

    @After
    public void tearDown() {
        IdentityService idSvc = activitiRule.getIdentityService();
        idSvc.deleteUser(USERNAME);

        TestCredentials.removeBot(idSvc, TENANT_ID);
    }

    @Test
    @org.activiti.engine.test.Deployment(resources = {
 "processes/link/omny/twitter/SendTweet.bpmn" }, tenantId = TENANT_ID)
    public void testSendTweet() {
        try {
            Set<String> collectVars = new HashSet<String>();
            Map<String, Object> putVars = new HashMap<String, Object>();
            putVars.put("tweet", "Hello! The time is " + new Date());
            new ActivitiSpec(activitiRule, "testSendTweet")
                    .whenEventOccurs("", SEND_TWEET_KEY, collectVars, putVars, TENANT_ID)
                    .whenExecuteJobsForTime(5000)
                    .thenProcessEndedAndInExclusiveEndEvent("endEvent")
                    .thenExtension(new DumpAuditTrail(activitiRule));
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

}