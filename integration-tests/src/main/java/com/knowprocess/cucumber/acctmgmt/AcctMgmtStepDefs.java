package com.knowprocess.cucumber.acctmgmt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.springframework.http.HttpStatus;
import org.springframework.web.client.ResourceAccessException;
import org.subethamail.wiser.WiserMessage;

import com.knowprocess.cucumber.IntegrationTestSupport;
import com.knowprocess.cucumber.ResponseResults;

import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

public class AcctMgmtStepDefs extends IntegrationTestSupport{

    @When("^an 'omny\\.registration' message is submitted for tenant '([\\w]*)'$")
    public void a_registration_message_is_submitted_for_tenant(String tenantId) throws Throwable {
        IntegrationTestSupport.tenantId = tenantId;
        String messageId = "omny.registration";
        executePost(String.format("/msg/omny/%1$s.json", messageId), readMessage(tenantId, messageId));
    }

    @Then("^email (\\d+) sent was a welcome email from '([\\w\\.@-]*)'$")
    public void email_sent_was_a_welcome_email(int messageIndex, String from) throws Throwable {
        WiserMessage msg = wiser.getMessages().get(messageIndex);
        assertNotNull(msg);
        assertTrue(msg.getMimeMessage().getSubject().contains("Welcome"));
        assertEquals(from, msg.getMimeMessage().getHeader("From", null));
    }

    @Then("^user profile exists for '([\\w\\.@-]*)'$")
    public void user_profile_exists_for_user(String username) throws Throwable {
        ResponseResults response = executeGet(String.format("/users/%1$s", username));
        response.statusCodeIs(HttpStatus.OK);
    }

    @When("^an 'omny\\.deregistration' message is submitted for tenant '([\\w]*)'$")
    public void an_deregistration_message_is_submitted_for_tenant(String tenantId) throws Throwable {
        IntegrationTestSupport.tenantId = tenantId;
        String messageId = "omny.deregistration";
        executePost(String.format("/msg/omny/%1$s.json", messageId), readMessage(tenantId, messageId));

    }

    @Then("^user profile does not exist for '([\\w\\.@-]*)'$")
    public void user_profile_does_not_exist_for_user(String username) throws Throwable {
        try {
            executeGet(String.format("/users/%1$s", username));
            fail("Still able to access profile of "+username);
        } catch (ResourceAccessException e) {
            ; // expected
        }
    }

}
