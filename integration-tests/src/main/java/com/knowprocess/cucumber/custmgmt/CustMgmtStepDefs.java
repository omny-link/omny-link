package com.knowprocess.cucumber.custmgmt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.subethamail.wiser.WiserMessage;

import com.knowprocess.cucumber.IntegrationTestSupport;

import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

public class CustMgmtStepDefs extends IntegrationTestSupport {

    @When("^an 'omny\\.enquiry' message including contact info is submitted for tenant '([\\w]*)'$")
    public void an_omny_enquiry_message_inc_contact_is_submitted_for_tenant(String tenantId) throws Throwable {
        IntegrationTestSupport.tenantId = tenantId;
        String messageId = "omny.enquiry";
        executePost(String.format("/msg/%1$s/%2$s.json", tenantId, messageId), readMessage(tenantId, messageId));
    }

    @When("^an 'omny\\.enquiry' message including contact, account and order is submitted for tenant '([\\w]*)'$")
    public void a_message_inc_contact_and_account_is_submitted_for_tenant(String tenantId) throws Throwable {
        IntegrationTestSupport.tenantId = tenantId;
        String messageId = "omny.enquiry";
        executePost(String.format("/msg/%1$s/%2$s.json", tenantId, messageId), readMessage(tenantId, messageId));
    }

    @Then("^email (\\d+) sent was a thanks for your enquiry from '([\\w\\.@-]*)' containing '([\\w\\. -]*)' in the subject$")
    public void a_thanks_for_your_enquiry_email_was_sent(int messageIndex, String from, String subject) throws Throwable {
        // activity: Send memo to enquirer
        WiserMessage msg = wiser.getMessages().get(messageIndex);
        assertNotNull(msg);
        assertTrue(msg.getMimeMessage().getSubject().contains(subject));
        assertEquals(from, msg.getMimeMessage().getHeader("From", null));
    }

    @Then("^email (\\d+) sent was an enquiry notification from '([\\w\\.@-]*)'$")
    public void an_enquiry_notification_email_was_sent(int messageIndex, String from) throws Throwable {
        // activity: Notify internal users
        WiserMessage msg = wiser.getMessages().get(messageIndex);
        assertNotNull(msg);
        assertTrue(msg.getMimeMessage().getSubject().contains("New Enquiry"));
        assertEquals(from, msg.getMimeMessage().getHeader("From", null));
    }

    @When("^a '([\\w]*)' action is submitted for contact '([\\w]*)' of tenant '([\\w]*)'$")
    public void an_action_is_submitted_for_contact_of_tenant(String action, String contactLocalId, String tenantId) throws Throwable {
        IntegrationTestSupport.tenantId = tenantId;
        executePost(String.format("/%1$s/process-instances/", tenantId), 
                readMessage(tenantId, action), false);
    }

    @Then("^a contact alert email was sent$")
    public void a_contact_alert_email_was_sent() throws Throwable {
        WiserMessage msg = wiser.getMessages().get(0);
        assertNotNull(msg);
        assertTrue(msg.getMimeMessage().getSubject().contains("Omny Link: A contact needs your attention"));
    }
}
