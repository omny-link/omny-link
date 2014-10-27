package org.activiti.spring.rest.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.form.TaskFormData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.roo.addon.equals.RooEquals;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.jpa.activerecord.RooJpaActiveRecord;
import org.springframework.roo.addon.json.RooJson;
import org.springframework.roo.addon.serializable.RooSerializable;
import org.springframework.roo.addon.tostring.RooToString;
import org.springframework.stereotype.Component;

import flexjson.JSONSerializer;

@SuppressWarnings("serial")
@RooJavaBean
@RooToString
@RooJpaActiveRecord
@RooEquals
@RooSerializable
@RooJson
@Component
public class Task {
    protected static final Logger LOGGER = LoggerFactory.getLogger(Task.class);

    private static final String[] JSON_FIELDS = { "assignee", "createTime",
            "dueDate", "description", "id", "name", "owner", "parentTaskId",
            "priority", "processDefinitionId", "suspended",
            "taskDefinitionKey", "formKey", "deploymentId", "formProperties" };

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
    private Integer priority;

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

    private List<FormProperty> formProperties;

    public Task() {
        super();
        formProperties = new ArrayList<FormProperty>();
    }

    public Task(org.activiti.engine.task.Task t) {
        this();
        setId(t.getId());
        setAssignee(t.getAssignee());
        setCreateTime(t.getCreateTime());
        // setDelegateState("TODO");
        setDescription(t.getDescription());
        setName(t.getName());
        setOwner(t.getOwner());
        setParentTaskId(t.getParentTaskId());
        setProcessDefinitionId(t.getProcessDefinitionId());
        setPriority(Integer.valueOf(t.getPriority()));
        setTaskDefinitionKey(t.getTaskDefinitionKey());
        setSuspended(t.isSuspended());
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
                    .list());
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public static Task findTask(String id) {
        Task task = wrap(
                getProcessEngine().getTaskService().createTaskQuery()
                        .taskId(String.valueOf(id)).list()).get(0);
        TaskFormData formData = getProcessEngine().getFormService()
                .getTaskFormData(
                id);
        task.setDeploymentId(formData.getDeploymentId());
        task.setFormKey(formData.getFormKey());
        for (org.activiti.engine.form.FormProperty prop : formData
                .getFormProperties()) {
            task.getFormProperties().add(new FormProperty(prop));
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

    public static List<Task> findAllTasks(String involvesUser, String sortFieldName, String sortOrder) {
        // TODO honour sort order
        return wrap(getProcessEngine().getTaskService().createTaskQuery()
                .list());
    }

    private static List<Task> wrap(
            final List<org.activiti.engine.task.Task> list) {
        ArrayList<Task> list2 = new ArrayList<Task>();
        for (org.activiti.engine.task.Task instance : list) {
            list2.add(new Task(instance));
        }
        return list2;
    }

    public String toJson() {
        return toJson(JSON_FIELDS);
    }

    public String toJson(String[] fields) {
        return new JSONSerializer().include(fields).exclude("*.class")
                .exclude("*.processEngine").serialize(this);
    }

    public static String toJsonArray(Collection<Task> collection) {
        return toJsonArray(collection, JSON_FIELDS);
    }

    public static String toJsonArray(Collection<Task> collection,
            String[] fields) {
        System.out.println("toJsonArray....");
        return new JSONSerializer().exclude("*.class")
                .exclude("*.processEngine").include(fields)
                .serialize(collection);
    }
}
