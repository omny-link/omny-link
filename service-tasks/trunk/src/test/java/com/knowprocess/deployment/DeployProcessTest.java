package com.knowprocess.deployment;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.ActivitiRule;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import com.knowprocess.resource.spi.Fetcher;

public class DeployProcessTest {

    @Rule
    public ActivitiRule activitiRule = new ActivitiRule();

    @Test
    public void testSimpleDiscoveryAcceleratorProcess() throws Exception {
        deployDeploymentProcess("process/com/knowprocess/deployment/DeploymentProcess.bpmn");
        deployAndRun(Fetcher.PROTOCOL + "/process/activities.bpmn");
    }

    @Test
    @Ignore
    public void testBPSimCarRepairProcess() throws Exception {
        deployDeploymentProcess("process/com/knowprocess/deployment/DeploymentProcess.bpmn");
        deployAndRun(Fetcher.PROTOCOL + "/process/car-repair-process-0.18.bpmn");
    }

    private void deployAndRun(String resource) {
        Map<String, Object> variableMap = new HashMap<String, Object>();
        // variableMap.put("resource", Fetcher.PROTOCOL + "/" + resource);
        variableMap.put("resource", resource);

        RuntimeService runtimeService = activitiRule.getRuntimeService();
        ProcessInstance processInstance = runtimeService
                .startProcessInstanceByKey("deploymentProcess", variableMap);
        assertNotNull(processInstance.getId());
        System.out.println("id " + processInstance.getId() + " "
                + processInstance.getProcessDefinitionId());

        List<Task> tasks = activitiRule.getTaskService().createTaskQuery()
                .list();
        assertEquals(1, tasks.size());
        System.out.println("task: " + tasks.get(0).getName() + "("
                + tasks.get(0).getId() + "), assigned to: "
                + tasks.get(0).getAssignee());
    }

    private void deployDeploymentProcess(String resource) {
        RepositoryService repositoryService = activitiRule
                .getRepositoryService();
        Deployment deployment = repositoryService.createDeployment()
                .addClasspathResource(resource).deploy();
        System.out.println("deployment returned: " + deployment);

        List<Deployment> deployments = activitiRule.getRepositoryService()
                .createDeploymentQuery().list();
        for (Deployment d : deployments) {
            System.out.println("deployment from search: " + d.getName() + "("
                    + d.getId() + ")");
        }
    }
}