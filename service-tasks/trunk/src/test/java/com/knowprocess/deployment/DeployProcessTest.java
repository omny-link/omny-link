package com.knowprocess.deployment;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

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

        ProcessInstance mainProc = svc
                .submitDeploymentRequest(Fetcher.PROTOCOL
                        + "/process/MyProcess.bpmn");
        ProcessInstance subProc = findDeploymentInstance(mainProc);
        List<String> errors = (List<String>) activitiRule.getRuntimeService()
                .getVariable(subProc.getId(), "errors");
        assertNotNull(errors);
        for (String error : errors) {
            System.out.println(error);
        }
        assertEquals(0, errors.size());

        // TODO at this stage the process does not complete normally so cancel
        // it.
        // activitiRule.getRuntimeService().deleteProcessInstance(
        // mainProc.getId(), "Clean up test data");
    }

    private ProcessInstance findDeploymentInstance(ProcessInstance mainProc) {
        List<ProcessInstance> instances = activitiRule.getRuntimeService()
                .createProcessInstanceQuery().list();
        // the second (last) will be the sub-proc
        ProcessInstance subProc = null;
        for (ProcessInstance instance : instances) {
            System.out.println("instance: " + instance.getId() + " "
                    + instance.getProcessDefinitionId());
            if (!instance.getId().equals(mainProc.getId())) {
                subProc = instance;
            }
        }
        assertNotNull(subProc);
        return subProc;
    }

    @Test
    public void testAddGatewayDefault() {
        Authentication.setAuthenticatedUserId(INITIATOR);

        ProcessInstance mainProc = svc.submitDeploymentRequest(Fetcher.PROTOCOL
                + "/process/SetGatewayDefaultTestProcess.bpmn");

        ProcessInstance subProc = findDeploymentInstance(mainProc);
        List<String> errors = (List<String>) activitiRule.getRuntimeService()
                .getVariable(subProc.getId(), "errors");
        assertNotNull(errors);
        for (String error : errors) {
            System.out.println(error);
        }
        assertEquals(0, errors.size());
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
        ProcessInstance mainProc = svc.submitDeploymentRequest(
                Fetcher.PROTOCOL
                        + "/process/4-yaoqiang-invoice-en-collaboration.bpmn",
                vars);

        ProcessInstance subProc = findDeploymentInstance(mainProc);
        List<String> errors = (List<String>) activitiRule.getRuntimeService()
                .getVariable(subProc.getId(), "errors");
        for (String error : errors) {
            System.out.println(error);
        }
        assertEquals(11, errors.size());

        // TODO at this stage the process does not complete normally so cancel
        // it.
        activitiRule.getRuntimeService().deleteProcessInstance(
                mainProc.getId(), "Clean up test data");
    }

    @Test
    @Ignore
    public void testBPSimCarRepairProcess() throws Exception {
        svc.submitDeploymentRequest(Fetcher.PROTOCOL
                + "/process/car-repair-process-0.18.bpmn");
    }

}