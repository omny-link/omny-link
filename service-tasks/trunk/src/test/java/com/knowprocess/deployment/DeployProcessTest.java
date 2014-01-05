package com.knowprocess.deployment;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.HashMap;
import java.util.List;

import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.ActivitiOptimisticLockingException;
import org.activiti.engine.form.FormProperty;
import org.activiti.engine.form.TaskFormData;
import org.activiti.engine.history.HistoricVariableInstance;
import org.activiti.engine.impl.identity.Authentication;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.runtime.ProcessInstance;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import com.knowprocess.resource.spi.Fetcher;
import com.knowprocess.test.ExtendedRule;

public class DeployProcessTest {

    private static final String INITIATOR = "tim@knowprocess.com";

    @Rule
	public ExtendedRule activitiRule = new ExtendedRule("test-activiti.cfg.xml");

    private DeploymentService svc;

    @Before
    public void setUp() {
        svc = new DeploymentService(activitiRule.getProcessEngine());
		svc.commission();
    }

	@After
	public void tearDown() {
		List<Deployment> defs = activitiRule.getRepositoryService()
				.createDeploymentQuery().list();
		for (Deployment deployment : defs) {
			try {
				activitiRule.getRepositoryService().deleteDeployment(
						deployment.getId(), true);
			} catch (ActivitiObjectNotFoundException e) {
				System.err.println("Unable to complete tear down:"
						+ e.getMessage());
			} catch (ActivitiOptimisticLockingException e) {
				System.err.println("Unable to complete tear down:"
						+ e.getMessage());
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} finally {
					// try again
					tearDown();
				}
			}
		}
	}

    @Test
    public void testSuccessfulDeploymentRequest() throws Exception {
        Authentication.setAuthenticatedUserId(INITIATOR);

        ProcessInstance mainProc = svc
                .submitDeploymentRequest(Fetcher.PROTOCOL
				+ "/process/MyProcess.bpmn", true);
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
				+ "/process/SetGatewayDefaultTestProcess.bpmn", true);

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

		// Start process ...
		ProcessInstance processInstance = svc.submitDeploymentRequest(
				Fetcher.PROTOCOL + "/process/activities.bpmn", true);

		// ... assert that it flows to the 'fixProcess' task with 4 errors
		// and no messages
		String taskId = activitiRule.assertTaskExists("Fix process", INITIATOR,
				true);
		List<String> errors = (List<String>) activitiRule.getTaskService()
				.getVariable(taskId, "errors");
		for (String error : errors) {
			System.out.println(error);
		}
		assertEquals(4, errors.size());
		List<String> messages = (List<String>) activitiRule.getTaskService()
				.getVariable(taskId, "messages");
		for (String msg : messages) {
			System.out.println(msg);
		}
		assertEquals(0, messages.size());

		// ... ensure that we have the errors and messages vars in the form
		TaskFormData data = activitiRule.getFormService().getTaskFormData(
				taskId);
		for (FormProperty prop : data.getFormProperties()) {
			System.out.println(prop.getName() + ":" + prop.getValue());
		}
		assertEquals(2, data.getFormProperties().size());

		List<HistoricVariableInstance> list = activitiRule.getHistoryService()
				.createHistoricVariableInstanceQuery()
				.processInstanceId(processInstance.getId())
				.list();
		for (HistoricVariableInstance historicVariableInstance : list) {
			System.out.println("Audit trail has var: "
					+ historicVariableInstance.getVariableName() + " = "
					+ historicVariableInstance.getValue());
		}

		activitiRule.dumpProcessState(processInstance.getId());

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
		ProcessInstance mainProc = svc.submitDeploymentRequest(Fetcher.PROTOCOL
				+ "/process/4-yaoqiang-invoice-en-collaboration.bpmn", vars,
				true);

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
				+ "/process/car-repair-process-0.18.bpmn", true);
    }

}