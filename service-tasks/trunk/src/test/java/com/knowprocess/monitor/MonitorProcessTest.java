package com.knowprocess.monitor;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.RuntimeService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.test.ActivitiRule;
import org.junit.Rule;
import org.junit.Test;

import com.knowprocess.test.mailserver.TestMailServer;

public class MonitorProcessTest {

	@Rule
    public TestMailServer mailServer = new TestMailServer();

    @Rule
    public ActivitiRule activitiRule = new ActivitiRule("test-activiti.cfg.xml");

    @Test
    public void testWwwTrakeo() {
        monitorSite("http://www.trakeo.com",
                "<title>trakeo - When your market is half a world away</title>");
    }

    @Test
    public void testAppTrakeo() {
        monitorSite("http://app.trakeo.com",
                "<title>Welcome to trakeo - Accurate Mass Balance Accounting</title>");
    }

    @Test
    public void testWwwKnowProcess() {
        monitorSite("http://www.knowprocess.com", "<title>Know Process</title>");
    }

    @Test
    public void testNonExistentAppKnowProcess() {
        try {
            monitorSite("http://app.knowprocess.com",
                    "<title>Know Process</title>");
        } catch (Throwable e) {
            System.out.println("Exception during monitoring, investigate...");
        }

        // Since site does not exist we should get a message that it is down.
        try {
            mailServer.dumpMailSent();
			mailServer.assertEmailSend(0, true,
					"monitor process says site is down", "",
					"donotreply@knowprocess.com", "tim@knowprocess.com", null);
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getClass().getName() + ":" + e.getMessage());
        }
    }

    private void monitorSite(String siteUrl, String expectedContent) {
        Deployment deployment = activitiRule
                .getRepositoryService()
                .createDeployment()
                .addClasspathResource(
                        "process/com/knowprocess/monitor/MonitorProcess.bpmn")
                .deploy();
        assertNotNull(deployment);
        
        Map<String, Object> variableMap = new HashMap<String, Object>();
        variableMap.put("resource", siteUrl);
        variableMap.put("expectedContent", expectedContent);

        RuntimeService runtimeService = activitiRule.getRuntimeService();
        ProcessInstance processInstance = runtimeService
                .startProcessInstanceByKey("monitorProcess", variableMap);
        assertNotNull(processInstance.getId());
        System.out.println("id " + processInstance.getId() + " "
                + processInstance.getProcessDefinitionId());

        // List<Task> tasks = activitiRule.getTaskService().createTaskQuery()
        // .list();
        // assertEquals(1, tasks.size());
        // System.out.println("task: " + tasks.get(0).getName() + "("
        // + tasks.get(0).getId() + "), assigned to: "
        // + tasks.get(0).getAssignee());

        // activitiRule.assertComplete(processInstance.getId());
    }

}