package com.knowprocess.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.activiti.engine.form.TaskFormData;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.runtime.Job;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.runtime.ProcessInstanceQuery;
import org.activiti.engine.task.IdentityLink;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.ActivitiRule;

/**
 * Provides observation and assertion support for tasks created within process
 * tests. Consider submitting as patch to Activiti.
 * 
 * @author tstephen
 * 
 */
public class ExtendedRule extends ActivitiRule {
    private Map<String, Object> emptyData = Collections.emptyMap();

    public ExtendedRule(String alternateConfig) {
        super(alternateConfig);
    }

    public void dumpProcessState(String piid) {
        ProcessInstanceQuery query = runtimeService
                .createProcessInstanceQuery().processInstanceId(piid);
        List<ProcessInstance> list = query.list();
        if (list.size() == 1) {
            ProcessInstance pi = list.get(0);
            System.out.println("process instance: " + pi.getId());
            System.out.println("... ended?: " + pi.isEnded());

            List<Job> jobList = managementService.createJobQuery()
                    .processInstanceId(piid).list();
            for (Job job : jobList) {
                System.out.println("job: " + job.getId() + ", "
                        + job.getExceptionMessage());
            }

            List<Task> list2 = taskService.createTaskQuery()
                    .processInstanceId(piid).list();
            for (Task task : list2) {
                System.out.print("...task: " + task.getName());
                if (task.getAssignee() == null
                        || task.getAssignee().trim().length() == 0) {
                    List<IdentityLink> identityLinksForTask = taskService
                            .getIdentityLinksForTask(task.getId());
                    System.out.print(", pails: ");
                    for (IdentityLink identityLink : identityLinksForTask) {
                        System.out.print("group=" + identityLink.getGroupId()
                                + ",user=" + identityLink.getUserId() + ";");
                    }
                    System.out.println();
                } else {
                    System.out.println(", assignee: " + task.getAssignee());
                }
            }

            Map<String, Object> variables = runtimeService.getVariables(piid);
            for (Map.Entry entry : variables.entrySet()) {
                System.out.println(entry.getKey() + " = " + entry.getValue());
            }
        } else {
            assertEquals(0, list.size());
            System.out.println("found '" + list.size()
                    + "' proc instances, assume ended");
        }

        System.out.println("Audit info for process: " + piid);
        List<HistoricActivityInstance> activityHistory = historyService
                .createHistoricActivityInstanceQuery().processInstanceId(piid)
                .list();
        for (HistoricActivityInstance ai : activityHistory) {
            System.out.println("..."
                    + ai.getActivityName()
                    + "("
                    + ai.getActivityType()
                    + "): assigned to: "
                    + ai.getAssignee()
                    + ", complete?: "
                    + (ai.getEndTime() == null ? "outstanding" : ai
                            .getEndTime()));
        }
    }

    public String assertAssignedTaskExists(String taskName, String participant) {
        return assertTaskExists(taskName, participant, true, null);
    }

    public String assertCandidateTaskExists(String taskName, String participant) {
        return assertTaskExists(taskName, participant, false, null);
    }

    public String assertTaskExists(String taskName, String participant,
            boolean assigned) {
        return assertTaskExists(taskName, participant, assigned, null);
    }

    public String assertTaskExists(String taskName, String participant,
            boolean assigned, String formKey) {
        return assertTaskExists(taskName, participant, assigned, formKey, null);
    }

    public String assertTaskExists(String taskName, String participant,
            boolean assigned, String formKey, Date dueDate) {
        return assertTaskExists(taskName, participant, assigned, formKey,
                dueDate, 50, null);
    }

    public String assertTaskExists(String taskName, String participant,
            boolean assigned, String formKey, Date dueDate, int priority,
            String bizKey) {
        List<Task> tasks = null;
        List<String> groups = new ArrayList<String>();
        groups.add(participant);
        if (assigned) {
            tasks = getTaskService().createTaskQuery()
                    .taskAssignee(participant).taskName(taskName).list();
        } else if (participant.indexOf('@') == -1) {
            tasks = taskService.createTaskQuery().taskCandidateGroupIn(groups)
                    .taskName(taskName).list();
        } else {
            tasks = taskService.createTaskQuery()
                    .taskCandidateUser(participant).taskName(taskName).list();
        }
        assertEquals("Unexpected no. of tasks named '" + taskName + "'", 1,
                tasks.size());
        Task task = tasks.get(0);
        assertEquals(taskName, task.getName());
        if (formKey != null) {
            TaskFormData taskFormData = formService.getTaskFormData(task
                    .getId());
            assertEquals(formKey, taskFormData.getFormKey());
        }

        // Note, do not get local vars here, no need (yet): YAGNI
        Map<String, Object> vars = taskService.getVariables(task.getId());
        // TODO comment this whilst working on TaskRank
        // assertEquals(priority, task.getPriority());

        if (!assigned) {
            taskService.claim(task.getId(), participant);
            tasks = taskService.createTaskQuery().taskAssignee(participant)
                    .taskName(taskName).list();

            assertEquals(1, tasks.size());
        }

        // check task's due date
        if (dueDate != null) {
            assertNotNull(task.getDueDate());
            // allow some leeway (2 sec) as not all dates are set exactly
            long diff = Math.abs(dueDate.getTime()
                    - task.getDueDate().getTime());
            System.out.println("diff: " + diff);
            assertTrue(diff < 2000);
        }

        return task.getId();
    }

    public void replaceCandidateUserForTask(String taskId, String currentUser,
            String newUser) {
        Task task = taskService.createTaskQuery()
                .taskCandidateUser(currentUser).taskId(taskId).singleResult();
        taskService.deleteCandidateUser(task.getId(), currentUser);
        taskService.addCandidateUser(task.getId(), newUser);
    }

    public void reassignTask(String taskName, String currentAssignee,
            String newAssignee) {
        List<Task> list = taskService.createTaskQuery()
                .taskAssignee(currentAssignee).taskName(taskName).list();
        for (Task task : list) {
            task.setAssignee(newAssignee);
            taskService.saveTask(task);
        }
    }

    public void assertComplete(ProcessInstance processInstance) {
        List<ProcessInstance> list = getRuntimeService()
                .createProcessInstanceQuery()
                .processInstanceId(processInstance.getId()).list();
        // assertTrue(result.isEnded());
    }
}
