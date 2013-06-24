package com.knowprocess.deployment;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.List;

import org.activiti.engine.impl.identity.Authentication;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.test.ActivitiRule;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import com.knowprocess.resource.spi.Fetcher;

public class DeployProcessTest {

    private static final String INITIATOR = "tim@knowprocess.com";
    private static final int DEFAULT_PRIORITY = 50;
    @Rule
    public ActivitiRule activitiRule = new ActivitiRule("test-activiti.cfg.xml");

    private DeploymentService svc;

    @Before
    public void setUp() {
        svc = new DeploymentService(activitiRule.getProcessEngine());
    }

    @Test
    public void testSuccessfulDeploymentRequest() throws Exception {
        Authentication.setAuthenticatedUserId(INITIATOR);

        ProcessInstance processInstance = svc
                .submitDeploymentRequest(Fetcher.PROTOCOL
                        + "/process/MyProcess.bpmn");

        List<String> errors = (List<String>) activitiRule.getRuntimeService()
                .getVariable(processInstance.getId(), "errors");
        for (String error : errors) {
            System.out.println(error);
        }
        assertEquals(0, errors.size());

        // TODO at this stage the process does not complete normally so cancel
        // it.
        activitiRule.getRuntimeService().deleteProcessInstance(
                processInstance.getId(), "Clean up test data");
    }

    @Test
    public void testSimpleDiscoveryAcceleratorProcess() throws Exception {
        Authentication.setAuthenticatedUserId(INITIATOR);

        ProcessInstance processInstance = svc
                .submitDeploymentRequest(Fetcher.PROTOCOL
                        + "/process/activities.bpmn");
        // activitiRule.assertAssignedTaskExists("Fix process", INITIATOR,
        // DEFAULT_PRIORITY);

        // TODO at this stage the process does not complete normally so cancel
        // it.
        activitiRule.getRuntimeService().deleteProcessInstance(
                processInstance.getId(), "Clean up test data");

        Authentication.setAuthenticatedUserId(null);
    }

    @Test
    public void testMiwgInvoiceDemoProcess() throws Exception {
        Authentication.setAuthenticatedUserId(INITIATOR);

        HashMap<String, Object> vars = new HashMap<String, Object>();
        vars.put("processParticipantToExecute",
                "Process Engine - Invoice Receipt");
        ProcessInstance processInstance = svc.submitDeploymentRequest(
                Fetcher.PROTOCOL
                        + "/process/4-yaoqiang-invoice-en-collaboration.bpmn",
                vars);

        // Task task = activitiRule.assertAssignedTaskExists("Fix process",
        // INITIATOR, DEFAULT_PRIORITY);
        // System.out.println("task: " + task.getName() + "(" + task.getId()
        // + "), assigned to: " + task.getAssignee());

        List<String> errors = (List<String>) activitiRule.getRuntimeService()
                .getVariable(processInstance.getId(), "errors");
        for (String error : errors) {
            System.out.println(error);
        }
        assertEquals(2, errors.size());

        // TODO at this stage the process does not complete normally so cancel
        // it.
        activitiRule.getRuntimeService().deleteProcessInstance(
                processInstance.getId(), "Clean up test data");
    }

    @Test
    @Ignore
    public void testBPSimCarRepairProcess() throws Exception {
        svc.submitDeploymentRequest(Fetcher.PROTOCOL
                + "/process/car-repair-process-0.18.bpmn");
    }

}