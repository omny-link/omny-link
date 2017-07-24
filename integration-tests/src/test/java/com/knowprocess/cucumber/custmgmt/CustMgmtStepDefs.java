package com.knowprocess.cucumber.custmgmt;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.springframework.http.HttpStatus;

import com.knowprocess.bpm.model.ProcessInstance;
import com.knowprocess.cucumber.IntegrationTestSupport;
import com.knowprocess.cucumber.ResponseResults;

import cucumber.api.PendingException;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

public class CustMgmtStepDefs extends IntegrationTestSupport {

    private String instanceId;

    @When("^an omny\\.enquiry message including contact info is submitted for tenant '([\\w]*)'$")
    public void an_omny_enquiry_message_inc_contact_is_submitted_for_tenant(String tenant) throws Throwable {
        StringBuilder enquiry = new StringBuilder();
        tenantId = tenant;
        enquiry.append("{"
            +"\"lastName\":\"Burns\","
            +"\"firstName\":\"Charles Montgomery III\","
            +"\"email\":\"mrburns@springfieldpower.com\","
            +"\"phone1\":\"44987654321\","
            +"\"type\":\"Enquiry\","
            +"\"message\":\"Hello there, I'd like to get a trial of Omny please\","
            +"\"tenantId\":\""+tenant+"\""
          +"}");
        executePost("/msg/"+tenant+"/omny.enquiry.json", enquiry.toString());
    }

    @When("^an omny\\.enquiry message including both contact and account is submitted for tenant '([\\w]*)'$")
    public void an_omny_enquiry_message_inc_contact_and_account_is_submitted_for_tenant(String tenant) throws Throwable {
        StringBuilder enquiry = new StringBuilder();
        tenantId = tenant;
        enquiry.append("{"
            +"\"lastName\":\"Burns\","
            +"\"firstName\":\"Charles Montgomery III\","
            +"\"email\":\"mrburns@springfieldpower.com\","
            +"\"phone1\":\"44987654321\","
            +"\"type\":\"Enquiry\","
            +"\"message\":\"Hello there, I'd like to get a trial of Omny please\","
            +"\"tenantId\":\""+tenant+"\""
          +"}");
        executePost("/msg/"+tenant+"/omny.enquiry.json", enquiry.toString());
    }

    @Then("^a (\\d+) response is returned identifying the process instance created$")
    public void a_response_is_returned_identifying_the_process_instance_created(int httpCode) throws Throwable {
        latestResponse.statusCodeIs(HttpStatus.valueOf(httpCode));
        String location = latestResponse.location();
        assertNotNull(location);

//        pollForProcessCompletion(location, 60000);

    }

    @Then("^the contact is retrievable by the provided identifier$")
    public void the_contact_is_retrievable_by_the_provided_identifier() throws Throwable {
        throw new PendingException();
    }

    @Then("^the process data includes '([\\w]*)'$")
    public void the_process_data_includes_var(String varName) throws Throwable {
        ResponseResults response = executeGet(String.format("/%1$s/%2$s/variables/%3$s", tenantId, instanceId, varName));
        assertTrue("Unable to find var "+varName, response.statusCodeIsSuccess());
        Object var = response.latestObject();
        assertNotNull(var);
    }

    private void pollForProcessCompletion(String location, int timeout) throws IOException, InterruptedException {
        ResponseResults response = executeGet(location);
        ProcessInstance instance = (ProcessInstance) response.parseObject(ProcessInstance.class);
        assertNotNull(instance);
        instanceId = instance.getId();

        if (!instance.getEnded() && timeout > 0) {
            Thread.sleep(timeout);
            pollForProcessCompletion(location, timeout);
        }
    }

}
