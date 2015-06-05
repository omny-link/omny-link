package com.knowprocess.bpm.model;

import javax.persistence.Id;

import lombok.Data;

import org.springframework.stereotype.Component;

@Data
@Component
public class Execution {

    /**
     */
    private String activityId;

    /**
     */
    @Id
    private String id;

    /**
     */
    private String parentId;

    /**
     */
    private String processInstanceId;

    /**
     */
    private Boolean ended;

    public Execution() {
        ;
    }

    public Execution(org.activiti.engine.runtime.Execution exe) {
        this();
        setActivityId(exe.getActivityId());
        setId(exe.getId());
        setParentId(exe.getParentId());
        setProcessInstanceId(exe.getProcessInstanceId());
        setEnded(exe.isEnded());
    }

    // public static String toJsonArray(Collection<? extends Execution>
    // collection) {
    // String[] fields = { "businessKey", "processDefinitionId", "suspended" };
    // return toJsonArray(collection, fields);
    // }
    //
    // public static String toJsonArray(
    // Collection<? extends Execution> collection,
    // String[] fields) {
    // System.out.println("toJsonArray....");
    // return new JSONSerializer().exclude("*.class")
    // .exclude("*.processEngine").include(fields)
    // .serialize(collection);
    // }
}
