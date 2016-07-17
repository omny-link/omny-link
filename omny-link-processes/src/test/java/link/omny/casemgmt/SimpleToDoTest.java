package link.omny.casemgmt;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

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
            "processes/link/omny/casemgmt/SimpleToDo.bpmn",
            "processes/link/omny/alerts/SendAlertNoOp.bpmn", },
            tenantId = TENANT_ID)
    public void testSimpleToDoSelfDueImmediately() throws Exception {
        new ActivitiSpec(activitiRule, "testSimpleToDoSelfDueImmediately")
                .whenEventOccurs(
                        "Task created",
                        TODO_KEY,
                        ActivitiSpec.emptySet(),
                        ActivitiSpec.buildMap(
                                ActivitiSpec.newPair("tenantId", TENANT_ID),
                                ActivitiSpec.newPair("where", LOCATION),
                                ActivitiSpec.newPair("who", USERNAME),
                                ActivitiSpec.newPair("when", "2000-01-01")),
                        TENANT_ID)
                .whenExecuteJobsForTime(2000)
                // escalation timer remains
                .thenSubProcessCalled("SendAlert")
                .thenUserTask("doSomething", ActivitiSpec.emptySet(),
                        ActivitiSpec.buildMap())
                .thenSubProcessCalled("SendAlert")
                .thenProcessEndedAndInExclusiveEndEvent("endEvent")
                .thenExtension(new DumpAuditTrail(activitiRule));
    }

    @SuppressWarnings("unchecked")
    @Test
    @org.activiti.engine.test.Deployment(resources = {
            "processes/link/omny/casemgmt/SimpleToDo.bpmn",
            "processes/link/omny/alerts/SendAlertNoOp.bpmn", }, tenantId = TENANT_ID)
    public void testSimpleToDoSelfDueTomorrow() throws Exception {
        GregorianCalendar cal = new GregorianCalendar();
        cal.add(Calendar.HOUR, 24);
        Date tomorrow = cal.getTime();
        new ActivitiSpec(activitiRule, "testSimpleToDoSelfDueTomorrow")
                .whenEventOccurs(
                        "Task created",
                        TODO_KEY,
                        ActivitiSpec.emptySet(),
                        ActivitiSpec.buildMap(
                                ActivitiSpec.newPair("tenantId", TENANT_ID),
                                ActivitiSpec.newPair("where", LOCATION),
                                ActivitiSpec.newPair("who", USERNAME),
                                ActivitiSpec.newPair("when", tomorrow)),
                        TENANT_ID)
                .whenProcessTimePassed(Calendar.HOUR, 25)
                .whenExecuteJobsForTime(5000)
                // escalation timer remains
                .thenExtension(new DumpAuditTrail(activitiRule))
                .thenSubProcessCalled("SendAlert")
                .thenUserTask("doSomething", ActivitiSpec.emptySet(),
                        ActivitiSpec.buildMap())
                .thenSubProcessCalled("SendAlert")
                .thenProcessEndedAndInExclusiveEndEvent("endEvent")
                .thenExtension(new DumpAuditTrail(activitiRule));
    }

    @SuppressWarnings("unchecked")
    @Test
    @org.activiti.engine.test.Deployment(resources = {
            "processes/link/omny/casemgmt/SimpleToDo.bpmn",
            "processes/link/omny/alerts/SendAlertNoOp.bpmn", }, tenantId = TENANT_ID)
    public void testSimpleTodoDelegateDueImmediately() throws Exception {
        new ActivitiSpec(activitiRule, "testSimpleTodoDelegateDueImmediately")
                .whenEventOccurs(
                        "Task created",
                        TODO_KEY,
                        ActivitiSpec.emptySet(),
                        ActivitiSpec.buildMap(
                                ActivitiSpec.newPair("tenantId", TENANT_ID),
                                ActivitiSpec.newPair("where", LOCATION),
                                ActivitiSpec.newPair("when", "2000-01-01"),
                                ActivitiSpec.newPair("who", null)), TENANT_ID)
                .thenUserTask(
                        "assignTask",
                        ActivitiSpec.emptySet(),
                        ActivitiSpec.buildMap(ActivitiSpec.newPair("who",
                                DELEGATE)))
                .whenExecuteJobsForTime(2000)
                // escalation timer remains
                .thenSubProcessCalled("SendAlert")
                .thenUserTask("doSomething", ActivitiSpec.emptySet(),
                        ActivitiSpec.buildMap())
                .thenSubProcessCalled("SendAlert")
                .thenProcessEndedAndInExclusiveEndEvent("endEvent")
                .thenExtension(new DumpAuditTrail(activitiRule));
    }

    @SuppressWarnings("unchecked")
    @Test
    @org.activiti.engine.test.Deployment(resources = {
            "processes/link/omny/casemgmt/SimpleToDo.bpmn",
            "processes/link/omny/alerts/SendAlertNoOp.bpmn", }, tenantId = TENANT_ID)
    public void testSimpleTodoDelegateDueImmediatelyButNeedsEscalation()
            throws Exception {
        new ActivitiSpec(activitiRule,
                "testSimpleTodoDelegateDueImmediatelyButNeedsEscalation")
                .whenEventOccurs(
                        "Task created",
                        TODO_KEY,
                        ActivitiSpec.emptySet(),
                        ActivitiSpec.buildMap(
                                ActivitiSpec.newPair("tenantId", TENANT_ID),
                                ActivitiSpec.newPair("where", LOCATION),
                                ActivitiSpec.newPair("who", USERNAME),
                                ActivitiSpec.newPair("when", "2000-01-01")),
                        TENANT_ID)
                .whenExecuteJobsForTime(2000)
                // .thenTimerExpired("dueTimer")
                .thenSubProcessCalled("SendAlert") // task arrival
                .whenProcessTimePassed(Calendar.HOUR, 25)
                .whenExecuteJobsForTime(2000)
                // .thenTimerExpired("escalateAfter24h")
                .thenSubProcessCalled("SendAlert") // task escalation
                .thenUserTask("doSomething", ActivitiSpec.emptySet(),
                        ActivitiSpec.buildMap())
                .thenSubProcessCalled("SendAlert")
                .thenProcessEndedAndInExclusiveEndEvent("endEvent")
                .thenExtension(new DumpAuditTrail(activitiRule));
    }
}