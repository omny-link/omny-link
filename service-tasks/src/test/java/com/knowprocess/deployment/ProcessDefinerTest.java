package com.knowprocess.deployment;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.ServiceTask;
import org.activiti.bpmn.model.UserTask;
import org.junit.BeforeClass;
import org.junit.Test;

import com.knowprocess.bpmn.model.TaskType;
import com.knowprocess.resource.spi.Fetcher;

public class ProcessDefinerTest {

    private static File outputDir = new File("target", "processes");

    private static ProcessDefiner definer;

    @BeforeClass
    public static void setUpClass() throws Exception {
        outputDir.mkdirs();
        definer = new ProcessDefiner();
    }

    @Test
    public void testParseSimple() {
        Fetcher fetcher = new Fetcher();
        try {
            String markup = fetcher
                    .fetchToString("classpath:///processes/Simple.pd.txt");
            BpmnModel processModel = definer.parse(markup);
            assertNotNull(processModel);
            assertEquals(1, processModel.getProcesses().size());
            // Expect Start + 3 Tasks + End + sequence flows
            assertEquals(9, processModel.getProcesses().get(0)
                    .getFlowElements().size());

            assertContainsUserTask(processModel.getProcesses().get(0)
                    .getFlowElements(), "User Task 1");
            assertContainsServiceTask(processModel.getProcesses().get(0)
                    .getFlowElements(), "Mail Task 2");
            assertContainsUserTask(processModel.getProcesses().get(0)
                    .getFlowElements(), "User Task 3");
            byte[] bytes = definer.convertToBpmn(processModel, "UTF-8");
            FileOutputStream stream = null;
            try {
                File bpmnFile = new File(outputDir, "Simple.bpmn");
                stream = new FileOutputStream(bpmnFile);
                stream.write(bytes);
                assertTrue(bpmnFile.exists());
            } finally {
                stream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
            fail(String.format("%1$s: %2$s", e.getClass().getName(),
                    e.getMessage()));
        }
    }

    private void assertContainsUserTask(Collection<FlowElement> flowElements,
            String name) {
        for (FlowElement el : flowElements) {
            if (el instanceof UserTask && el.getName().equals(name)) {
                return;
            }
        }
        fail(String.format("Unable to find user task named %1$s", name));
    }

    private void assertContainsServiceTask(
            Collection<FlowElement> flowElements, String name) {
        for (FlowElement el : flowElements) {
            if (el instanceof ServiceTask && el.getName().equals(name)) {
                return;
            }
        }
        fail(String.format("Unable to find service task named %1$s", name));
    }

    @Test
    public void testParseBusinessRuleTaskType() {
        assertEquals(TaskType.BUSINESS_RULE_TASK,
                definer.getTaskType("1. :decision Make decision"));
    }

    @Test
    public void testParseReceiveTaskType() {
        assertEquals(TaskType.RECEIVE_TASK,
                definer.getTaskType("1. :receive Wait for message"));
    }

    @Test
    public void testParseScriptTaskType() {
        assertEquals(TaskType.SCRIPT_TASK,
                definer.getTaskType("1. :script Run this script"));
        assertEquals(TaskType.SCRIPT_TASK,
                definer.getTaskType("1. :javascript Run this script"));
    }

    @Test
    public void testParseSendTaskType() {
        assertEquals(TaskType.SEND_TASK,
                definer.getTaskType("1. :send Send a message"));
    }

    @Test
    public void testParseServiceTaskType() {
        assertEquals(TaskType.SERVICE_TASK,
                definer.getTaskType("1. :mail Invoke service"));
    }

    @Test
    public void testParseUnspecifiedTaskType() {
        assertEquals(TaskType.TASK, definer.getTaskType("1. Do something"));
    }

    @Test
    public void testParseUserTaskType() {
        assertEquals(TaskType.USER_TASK,
                definer.getTaskType("1. +initiator Do something"));
    }
}
