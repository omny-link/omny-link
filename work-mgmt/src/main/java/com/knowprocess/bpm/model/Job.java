package com.knowprocess.bpm.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Id;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class Job implements Serializable {
    private static final long serialVersionUID = -2657367116355427744L;

    protected static final Logger LOGGER = LoggerFactory
            .getLogger(Job.class);

    @Id
    private String id;

    private String processInstanceId;

    private String processDefinitionId;

    private String executionId;

    private String exceptionMessage;

    private Integer retries;

    private Date dueDate;

    private String tenantId;

    @JsonProperty
    private transient Long instanceCount;

    public Job() {
        super();
    }

    public Job(org.activiti.engine.runtime.Job job) {
        this();
        setId(job.getId());
        setExceptionMessage(job.getExceptionMessage());
        setProcessInstanceId(job.getProcessInstanceId());
        setProcessDefinitionId(job.getProcessDefinitionId());
        setExecutionId(job.getExecutionId());
        setRetries(job.getRetries());
        setDueDate(job.getDuedate());
        setTenantId(job.getTenantId());
    }

}
