package com.knowprocess.deployment;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Collection;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.CallActivity;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.ScriptTask;
import org.activiti.bpmn.model.ServiceTask;
import org.activiti.bpmn.model.SubProcess;
import org.activiti.bpmn.model.UserTask;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.knowprocess.bpmn.model.TaskSubType;
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
            writeToFile(processModel, "Simple.bpmn");
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

            writeToFile(processModel, "Plan.bpmn");
        } catch (Exception e) {
            e.printStackTrace();
            fail(String.format("%1$s: %2$s", e.getClass().getName(),
                    e.getMessage()));
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

    private CallActivity assertContainsCallActivityNamed(
            Collection<FlowElement> flowElements, String name) {
        for (FlowElement el : flowElements) {
            if (el instanceof CallActivity && el.getName().equals(name)) {
                return (CallActivity) el;
            }
        }
        fail(String.format("Unable to find call activity named %1$s", name));
        return null;
    }

    private ScriptTask assertContainsScriptTaskNamed(
            Collection<FlowElement> flowElements, String name) {
        for (FlowElement el : flowElements) {
            if (el instanceof ScriptTask && el.getName().equals(name)) {
                return (ScriptTask) el;
            }
        }
        fail(String.format("Unable to find script task named %1$s", name));
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
    public void testParseOrderedServiceMarkdown() {
        ActivityModel act = definer.parseLine("1. :service Do stuff // This is a comment");
        assertEquals(TaskSubType.SERVICE, act.subType);
        assertEquals("Do stuff", act.name);
        assertEquals("This is a comment", act.doc);

        act = definer.parseLine("1. :service +system Create user in WordPress // Role MUST be subscriber");
        assertEquals(TaskSubType.SERVICE, act.subType);
        assertEquals("Create user in WordPress", act.name);
        assertEquals("system", act.actor);
        assertEquals("Role MUST be subscriber", act.doc);
    }

    @Test
    public void testParseOrderedCallActivityMarkdown() {
        ActivityModel act = definer.parseLine("4. :UpdateOrder Save order");
        assertEquals(TaskSubType.CALL_ACTIVITY, act.subType);
        assertEquals("Save order", act.name);
        assertEquals("UpdateOrder", act.actor);
    }

    @Test
    public void testParseOrderedBusinessRuleMarkdown() {
        ActivityModel act = definer.parseLine("1. :decision Make decision");
        assertEquals(TaskSubType.BUSINESS_RULE, act.subType);
        assertEquals("Make decision", act.name);
    }

    @Test
    public void testParseOrderedReceiveMarkdown() {
        ActivityModel act = definer.parseLine("1. :receive Wait for message");
        assertEquals(TaskSubType.RECEIVE, act.subType);
        assertEquals("Wait for message", act.name);
    }

    @Test
    public void testParseOrderedScriptMarkdown() {
        ActivityModel act = definer.parseLine("1. :script Run this script");
        assertEquals(TaskSubType.SCRIPT, act.subType);
        act = definer.parseLine("1. :javascript Run this script");
        assertEquals(TaskSubType.SCRIPT, act.subType);
    }

    @Test
    public void testParseOrderedSendMarkdown() {
        ActivityModel act = definer.parseLine("1. :send Send a message");
        assertEquals(TaskSubType.SEND, act.subType);
    }

    @Test
    public void testParseOrderedUnspecifiedMarkdown() {
        ActivityModel act = definer.parseLine("1. Do something");
        assertEquals(TaskSubType.LOG, act.subType);
    }

    @Test
    public void testParseOrderedUserMarkdown() {
        ActivityModel act = definer.parseLine("1. +initiator Do something");
        assertEquals(TaskSubType.USER, act.subType);
        act = definer.parseLine("1. +initiator User Task 1");
    }

    @Test
    public void testWrapLines() {
        assertEquals("Do something and then\nsomething else and lastly\ndo the other",
                definer.wrapLines("Do something and then something else and lastly do the other"));
    }

    @Ignore // TODO neither data objects nor activiti:class getting written to BPMN
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
            writeToFile(processModel, "Data.bpmn");
        } catch (IOException e) {
            e.printStackTrace();
            fail(String.format("%1$s: %2$s", e.getClass().getName(),
                    e.getMessage()));
        }
    }

    @Test
    public void testParseOrchestration() {
        Fetcher fetcher = new Fetcher();
        try {
            String markdown = fetcher
                    .fetchToString("classpath:///processes/Orchestration.pd.txt");
            BpmnModel processModel = definer.parse(markdown, "Orchestration");
            assertNotNull(processModel);
            assertEquals(1, processModel.getProcesses().size());
            // Expect Start + 4 Tasks + End + sequence flows
            assertEquals(11, processModel.getProcesses().get(0)
                    .getFlowElements().size());

            assertContainsServiceTaskNamed(processModel.getProcesses().get(0)
                    .getFlowElements(), "Create user in WordPress.");
            assertContainsCallActivityNamed(processModel.getProcesses().get(0)
                    .getFlowElements(), "Send contact an email\nusing the template above");
            assertContainsScriptTaskNamed(processModel.getProcesses().get(0)
                    .getFlowElements(), "Update order fields");
            CallActivity callActivity = assertContainsCallActivityNamed(processModel.getProcesses().get(0)
                    .getFlowElements(), "Save order");
            assertEquals("UpdateOrder", callActivity.getCalledElement());

            writeToFile(processModel, "Orchestration.bpmn");
        } catch (IOException e) {
            e.printStackTrace();
            fail(String.format("%1$s: %2$s", e.getClass().getName(),
                    e.getMessage()));
        }
    }

    private void writeToFile(BpmnModel processModel, String fileName)
            throws UnsupportedEncodingException, FileNotFoundException,
            IOException {
        byte[] bytes = definer.convertToBpmn(processModel, "UTF-8");
        FileOutputStream stream = null;
        try {
            File bpmnFile = new File(outputDir, fileName);
            stream = new FileOutputStream(bpmnFile);
            stream.write(bytes);
            assertTrue(bpmnFile.exists());
        } finally {
            stream.close();
        }
    }
}
