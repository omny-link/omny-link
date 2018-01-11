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
