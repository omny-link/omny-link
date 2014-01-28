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
		System.out.println("Seeking deployment with id: " + id);
        ProcessDefinition pd = execution.getEngineServices()
                .getRepositoryService().createProcessDefinitionQuery()
                .deploymentId(id).singleResult();
		System.out.println("... found deployment: " + pd);
		System.out.println("... id: " + pd.getId());
        ProcessInstance processInstance = execution.getEngineServices()
                .getRuntimeService()
                .startProcessInstanceById(pd.getId());
		System.out.println("started proc: " + processInstance);
		execution.setVariable("processInstanceId", processInstance.getId());
    }

}
