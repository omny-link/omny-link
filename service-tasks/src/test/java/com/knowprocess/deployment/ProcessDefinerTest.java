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
import org.activiti.bpmn.model.SubProcess;
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
            String markdown = fetcher
                    .fetchToString("classpath:///processes/Simple.pd.txt");
            BpmnModel processModel = definer.parse(markdown, "Simple");
            assertNotNull(processModel);
            assertEquals(1, processModel.getProcesses().size());
            // Expect Start + 3 Tasks + End + sequence flows
            assertEquals(9, processModel.getProcesses().get(0)
                    .getFlowElements().size());

            assertContainsUserTaskNamed(processModel
                    .getProcesses().get(0).getFlowElements(), "User Task 1");
            assertContainsServiceTaskNamed(processModel.getProcesses().get(0)
                    .getFlowElements(), "Mail Task 2");
            UserTask userTask3 = assertContainsUserTaskNamed(processModel
                    .getProcesses().get(0)
                    .getFlowElements(), "User Task 3");
            assertTrue(userTask3.getCandidateUsers().contains(
                    "tim@knowprocess.com"));
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

    @Test
    public void testParseProcessIncSubTasks() {
        Fetcher fetcher = new Fetcher();
        BpmnModel processModel = null;
        try {
            String markdown = fetcher
                    .fetchToString("classpath:///processes/Plan.pd.txt");
            processModel = definer.parse(markdown, "Plan");
            assertNotNull(processModel);
            assertEquals(1, processModel.getProcesses().size());
            processModel.getProcesses().get(0).setName("Plan");

            // Expect Start + 3 Tasks + End + sequence flows
            // assertEquals(11, processModel.getProcesses().get(0)
            // .getFlowElements().size());

            assertContainsUserTaskNamed(processModel.getProcesses().get(0)
                    .getFlowElements(), "User Task 1");
            assertContainsUserTaskNamed(processModel.getProcesses().get(0)
                    .getFlowElements(), "User Task 2");
            assertContainsSubProcessNamed(processModel.getProcesses().get(0)
                    .getFlowElements(), "Sub-Process");
            assertContainsUserTaskNamed(processModel.getProcesses().get(0)
                    .getFlowElements(), "User Task 3");

        } catch (IOException e) {
            e.printStackTrace();
            fail(String.format("%1$s: %2$s", e.getClass().getName(),
                    e.getMessage()));
        } finally {
            FileOutputStream stream = null;
            try {
                byte[] bytes = definer.convertToBpmn(processModel, "UTF-8");
                File bpmnFile = new File(outputDir, "Plan.bpmn");
                stream = new FileOutputStream(bpmnFile);
                stream.write(bytes);
                assertTrue(bpmnFile.exists());
            } catch (IOException e) {
                e.printStackTrace();
                fail(String.format("%1$s:%2$s", e.getClass().getName(),
                        e.getMessage()));
            } finally {
                try {
                    stream.close();
                } catch (IOException e) {
                    ;
                }
            }
        }
    }

    private UserTask assertContainsUserTaskNamed(
            Collection<FlowElement> flowElements, String name) {
        for (FlowElement el : flowElements) {
            if (el instanceof UserTask && el.getName().equals(name)) {
                return (UserTask) el;
            }
        }
        fail(String.format("Unable to find user task named %1$s", name));
        return null;
    }

    private ServiceTask assertContainsServiceTaskNamed(
            Collection<FlowElement> flowElements, String name) {
        for (FlowElement el : flowElements) {
            if (el instanceof ServiceTask && el.getName().equals(name)) {
                return (ServiceTask)el;
            }
        }
        fail(String.format("Unable to find service task named %1$s", name));
        return null;
    }

    private SubProcess assertContainsSubProcessNamed(
            Collection<FlowElement> flowElements, String name) {
        for (FlowElement el : flowElements) {
            if (el instanceof SubProcess && el.getName().equals(name)) {
                return (SubProcess) el;
            }
        }
        fail(String.format("Unable to find sub-process named %1$s", name));
        return null;
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

    // TODO neither data objects nor activiti:class getting written to BPMN
    @Test
    public void testParseData() {
        Fetcher fetcher = new Fetcher();
        try {
            String markdown = fetcher
                    .fetchToString("classpath:///processes/Data.pd.txt");
            BpmnModel processModel = definer.parse(markdown, "Data");
            assertNotNull(processModel);
            assertEquals(1, processModel.getProcesses().size());
            // Expect Start + 3 Tasks + End + sequence flows
            assertEquals(9, processModel.getProcesses().get(0)
                    .getFlowElements().size());
            byte[] bytes = definer.convertToBpmn(processModel, "UTF-8");
            FileOutputStream stream = null;
            try {
                File bpmnFile = new File(outputDir, "Data.bpmn");
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
}
