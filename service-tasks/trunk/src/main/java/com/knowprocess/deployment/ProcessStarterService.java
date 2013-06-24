package com.knowprocess.deployment;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;

public class ProcessStarterService implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        System.out.println("ProcessStarterService.execute");
        String id = (String) execution.getVariable("deploymentId");
        ProcessDefinition pd = execution.getEngineServices()
                .getRepositoryService().createProcessDefinitionQuery()
                .deploymentId(id).singleResult();
        ProcessInstance processInstance = execution.getEngineServices()
                .getRuntimeService()
                .startProcessInstanceById(pd.getId());
        System.out.println("started proc: " + processInstance.getId());
        execution.setVariable("processInstanceId", processInstance.getId());
    }

}
