package com.knowprocess.bpm.model;

import lombok.Data;

@Data
public class OperationsSummary {

    protected String tenantId;
    protected long jobs;
    protected long completedInstances;
    protected long activeInstances;
    protected long activeDefinitions;
    protected long totalDefinitions;
    protected long tasks;
    protected long users;
}
