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
