package com.knowprocess.bpm.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import lombok.Data;

import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricVariableInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Data
@Component
public class ProcessInstance extends Execution {

    private static ProcessEngine processEngine;

    /**
     */
    private String businessKey;

    /**
     */
    private String processDefinitionId;

    /**
     */
    private Boolean suspended;

    private Map<String, Object> processVariables;

    public ProcessInstance() {
        super();
    }

    public ProcessInstance(org.activiti.engine.runtime.ProcessInstance pi) {
        this();
        setBusinessKey(pi.getBusinessKey());
        setProcessDefinitionId(pi.getProcessDefinitionId());
        setSuspended(pi.isSuspended());
        setProcessVariables(pi.getProcessVariables());
    }

    public ProcessInstance(HistoricProcessInstance hpi) {
        this();
        setBusinessKey(hpi.getBusinessKey());
        setProcessDefinitionId(hpi.getProcessDefinitionId());
        setSuspended(false);
        setProcessVariables(hpi.getProcessVariables());
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
        return processVariables;
    }

    public static long countProcessInstances() {
        return processEngine.getRuntimeService().createProcessInstanceQuery()
                .count();
    }

    public static List<ProcessInstance> findAllProcessInstances() {
        List<ProcessInstance> instances = new ArrayList<ProcessInstance>();
        instances.addAll(wrap(processEngine.getRuntimeService()
                .createProcessInstanceQuery().list()));
        // instances.addAll(wrap(processEngine.getHistoryService()
        // .createHistoricProcessInstanceQuery().list()
        // .toArray(new HistoricProcessInstance[])));
        return instances;
    }

    public static ProcessInstance findProcessInstance(String id) {
        List<org.activiti.engine.runtime.ProcessInstance> list = processEngine
                .getRuntimeService().createProcessInstanceQuery()
                .processDefinitionId(id).list();
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
                System.err.println("Cannot find process with id: " + id);
                throw new ActivitiObjectNotFoundException(ProcessInstance.class);
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

    private static List<ProcessInstance> wrap(
            final List<org.activiti.engine.runtime.ProcessInstance> list) {
        ArrayList<ProcessInstance> list2 = new ArrayList<ProcessInstance>();
        for (org.activiti.engine.runtime.ProcessInstance instance : list) {
            list2.add(new ProcessInstance(instance));
        }
        return list2;
    }

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
