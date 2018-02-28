package com.knowprocess.cucumber.workmgmt;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;

import org.springframework.http.HttpStatus;

import com.knowprocess.bpm.model.ProcessInstance;
import com.knowprocess.bpm.model.Task;
import com.knowprocess.cucumber.IntegrationTestSupport;
import com.knowprocess.cucumber.ResponseResults;

import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

public class ProcessEngineStepDefs extends IntegrationTestSupport {

    @Then("^a (\\d+) response is returned identifying the process instance created$")
    public void a_response_is_returned_identifying_the_process_instance_created(int httpCode) throws Throwable {
        latestResponse.statusCodeIs(HttpStatus.valueOf(httpCode));
        String location = latestResponse.location();
        assertNotNull(location);
        instanceId = location.substring(location.lastIndexOf('/')+1);
        assertNotNull(instanceId);
    }

    @Then("^the process data includes '([\\w]*)'$")
    public void the_process_data_includes_var(String varName) throws Throwable {
        ResponseResults response = executeGet(String.format("/%1$s/process-instances/%2$s/variables/%3$s", tenantId, instanceId, varName));
        assertTrue("Unable to find var "+varName, response.statusCodeIsSuccess());
        assertNotNull(response);
    }

    @When("^the user task '([\\w]*)' is completed$")
    public void the_user_task_is_completed(String activityId) throws Throwable {
        Thread.sleep(1000); // Allow time for task to be created
        ResponseResults response = executeGet(String.format("/%1$s/tasks/%2$s/", tenantId, "tim@omny.link"));
        Task[] tasks = (Task[]) response.parseArray(Task.class);
        assertNotNull(tasks);
        for (Task task : tasks) {
            // Basically if it's a support ticket in the last minute, we reckon it's the right one
            if (task.getTaskDefinitionKey().equals(activityId) && (new Date().getTime()-task.getCreateTime().getTime()) < 3*60*1000) {
                response = executePut(
                        String.format("/task/%2$s?complete=complete", tenantId, task.getId()),
                        task);
                Task updatedTask = (Task) response.parseObject(Task.class);
                assertNotNull(updatedTask);     
            }
        }
    }

    @Then("^the process has completed$")
    public void the_process_has_completed() throws Throwable {
        ResponseResults response = executeGet(String.format("/%1$s/process-instances/%2$s", tenantId, instanceId));
        ProcessInstance instance = (ProcessInstance) response.parseObject(ProcessInstance.class);
        assertNotNull(instance);
        assertTrue(instance.getEnded());
    }
    
    @Then("^the process has completed within (\\d+)ms$")
            public void the_process_has_completed_within_ms(int timeout) throws Throwable {
        Thread.sleep(timeout);
        ResponseResults response = executeGet(String.format("/%1$s/process-instances/%2$s", tenantId, instanceId));
        ProcessInstance instance = (ProcessInstance) response.parseObject(ProcessInstance.class);
        assertNotNull(instance);
        assertTrue(instance.getEnded());
    }

}
