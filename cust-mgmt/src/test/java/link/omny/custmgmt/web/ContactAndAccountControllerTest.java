/*******************************************************************************
 *Copyright 2015-2018 Tim Stephenson and contributors
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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;

import link.omny.custmgmt.Application;
import link.omny.custmgmt.model.Account;
import link.omny.custmgmt.model.Contact;
import link.omny.custmgmt.model.CustomContactField;

/**
 * @author Tim Stephenson
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = Application.class)
@WebAppConfiguration
public class ContactAndAccountControllerTest {

    private static final String TENANT_ID = "test";

    @Autowired
    private ContactController contactController;

    @Autowired
    private AccountController acctController;

    private Long contactId;

    private long acctId;

    @AfterEach
    public void tearDown() {
        contactController.delete(TENANT_ID, contactId);
        // check clean
        List<EntityModel<Contact>> list = contactController.listForTenantAsJson(
                TENANT_ID, null, null, false);
        assertEquals(0, list.size());
    }

    @Test
    public void testLifecycle() throws IOException {
        Contact contact = getContact();
        ResponseEntity<?> contactResp = contactController.create(TENANT_ID,
                contact);
        assertEquals(HttpStatus.CREATED, contactResp.getStatusCode());
        List<String> locationHdrs = contactResp.getHeaders().get("Location");
        assertEquals(1, locationHdrs.size());
        assertNotNull(locationHdrs.get(0));
		// TODO migrate to http://host/tenant/contacts/id
		// assertThat(locationHdrs.get(0), containsString(TENANT_ID + "/contacts/"));
        contactId = Long.parseLong(locationHdrs.get(0).substring(
                locationHdrs.get(0).lastIndexOf('/') + 1));

        Account acct = getAccount();
        ResponseEntity<?> acctResp = acctController.create(TENANT_ID, acct);
        assertEquals(HttpStatus.CREATED, acctResp.getStatusCode());
        locationHdrs = acctResp.getHeaders().get("Location");
        assertEquals(1, locationHdrs.size());
        assertNotNull(locationHdrs.get(0));
        // TODO
//        assertThat(locationHdrs.get(0), containsString(TENANT_ID + "/accounts/"));
        acctId = Long.parseLong(locationHdrs.get(0).substring(
                locationHdrs.get(0).lastIndexOf('/') + 1));

        contactController.setAccount(TENANT_ID, contactId, "/accounts/"
                + acctId);

        EntityModel<Contact> contactModel = contactController.findEntityById(
                TENANT_ID, contact.getId());
        assertContactEquals(contact, acct, contactModel.getContent());

        Contact contact2 = contactController.findById(TENANT_ID, contactId);
        assertNotNull(contact2.getFirstContact());
        Date lastUpdated = contact2.getLastUpdated();
        assertNotNull(lastUpdated); // Set when account linked

        // SIMULATE REST UPDATE BEHAVIOUR
        contact2.setAccount(null);
        contactController.update(TENANT_ID, contactId, contact2);
        contact2 = contactController.findById(TENANT_ID, contactId);
        assertNotNull(contact2.getAccount(), "Update has de-linked contact and account");
        assertEquals(acct.getName(), contact2.getAccount().getName());
        assertTrue(lastUpdated.getTime() <= contact2.getLastUpdated().getTime());

        // FETCH ALL CONTACTS
        List<EntityModel<Contact>> contacts = contactController.listForTenantAsJson(
                TENANT_ID, null, null, false);
        assertEquals(1, contacts.size());
        // assertContactEquals(contact, acct, contacts);
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

        ResponseEntity<?> contactResp = contactController.create(TENANT_ID,
                contact);
        assertEquals(HttpStatus.CREATED, contactResp.getStatusCode());
        List<String> locationHdrs = contactResp.getHeaders().get("Location");
        assertEquals(1, locationHdrs.size());
        assertNotNull(locationHdrs.get(0));
        contactId = Long.parseLong(locationHdrs.get(0).substring(
                locationHdrs.get(0).lastIndexOf('/') + 1));

        EntityModel<Contact> contactModel = contactController.findEntityById(
                TENANT_ID, contact.getId());
        assertContactEquals(contact, acct, contactModel.getContent());

        Contact contact2 = contactController.findById(TENANT_ID, contactId);
        assertNotNull(contact2.getFirstContact());

        // UPDATE CONTACT
        contact.setFirstName(contact.getFirstName() + "Updated");
        contact.setLastName(contact.getLastName() + "Updated");
        contact.setEmail(contact.getEmail() + "Updated");
        acct.setName(acct.getName() + "Updated");
        contactController.update(TENANT_ID, contactId, contact);

        // CHECK UPDATED CONTACT
        List<EntityModel<Contact>> contactList = contactController.findByEmail(TENANT_ID,
                contact.getEmail());
        assertEquals(1, contactList.size());
        assertContactEquals(contact, acct, contactList.get(0).getContent());

        contact2 = contactController.findById(TENANT_ID, contactId);
        assertNotNull(contact2.getFirstContact());
        assertNotNull(contact2.getLastUpdated());
    }

    private void assertContactEquals(Contact contact, Account acct,
            Contact contactResults) {
        assertEquals(contact.getFullName(), contactResults.getFullName());
        assertEquals(contact.getEmail(), contactResults.getEmail());
        assertEquals(1, contactResults.getCustomFields().size());
        assertEquals("blue",
                contactResults.getCustomFieldValue("eyeColor"));
        assertEquals(acct.getName(), contactResults.getAccount().getName());
    }

    protected Contact getContact() throws IOException {
        return new Contact()
                .setFirstName("Fred")
                .setLastName("Flintstone")
                .setEmail("fred@bedrockslateandgravel.com")
                .addCustomField(new CustomContactField("eyeColor", "blue"));
    }

    protected Account getAccount() throws IOException {
        return new Account()
                .setName("trademark")
                .setDescription("test")
                .setTenantId("client1");
    }
}
