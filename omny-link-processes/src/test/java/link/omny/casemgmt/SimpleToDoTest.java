package link.omny.casemgmt;

import static org.junit.Assert.fail;

import org.activiti.bdd.ActivitiSpec;
import org.activiti.bdd.ext.DumpAuditTrail;
import org.activiti.bdd.test.activiti.ExtendedRule;
import org.activiti.bdd.test.mailserver.TestMailServer;
import org.activiti.engine.IdentityService;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class SimpleToDoTest {

    private static final String MSG_NAMESPACE = "omny";

    private static final String USERNAME = "homer@springfield.com";

    private static final String DELEGATE = "marge@springfield.com";

    private static final String TENANT_ID = MSG_NAMESPACE;

    private static final String TODO_KEY = "SimpleToDo";

    private static final String LOCATION = "Swindon";

    @Rule
    public ExtendedRule activitiRule = new ExtendedRule("test-activiti.cfg.xml");

    @Rule
    public TestMailServer mailServer = new TestMailServer();

    @Before
    public void setUp() {
        IdentityService idSvc = activitiRule.getIdentityService();
        idSvc.saveUser(idSvc.newUser(USERNAME));
        idSvc.saveUser(idSvc.newUser(DELEGATE));
    }

    @After
    public void tearDown() {
        IdentityService idSvc = activitiRule.getIdentityService();
        idSvc.deleteUser(USERNAME);
        idSvc.deleteUser(DELEGATE);
    }

    @SuppressWarnings("unchecked")
    @Test
    @org.activiti.engine.test.Deployment(resources = {
            "processes/link/omny/casemgmt/SimpleToDoProcess.bpmn",
            "processes/link/omny/notifications/SendNotificationNoOp.bpmn", }, 
            tenantId = TENANT_ID)
    public void testSimpleTodoSelf() {
        try {
            new ActivitiSpec(activitiRule, "testSimpleTodoSelf")
                    .whenEventOccurs(
                            "Task created",
                            TODO_KEY,
                            ActivitiSpec.emptySet(),
                            ActivitiSpec.buildMap(
                                    ActivitiSpec.newPair("tenantId", TENANT_ID),
                                    ActivitiSpec.newPair("where", LOCATION),
                                    ActivitiSpec.newPair("who", USERNAME),
                                    ActivitiSpec.newPair("when", null)),
                            TENANT_ID)
                    .thenSubProcessCalled("SendNotification")
                    .thenUserTask("doSomething", ActivitiSpec.emptySet(),
                            ActivitiSpec.buildMap())
                    .thenSubProcessCalled("SendNotification")
                    .thenProcessEndedAndInExclusiveEndEvent("endEvent")
                    .thenExtension(new DumpAuditTrail(activitiRule));
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    @org.activiti.engine.test.Deployment(resources = {
            "processes/link/omny/casemgmt/SimpleToDoProcess.bpmn",
            "processes/link/omny/notifications/SendNotificationNoOp.bpmn", }, tenantId = TENANT_ID)
    public void testSimpleTodoDelegate() {
        try {
            new ActivitiSpec(activitiRule, "testSimpleTodoDelegate")
                    .whenEventOccurs(
                            "Task created",
                            TODO_KEY,
                            ActivitiSpec.emptySet(),
                            ActivitiSpec.buildMap(
                                    ActivitiSpec.newPair("tenantId", TENANT_ID),
                                    ActivitiSpec.newPair("where", LOCATION),
                                    ActivitiSpec.newPair("when", null),
                                    ActivitiSpec.newPair("who", null)),
                            TENANT_ID)
                    .thenUserTask("acceptTask", ActivitiSpec.emptySet(),
                            ActivitiSpec.buildMap(
                                    ActivitiSpec.newPair("who", DELEGATE)))
                    .thenSubProcessCalled("SendNotification")
                    .thenUserTask("doSomething", ActivitiSpec.emptySet(),
                            ActivitiSpec.buildMap())
                    .thenSubProcessCalled("SendNotification")
                    .thenProcessEndedAndInExclusiveEndEvent("endEvent")
                    .thenExtension(new DumpAuditTrail(activitiRule));
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }
}