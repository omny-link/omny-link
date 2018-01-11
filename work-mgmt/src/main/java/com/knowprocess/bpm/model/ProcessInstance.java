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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.ManagementService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricVariableInstance;
import org.activiti.engine.impl.cmd.AbstractCustomSqlExecution;
import org.activiti.engine.impl.cmd.CustomSqlExecution;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.knowprocess.bpm.impl.TaskAllocationMapper;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Component
public class ProcessInstance extends Execution
/* implements org.activiti.engine.runtime.ProcessInstance */{

    private static final long serialVersionUID = 6550600017648645656L;

    protected static ProcessEngine processEngine;

    /**
     */
    private String businessKey;

    /**
     */
    private String processDefinitionId;

    private String processDefinitionKey;

    /**
     */
    private Boolean suspended;

    private Map<String, Object> processVariables;

    private List<HistoricDetail> auditTrail;

    private Date startTime;

    private Date endTime;

    private String superInstanceId;

    public ProcessInstance() {
        super();
    }

    public ProcessInstance(org.activiti.engine.runtime.ProcessInstance pi) {
        this();
        setActivityId(pi.getActivityId());
        setBusinessKey(pi.getBusinessKey());
        setEnded(pi.isEnded());
        setId(pi.getId());
        setParentId(pi.getParentId());
        setProcessDefinitionId(pi.getProcessDefinitionId());
        setProcessInstanceId(pi.getProcessInstanceId());
        setProcessVariables(pi.getProcessVariables());
        if (pi instanceof ExecutionEntity) {
            setSuperInstanceId(((ExecutionEntity) pi).getSuperExecutionId());
        }
        setSuspended(pi.isSuspended());
        setTenantId(pi.getTenantId());
    }

    public ProcessInstance(HistoricProcessInstance hpi) {
        this();
        // Note that in the event of multiple end events this will not be
        // deterministic
        setActivityId(hpi.getEndActivityId());
        setBusinessKey(hpi.getBusinessKey());
        setStartTime(hpi.getStartTime());
        setEndTime(hpi.getEndTime());
        setEnded(hpi.getEndTime() != null);
        setId(hpi.getId());
        setParentId(hpi.getSuperProcessInstanceId());
        setProcessDefinitionId(hpi.getProcessDefinitionId());
        // don't have an instance id
        setProcessVariables(hpi.getProcessVariables());
        setSuspended(false);
        setTenantId(hpi.getTenantId());
    }

    public ProcessInstance(org.activiti.engine.runtime.Execution execution) {
        this();
        setActivityId(execution.getActivityId());
        setEnded(execution.isEnded());
        setId(execution.getId());
        setParentId(execution.getParentId());
        setSuspended(execution.isSuspended());
    }

    // Autowiring static fields is obviously dangerous, but should be ok in this
    // case as PE is thread safe.
    @Autowired(required = true)
    public void setProcessEngine(ProcessEngine pe) {
        ProcessInstance.processEngine = pe;
    }

    public void setProcessVariables(Map<String, Object> vars) {
        this.processVariables = vars;
    }

    public Map<String, Object> getProcessVariables() {
        if (processVariables == null) {
            processVariables = new HashMap<String, Object>();
        }
        return processVariables;
    }

    public static long countProcessInstances() {
        return processEngine.getRuntimeService().createProcessInstanceQuery()
                .count();
    }

    public List<HistoricDetail> getAuditTrail() {
        if (auditTrail == null) {
            auditTrail = new ArrayList<HistoricDetail>();
        }
        return auditTrail;
    }

    public static List<ProcessInstance> findAllProcessInstances() {
        List<ProcessInstance> instances = new ArrayList<ProcessInstance>();
        instances.addAll(wrap(processEngine.getRuntimeService()
                .createProcessInstanceQuery().list()));
        instances.addAll(wrap(processEngine.getHistoryService()
                .createHistoricProcessInstanceQuery().list()));
        return instances;
    }

    public static ProcessInstance findProcessInstance(String id) {
        List<org.activiti.engine.runtime.ProcessInstance> list = processEngine
                .getRuntimeService().createProcessInstanceQuery()
                .processInstanceId(id).list();
        ProcessInstance instance = null;
        if (list.size() > 0) {
            instance = wrap(list).get(0);
            instance.setProcessVariables(processEngine.getRuntimeService()
                    .getVariables(id));
        } else {
            // assume already ended
            List<HistoricProcessInstance> endedList = processEngine
                    .getHistoryService().createHistoricProcessInstanceQuery()
                    .processInstanceId(id).list();
            if (endedList.size() > 0) {
                HistoricProcessInstance hpi = endedList.get(0);
                instance = new ProcessInstance(hpi);
                List<HistoricVariableInstance> histVars = processEngine
                        .getHistoryService()
                        .createHistoricVariableInstanceQuery()
                        .processInstanceId(id).list();
                for (HistoricVariableInstance histVar : histVars) {
                    instance.getProcessVariables().put(
                            histVar.getVariableName(), histVar.getValue());
                }
            } else {
                List<org.activiti.engine.runtime.Execution> execList
                        = processEngine.getRuntimeService().createExecutionQuery().executionId(id).list();
                if (execList.size() > 0) {
                    return findProcessInstance(execList.get(0).getProcessInstanceId());
                } else {
                    System.err.println("Cannot find process with id: " + id);
                    throw new ActivitiObjectNotFoundException(ProcessInstance.class);
                }
            }
        }
        return instance;
    }

    public static List<ProcessInstance> findProcessInstanceEntries(
            int firstResult, int maxResults) {
        return wrap(processEngine.getRuntimeService()
                .createProcessInstanceQuery().listPage(firstResult, maxResults));
    }

    public static List<ProcessInstance> findProcessInstanceEntries(
            int firstResult, int maxResults, String sortFieldName,
            String sortOrder) {
        // TODO honour sort order
        return wrap(processEngine.getRuntimeService()
                .createProcessInstanceQuery().listPage(firstResult, maxResults));
    }

    public static List<ProcessInstance> findAllProcessInstances(
            String sortFieldName, String sortOrder) {
        System.out.println("pe: " + processEngine);
        // TODO honour sort order
        return wrap(processEngine.getRuntimeService()
                .createProcessInstanceQuery().list());
    }

    public static List<ProcessInstance> wrap(final List<?> list) {
        ArrayList<ProcessInstance> list2 = new ArrayList<ProcessInstance>();
        for (Object instance : list) {
            if (instance instanceof org.activiti.engine.runtime.ProcessInstance) {
                list2.add(new ProcessInstance(
                        (org.activiti.engine.runtime.ProcessInstance) instance));
            } else {
                list2.add(new ProcessInstance(
                        (HistoricProcessInstance) instance));
            }
        }
        return list2;
    }

    public void addToAuditTrail(List<HistoricActivityInstance> list) {
        auditTrail = getAuditTrail();
        ManagementService managementService = processEngine
                .getManagementService();
        for (HistoricActivityInstance detail : list) {
            HistoricDetail wrapper = new HistoricDetail(detail);
            auditTrail.add(wrapper);
            if ("userTask".equals(detail.getActivityType())) {

                CustomSqlExecution<TaskAllocationMapper, List<Map<String, Object>>> customSqlExecution = new AbstractCustomSqlExecution<TaskAllocationMapper, List<Map<String, Object>>>(
                        TaskAllocationMapper.class) {
                    public List<Map<String, Object>> execute(
                            TaskAllocationMapper customMapper) {
                        return customMapper.selectTaskAllocation(detail
                                .getTaskId());
                    }
                };

                List<Map<String, Object>> results = managementService
                        .executeCustomSql(customSqlExecution);
                for (Map<String, Object> map : results) {
                    wrapper.addAllocation(new Allocation(map.get("type"), map
                            .get("groupId"), map.get("userId")));
                }
            }
        }
    }

    // public void setAuditTrail(
    // List<org.activiti.engine.history.HistoricDetail> list) {
    // System.out.println("setAuditTrail: " + list.size());
    // auditTrail = new ArrayList<HistoricDetail>();
    // for (org.activiti.engine.history.HistoricDetail detail : list) {
    // auditTrail.add(new HistoricDetail(detail));
    // }
    // }

    // private static List<? extends ProcessInstance> wrap(
    // final List<HistoricProcessInstance> list) {
    // ArrayList<ProcessInstance> list2 = new ArrayList<ProcessInstance>();
    // for (org.activiti.engine.runtime.ProcessInstance instance : list) {
    // list2.add(new ProcessInstance(instance));
    // }
    // return list2;
    // }

    // public String toJson() {
    // return new JSONSerializer()
    // .exclude("*.class").exclude("processEngine")
    // .serialize(this);
    // }
}
