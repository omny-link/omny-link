package com.knowprocess.cucumber.support;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.subethamail.wiser.WiserMessage;

import com.knowprocess.cucumber.IntegrationTestSupport;

import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

public class SupportStepDefs extends IntegrationTestSupport {

    @When("^an 'omny\\.ticket' message including screenshot is submitted for tenant '([\\w]*)'$")
    public void an_omny_ticket_message_inc_contact_is_submitted_for_tenant(String tenantId) throws Throwable {
        SupportStepDefs.tenantId = tenantId;
        String messageId = "omny.ticket";
        executePost(String.format("/msg/%1$s/%2$s.json", tenantId, messageId), readMessage(tenantId, messageId));
    }

    @Then("^a thanks for the ticket email was sent$")
    public void a_thanks_for_the_ticket_email_was_sent() throws Throwable {
        System.out.println("Messages: "+wiser.getMessages().size());
        WiserMessage msg = wiser.getMessages().get(2);
        assertNotNull(msg);
        System.out.println("subject: "+msg.getMimeMessage().getSubject());
        assertTrue(msg.getMimeMessage().getSubject().contains("[Omny Link] Thanks for your support ticket"));
    }

    @Then("^a support case notification email was sent$")
    public void a_support_case_notification_email_was_sent() throws Throwable {
        WiserMessage msg = wiser.getMessages().get(0);
        assertNotNull(msg);
        System.out.println("subject: "+msg.getMimeMessage().getSubject());
        assertTrue(msg.getMimeMessage().getSubject().contains("New support ticket"));
    }
}
