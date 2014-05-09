package org.activiti.spring.rest.beans;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.activiti.spring.rest.model.Task;
import org.junit.Test;

public class MessageRegistryTest {

    private static final String userEmail = "tim@knowprocess.com";
    protected static MessageRegistry msgRegistry = new MessageRegistry();

    @Test
    public void testTodoMessage() {
        String json = "{\"assignee\":\"" + userEmail + "\"}";
        String msgType = "org.activiti.spring.rest.model.Task";

        Task bean = (Task) msgRegistry.deserialiseMessage(msgType, json);
        assertNotNull(bean);
        System.out.println("bean: " + bean);
        assertEquals(userEmail, bean.getAssignee());
    }

    @Test
    public void testFullTodoMessage() {
        String description = "P1D";
        int priority = 25;
        String json = "{\"assignee\":\"" + userEmail + "\",\"description\":\""
                + description + "\",\"priority\":\"" + priority + "\"}";
        String msgType = "org.activiti.spring.rest.model.Task";

        Task task = (Task) msgRegistry.deserialiseMessage(msgType, json);
        assertNotNull(task);
        System.out.println("task: " + task);
        assertEquals(userEmail, task.getAssignee());
        assertEquals(description, task.getDescription());
        assertEquals(priority, task.getPriority().intValue());
    }
}
