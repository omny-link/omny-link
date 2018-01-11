/*******************************************************************************
 *Copyright 2018 Tim Stephenson and contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package com.knowprocess.bpm.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import lombok.Data;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.form.TaskFormData;
import org.activiti.engine.task.DelegationState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Data
@Component
public class Task implements Serializable, org.activiti.engine.task.Task {
    private static final long serialVersionUID = -5315914372085177492L;

    protected static final Logger LOGGER = LoggerFactory.getLogger(Task.class);

	// private static final String[] JSON_FIELDS = { "assignee", "createTime",
	// "dueDate", "description", "id", "name", "owner", "parentTaskId",
	// "priority", "processDefinitionId", "suspended",
	// "taskDefinitionKey", "formKey", "deploymentId", "formProperties" };

    private static ProcessEngine processEngine;

    /**
     */
    private String assignee;

    /**
     */
    @Temporal(TemporalType.TIMESTAMP)
    @DateTimeFormat(style = "M-")
    private Date createTime;

    /**
     */
    @Temporal(TemporalType.TIMESTAMP)
    @DateTimeFormat(style = "M-")
    private Date dueDate;

    /**
     */
    private String delegateState;

    /**
     */
    private String description;

    /**
     */
    @Id
    private String id;

    /**
     */
    private String name;

    /**
     */
    private String owner;

    /**
     */
    private String parentTaskId;

    /**
     */
    private int priority;

    /**
     */
    private String processDefinitionId;

    /**
     */
    private String taskDefinitionKey;

    /**
     */
    private Boolean suspended;

    private String formKey;

    private String deploymentId;

    private String executionId;

    private String processInstanceId;

    private String businessKey;

    // AKA Case?
    private String category;

    private String tenantId;

    private List<FormProperty> formProperties;

    private Map<String, Object> processVariables;

    private Map<String, Object> taskLocalVariables;

    public Task() {
        super();
        formProperties = new ArrayList<FormProperty>();
    }

    public Task(org.activiti.engine.task.Task t) {
        this();
        setId(t.getId());
        setAssignee(t.getAssignee());
        setCategory(t.getCategory());
        setCreateTime(t.getCreateTime());
        // setDelegateState(t.getDelegationState());
        setDescription(t.getDescription());
        setDueDate(t.getDueDate());
        setName(t.getName());
        setOwner(t.getOwner());
        setParentTaskId(t.getParentTaskId());
        setProcessDefinitionId(t.getProcessDefinitionId());
        setPriority(Integer.valueOf(t.getPriority()));
        setProcessInstanceId(t.getProcessInstanceId());
        setProcessVariables(t.getProcessVariables());
        setSuspended(t.isSuspended());
        setTaskDefinitionKey(t.getTaskDefinitionKey());
        setTaskLocalVariables(t.getTaskLocalVariables());
        setBusinessKey(processEngine.getRuntimeService()
                .createProcessInstanceQuery()
                .processInstanceId(t.getProcessInstanceId()).singleResult()
                .getBusinessKey());
        setTenantId(t.getTenantId());
    }

    // Autowiring static fields is obviously dangerous, but should be ok in this
    // case as PE is thread safe.
    @Autowired(required = true)
    public void setProcessEngine(ProcessEngine pe) {
        LOGGER.debug("injecting process engine to " + getClass().getName());
        Task.processEngine = pe;
    }

    private static ProcessEngine getProcessEngine() {
        if (processEngine == null) {
            throw new IllegalStateException(
                    "Process Engine MUST have been injected by now.");
        }
        return processEngine;
    }

    public static long countTasks() {
        return getProcessEngine().getTaskService().createTaskQuery().count();
    }

    public static List<Task> findAllTasks() {
        try {
            return wrap(getProcessEngine().getTaskService().createTaskQuery()
                    .includeTaskLocalVariables().includeProcessVariables()
                    .list());
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public static Task findTask(String id) {
        Task task = wrap(
                getProcessEngine().getTaskService().createTaskQuery()
                        .taskId(String.valueOf(id)).includeTaskLocalVariables()
                        .includeProcessVariables()
                        .list()).get(0);
        TaskFormData formData = getProcessEngine().getFormService()
                .getTaskFormData(id);
        task.setDeploymentId(formData.getDeploymentId());
        task.setFormKey(formData.getFormKey());
        for (org.activiti.engine.form.FormProperty prop : formData
                .getFormProperties()) {
            // TODO would work if we had credentials. consider using autowired
            // RestGet and moving this to TaskController
            // if (prop.getValue().startsWith("http://")) {
            // // Try to freshen the resource
            // try {
            // FormProperty fp = new FormProperty(prop);
            // fp.setValue(new RestGet().fetchToString(prop.getValue()));
            // task.getFormProperties().add(fp);
            // } catch (Exception e) {
            // LOGGER.error(e.getMessage());
            // task.getFormProperties().add(new FormProperty(prop));
            // }
            // } else {
                task.getFormProperties().add(new FormProperty(prop));
            // }
        }
        return task;
    }

    public static List<Task> findTaskEntries(int firstResult, int maxResults) {
        return wrap(processEngine.getTaskService().createTaskQuery()
                .listPage(firstResult, maxResults));
    }

    public static List<Task> findTaskEntries(String involvesUser, int firstResult,
            int maxResults, String sortFieldName, String sortOrder) {
        // TODO honour sort order
        return wrap(getProcessEngine().getTaskService().createTaskQuery()
                .listPage(firstResult, maxResults));
    }

    public static List<Task> findAllTasks(String tenantId, String involvesUser,
            String sortFieldName, String sortOrder) {
        // TODO honour sort order
        return wrap(getProcessEngine().getTaskService().createTaskQuery()
                .taskTenantId(tenantId).taskCandidateOrAssigned(involvesUser)
				.includeTaskLocalVariables()
                .list());
    }

    /**
     * @deprecated Use the version that takes a tenant id.
     */
    public static List<Task> findAllTasks(String involvesUser, String sortFieldName, String sortOrder) {
        // TODO honour sort order
        return wrap(getProcessEngine().getTaskService().createTaskQuery()
                .taskCandidateOrAssigned(involvesUser).list());
    }

    public static List<Task> wrap(
            final List<org.activiti.engine.task.Task> list) {
        ArrayList<Task> list2 = new ArrayList<Task>();
        for (org.activiti.engine.task.Task instance : list) {
            list2.add(new Task(instance));
        }
        return list2;
    }

    @Override
    public boolean isSuspended() {
        return suspended;
    }

    @Override
    @JsonIgnore
    public void delegate(String arg0) {
        LOGGER.error("Delegation not implemented");
    }

    @Override
    @JsonIgnore
    public DelegationState getDelegationState() {
        LOGGER.error("Delegation not implemented");
        return null;
    }

    @Override
    @JsonIgnore
    public void setDelegationState(DelegationState arg0) {
        LOGGER.error("Delegation not implemented");
    }

	// public String toJson() {
	// return toJson(JSON_FIELDS);
	// }
	//
	// public String toJson(String[] fields) {
	// return new JSONSerializer().include(fields).exclude("*.class")
	// .exclude("*.processEngine").serialize(this);
	// }
	//
	// public static String toJsonArray(Collection<Task> collection) {
	// return toJsonArray(collection, JSON_FIELDS);
	// }
	//
	// public static String toJsonArray(Collection<Task> collection,
	// String[] fields) {
	// System.out.println("toJsonArray....");
	// return new JSONSerializer().exclude("*.class")
	// .exclude("*.processEngine").include(fields)
	// .serialize(collection);
	// }
}
