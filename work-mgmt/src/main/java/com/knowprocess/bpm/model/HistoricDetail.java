package com.knowprocess.bpm.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

import org.activiti.engine.history.HistoricActivityInstance;
import org.springframework.stereotype.Component;

@Data
@Component
@NoArgsConstructor
public class HistoricDetail implements Serializable {

    private static final long serialVersionUID = -3639106734679237763L;

    private String id;

    private String taskId;

    private Date time;
    //
    // private String variableName;
    //
    // private Object variableValue;

    public java.lang.String activityName;

    public java.lang.String activityType;

    public java.lang.String assignee;

    public java.util.Date startTime;

    public java.util.Date endTime;

    public java.lang.Long durationInMillis;

    public String processInstanceId;

    public String processDefinitionId;

    public String calledProcessInstanceId;

    public List<Allocation> allocations;

    // public HistoricDetail(org.activiti.engine.history.HistoricDetail detail)
    // {
    // setId(detail.getId());
    // setTaskId(detail.getTaskId());
    // setTime(detail.getTime());
    // if (detail instanceof HistoricVariableUpdate) {
    // setVariableName(((HistoricVariableUpdate)detail).getVariableName());
    // setVariableValue(((HistoricVariableUpdate) detail).getValue());
    // }
    // }

    public HistoricDetail(HistoricActivityInstance detail) {
        setId(detail.getId());
        setTaskId(detail.getTaskId());
        setActivityName(detail.getActivityName());
        setActivityType(detail.getActivityType());
        setTime(detail.getTime());
        setAssignee(detail.getAssignee());
        setStartTime(detail.getStartTime());
        setEndTime(detail.getEndTime());
        setDurationInMillis(detail.getDurationInMillis());
        setCalledProcessInstanceId(detail.getCalledProcessInstanceId());
        setProcessInstanceId(detail.getProcessInstanceId());
        setProcessDefinitionId(detail.getProcessDefinitionId());
    }

    public List<Allocation> getAllocations() {
        if (allocations==null) { 
            allocations = new ArrayList<Allocation>();
        }
        return allocations;
    }

    public void addAllocation(Allocation allocation) {
        getAllocations().add(allocation);
    }
}