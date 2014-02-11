package com.knowprocess.test.activiti;

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
import org.activiti.engine.history.HistoricVariableInstance;
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
	public static final int DEFAULT_PRIORITY = 50;
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
			System.out.println("******* process instance: " + pi.getId()
					+ " *******");
			// TODO This is _always_ false as record will be gone if is ended
			// System.out.println("... ended?: " + pi.isEnded());

			dumpCurrentJobs(piid);
			dumpCurrentTasks(piid);
			dumpVariables(piid);
		} else {
			assertEquals(0, list.size());
			System.out.println("found '" + list.size()
					+ "' proc instances, assume ended");
		}

		dumpAuditTrail(piid);
	}

	public void dumpCurrentJobs(String piid) {
		List<Job> jobList = managementService.createJobQuery()
				.processInstanceId(piid).list();
		for (Job job : jobList) {
			System.out.println("job: " + job.getId() + ", "
					+ job.getExceptionMessage());
		}
	}

	public void dumpCurrentTasks(String piid) {
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
	}

	public void dumpVariables(String piid) {
		System.out.println(String.format(
				"************ Process variables for piid %1$s ************",
				piid));
		if (isComplete(piid)) {
			List<HistoricVariableInstance> list = getHistoryService()
					.createHistoricVariableInstanceQuery()
					.processInstanceId(piid).list();
			System.out.println(String.format("  (At end there were %1$s vars)",
					list.size()));
			for (HistoricVariableInstance hvi : list) {
				System.out.println(String.format("%1$s: %2$s",
						hvi.getVariableName(), hvi.getValue()));
			}
		} else {
			Map<String, Object> variables = runtimeService.getVariables(piid);
			for (Map.Entry<String, ?> entry : variables.entrySet()) {
				System.out.println(entry.getKey()
						+ " = "
						+ (entry.getValue() == null ? "null " : entry
								.getValue()));
			}
		}
	}

	public void dumpAuditTrail(String piid) {
		System.out.println(String.format(
				"************* Audit info for process: %1$s *************",
				piid));
		List<HistoricActivityInstance> activityHistory = historyService
				.createHistoricActivityInstanceQuery().processInstanceId(piid)
				.list();
		for (HistoricActivityInstance ai : activityHistory) {
			String msg = String.format(
					"... %1$s (%2$s):%3$s complete?: %4$s",
					ai.getActivityName(),
					ai.getActivityType(),
					(ai.getAssignee() == null ? "" : " assigned to: "
							+ ai.getAssignee())
							+ ",", (ai.getEndTime() == null ? "outstanding"
							: ai.getEndTime()));
			System.out.println(msg);
			if (ai.getActivityType().equals("callActivity")) {
				dumpProcessState(ai.getCalledProcessInstanceId());
			}
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
				dueDate, DEFAULT_PRIORITY, null);
	}

	/**
	 * @return taskId.
	 */
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
		assertTrue(isComplete(processInstance.getId()));
	}

	protected boolean isComplete(String piid) {
		return getHistoryService().createHistoricProcessInstanceQuery()
				.processInstanceId(piid).singleResult().getEndTime() != null;
	}

	public void assertVariableValue(String piid, String name, Object val) {
		List<HistoricVariableInstance> list = getHistoryService()
				.createHistoricVariableInstanceQuery().processInstanceId(piid)
				.variableName(name).list();
		assertEquals(String.format(
				"Variable holds wrong value, expected %1$s but was %2$s", val,
				list.get(0).getValue()), val, list.get(0).getValue());
	}
}
