/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.omg.bpmn.miwg.activiti;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.junit.After;
import org.junit.Test;

/**
 * @author Tim Stephenson
 */
public class RestServiceTaskTest extends PluggableActivitiTestCase {

    private Deployment deployment;

    @After
    public void tearDown() {
        repositoryService.deleteDeployment(deployment.getId(), true);
    }

    @Test
    public void testA_1_1_REST() {
        try {
            System.out.println("engine: " + processEngine.toString());
            System.out.println("engine cfg: "
                    + processEngineConfiguration.toString());

            deployment = repositoryService
                    .createDeployment()
                    .addClasspathResource(
                            "org/omg/bpmn/miwg/Reference/A.1.1.REST.bpmn")
                    .deploy();
            assertNotNull(deployment);

            ProcessInstance instance = runtimeService
                    .startProcessInstanceByKey("process-A.1.1.REST");
            assertNotNull(instance);

            Task task = taskService.createTaskQuery()
                    .processInstanceId(instance.getId()).singleResult();
            assertEquals("task1", task.getTaskDefinitionKey());
            taskService.complete(task.getId());

            // Web service 'task2' called here

            task = taskService.createTaskQuery()
                    .processInstanceId(instance.getId()).singleResult();
            assertEquals("task3", task.getTaskDefinitionKey());
            taskService.complete(task.getId());
        } catch (ActivitiException e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void testA_1_2_REST() {
        try {
            System.out.println("engine: " + processEngine.toString());
            System.out.println("engine cfg: "
                    + processEngineConfiguration.toString());

            deployment = repositoryService
                    .createDeployment()
                    .addClasspathResource(
                            "org/omg/bpmn/miwg/Reference/A.1.2.REST.bpmn")
                    .deploy();
            assertNotNull(deployment);

            ProcessInstance instance = runtimeService
                    .startProcessInstanceByKey("process-A.1.2.REST");
            assertNotNull(instance);

            Task task = taskService.createTaskQuery()
                    .processInstanceId(instance.getId()).singleResult();
            assertEquals("task1", task.getTaskDefinitionKey());
            taskService.complete(task.getId());

            // Web service 'task2' called here

            task = taskService.createTaskQuery()
                    .processInstanceId(instance.getId()).singleResult();
            assertEquals("task3", task.getTaskDefinitionKey());
            taskService.complete(task.getId());
        } catch (ActivitiException e) {
            e.printStackTrace();
            fail();
        }
    }
}
