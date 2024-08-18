/*******************************************************************************
 * Copyright 2015-2022 Tim Stephenson and contributors
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
package link.omny.custmgmt.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.containsString;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import link.omny.custmgmt.Application;
import link.omny.custmgmt.model.Account;
import link.omny.custmgmt.model.Contact;
import link.omny.custmgmt.model.CustomAccountField;
import link.omny.custmgmt.model.CustomContactField;
import link.omny.supportservices.model.Note;

/**
 * @author Tim Stephenson
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = Application.class)
@WebAppConfiguration
public class ContactAndAccountControllerTest {

    protected static final Logger LOGGER = LoggerFactory
            .getLogger(ContactAndAccountControllerTest.class);
    private static final String AUTHOR = "tester";
    private static final String tenantId = "test";

    @Autowired
    private ContactController contactController;

    @Autowired
    private AccountController acctController;

    @Autowired
    private ObjectMapper objectMapper;

    private Long contactId;

    private Long acctId;

    @AfterEach
    public void tearDown() {
        contactController.delete(tenantId, contactId);
        // check clean
        List<EntityModel<Contact>> list = contactController.listForTenantAsJson(
                tenantId, 0, 100, false);
        assertEquals(0, list.size());
    }

    @Test
    public void testContactLifecycle() throws IOException {
        Date start = new Date();
        Contact contact = getContact();
        Long contactId = assertContactCreation(contact);

        Account acct = getAccount();
        Long acctId = assertAccountCreation(acct);

        contactController.setAccount(tenantId, contactId, "/accounts/"
                + acctId);

        Contact contactRetrieved = retrieveContact(tenantId, contact.getId());
        assertContactEquals(contact, acct, contactRetrieved);

        Contact contact2 = contactController.findById(tenantId, contactId);
        assertNotNull(contact2.getLastUpdated()); // Set when account linked
        assertTrue(contact2.getLastUpdated().after(start));

        // SIMULATE REST UPDATE BEHAVIOUR
        contact2.setAccount(null);
        contactController.update(tenantId, contactId, contact2);
        Contact contact3 = retrieveContact(tenantId, contactId);
        assertNotNull(contact3);
        assertNotNull(contact3.getAccount(), "Update has de-linked contact and account");
        assertEquals(acct.getName(), contact3.getAccount().getName());
        assertTrue(contact3.getLastUpdated().after(contact2.getLastUpdated()));
        assertEquals(contact3.getNotes().size(), contact.getNotes().size());
        assertEquals(1, contact3.getActivities().size());

        // FETCH ALL CONTACTS
        List<EntityModel<Contact>> contacts = contactController.listForTenantAsJson(
                tenantId, 0, 100, false);
        assertEquals(1, contacts.size());
        // assertContactEquals(contact, acct, contacts);

        assertExportContactsAsCsv(contact);

        assertExportAccountsAsCsv(acct);
    }

    private void assertExportAccountsAsCsv(Account acct) {
        ResponseEntity<String> entity = acctController.listForTenantAsCsv(tenantId, 0, 10);
        String csv = entity.getBody();
        System.out.println(csv);
    }

    private void assertExportContactsAsCsv(Contact contact) {
        ResponseEntity<String> entity = contactController.listForTenantAsCsv(tenantId, 0, 10);
        String csv = entity.getBody();
        assertNotNull(csv);
        String[] lines = csv.split("\n");
        assertEquals(2, lines.length);
        assertThat(lines[1], containsString(contact.getFirstName()));
        assertThat(lines[1], containsString(contact.getLastName()));
        Note[] notes = contact.getNotes().toArray(new Note[1]);
        assertThat(lines[1], containsString(notes[0].getAuthor()));
        System.out.println(csv);
    }

    private @NonNull Long assertAccountCreation(Account acct) {
        ResponseEntity<?> acctResp = acctController.create(tenantId, acct);
        assertEquals(HttpStatus.CREATED, acctResp.getStatusCode());
        List<String> locationHdrs = acctResp.getHeaders().get("Location");
        assertEquals(1, locationHdrs.size());
        assertNotNull(locationHdrs.get(0));
        // TODO
//        assertThat(locationHdrs.get(0), containsString(TENANT_ID + "/accounts/"));
        Long acctId = Long.parseLong(locationHdrs.get(0).substring(
                locationHdrs.get(0).lastIndexOf('/') + 1));
        return acctId;
    }

    private Long assertContactCreation(Contact contact) {
        ResponseEntity<?> contactResp = contactController.create(tenantId,
                contact);
        assertEquals(HttpStatus.CREATED, contactResp.getStatusCode());
        List<String> locationHdrs = contactResp.getHeaders().get("Location");
        assertNotNull(locationHdrs);
        assertEquals(1, locationHdrs.size());
        assertNotNull(locationHdrs.get(0));
        // TODO migrate to http://host/tenant/contacts/id
        // assertThat(locationHdrs.get(0), containsString(TENANT_ID + "/contacts/"));
        contactId = Long.parseLong(locationHdrs.get(0).substring(
                locationHdrs.get(0).lastIndexOf('/') + 1));
        assertNotNull(contactId);
        return contactId;
    }

    /**
     * Test creating account first.
     *
     * @throws IOException
     */
    @Test
    public void testAccountLifecycle() throws IOException {
        Date start = new Date();
        Account acct = getAccount();
        acctId = assertAccountCreation(acct);

        Contact contact = getContact();
        Long contactId = assertContactCreation(contact);

        contactController.setAccount(tenantId, contactId, "/accounts/"
                + acctId);

        Account acct2 = acctController.findById(tenantId, acctId);
        String json = objectMapper.writeValueAsString(acct2);
        assertNotNull(json);
        assertThat("Json must contain the expected custom field", json.contains("\"budget\":"));
        acct2.setAliases("trading as");
        acctController.update(tenantId, acctId, acct2);

        // SIMULATE REST UPDATE BEHAVIOUR
        Account acct3 = acctController.findById(tenantId, acctId);
        assertNotNull(acct3);
        assertTrue(acct3.getLastUpdated().after(start));
        assertEquals(acct.getName(), acct3.getName());
        assertEquals(acct.getNotes().size(), acct3.getNotes().size());
        assertEquals(acct2.getAliases(), acct3.getAliases());

        assertExportAccountsAsCsv(acct);

        assertExportContactsAsCsv(contact);
    }

    /**
     * Test creating contact and account in one go.
     *
     * <p>
     * However note that Spring REST does not support this via the HTTP Server.
     *
     * @throws IOException
     */
    @Test
    public void testUnifiedLifecycle() throws IOException {
        Contact contact = getContact();
        Account acct = getAccount();
        contact.setAccount(acct);

        ResponseEntity<?> contactResp = contactController.create(tenantId,
                contact);
        assertEquals(HttpStatus.CREATED, contactResp.getStatusCode());
        List<String> locationHdrs = contactResp.getHeaders().get("Location");
        assertEquals(1, locationHdrs.size());
        assertNotNull(locationHdrs.get(0));
        contactId = Long.parseLong(locationHdrs.get(0).substring(
                locationHdrs.get(0).lastIndexOf('/') + 1));

        Contact retrievedContact = retrieveContact(tenantId, contact.getId());
        assertContactEquals(contact, acct, retrievedContact);

        Contact contact2 = contactController.findById(tenantId, contactId);

        // UPDATE CONTACT
        contact.setFirstName(contact.getFirstName() + "Updated");
        contact.setLastName(contact.getLastName() + "Updated");
        contact.setEmail(contact.getEmail() + "Updated");
        acct.setName(acct.getName() + "Updated");
        contactController.update(tenantId, contactId, contact);

        // CHECK UPDATED CONTACT
        List<EntityModel<Contact>> contactList = contactController.findByEmail(tenantId,
                contact.getEmail());
        assertEquals(1, contactList.size());
        assertContactEquals(contact, acct, contactList.get(0).getContent());

        contact2 = contactController.findById(tenantId, contactId);
        assertNotNull(contact2.getLastUpdated());
    }

    private void assertContactEquals(Contact contact, Account acct,
            Contact contactResults) {
        assertEquals(tenantId, contactResults.getTenantId());
        assertEquals(contact.getFullName(), contactResults.getFullName());
        assertEquals(contact.getEmail(), contactResults.getEmail());
        assertEquals(contact.getStage(), contactResults.getStage());
        assertNull(contactResults.getStage());
        assertEquals(1, contactResults.getCustomFields().size());
        assertEquals("blue",
                contactResults.getCustomFieldValue("eyeColor"));
        assertEquals(tenantId, contactResults.getAccount().getTenantId());
        assertEquals(acct.getName(), contactResults.getAccount().getName());
        // NOTE audit columns are not populated in this test harness.
    }

    protected Contact getContact() throws IOException {
        return new Contact()
                .setFirstName("Fred")
                .setLastName("Flintstone")
                .setEmail("fred@bedrockslateandgravel.com")
                .addCustomField(new CustomContactField("eyeColor", "blue"))
                .addNote(new Note(AUTHOR, "Creating new prospect"))
                .setTenantId("client1"); // Should be replaced with TENANT_ID
    }

    protected Account getAccount() throws IOException {
        return new Account()
                .setName("trademark")
                .setDescription("test")
                .addNote(new Note(AUTHOR, "Creating new prospect"))
                .addCustomField(new CustomAccountField("budget", "5000"))
                .setTenantId("client1"); // Should be replaced with TENANT_ID
    }

    private Contact retrieveContact(String tenantId, Long contactId) {
        LOGGER.info("Attempting to retrieve contact {} for {}", contactId, tenantId);
        String body = contactController.findEntityById(tenantId, contactId).getBody();
        try {
            return objectMapper.readValue(body, Contact.class);
        } catch (JsonProcessingException e) {
            LOGGER.error("cannot deserialise: {}", body);
            fail("unable to deserialise contact", e);
        }
        return null;
    }
}
