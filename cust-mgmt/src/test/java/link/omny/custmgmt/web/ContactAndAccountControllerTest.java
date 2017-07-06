package link.omny.custmgmt.web;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import link.omny.custmgmt.Application;
import link.omny.custmgmt.model.Account;
import link.omny.custmgmt.model.Contact;
import link.omny.custmgmt.model.CustomContactField;
import link.omny.custmgmt.model.CustomField;
import link.omny.custmgmt.repositories.ContactRepository;
import link.omny.custmgmt.repositories.CustomContactFieldRepository;
import link.omny.custmgmt.web.ContactController.ContactResource;
import link.omny.custmgmt.web.ContactController.ShortContact;

/**
 * @author Tim Stephenson
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
public class ContactAndAccountControllerTest {

    private static final String TENANT_ID = "test";

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ContactRepository contactRepo;

    @Autowired
    private CustomContactFieldRepository customContactRepo;

    @Autowired
    private ContactController contactController;

    @Autowired
    private AccountController acctController;

    private Long contactId;

    private long acctId;

    @After
    public void tearDown() {
        contactController.delete(TENANT_ID, contactId);
        // check clean
        List<ShortContact> list = contactController.listForTenantAsJson(
                TENANT_ID, null, null, null);
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
        contactId = Long.parseLong(locationHdrs.get(0).substring(
                locationHdrs.get(0).lastIndexOf('/') + 1));

        Account acct = getAccount();
        ResponseEntity<?> acctResp = acctController.create(TENANT_ID, acct);
        assertEquals(HttpStatus.CREATED, acctResp.getStatusCode());
        locationHdrs = contactResp.getHeaders().get("Location");
        assertEquals(1, locationHdrs.size());
        assertNotNull(locationHdrs.get(0));
        acctId = Long.parseLong(locationHdrs.get(0).substring(
                locationHdrs.get(0).lastIndexOf('/') + 1));

        contactController.setAccount(TENANT_ID, contactId, "/accounts/"
                + acctId);

        Contact contactResults = contactController.findById(
                TENANT_ID, contact.getId().toString());
        assertContactEquals(contact, acct, contactResults);

        Contact contact2 = contactRepo.findOne(contactId);
        assertNotNull(contact2.getFirstContact());
        Date lastUpdated = contact2.getLastUpdated();
        assertNotNull(lastUpdated); // Set when account linked

        // SIMULATE REST UPDATE BEHAVIOUR
        contact2.setAccount(null);
        contactController.update(TENANT_ID, contactId, contact2);
        contact2 = contactRepo.findOne(contactId);
        assertNotNull("Update has de-linked contact and account",
                contact2.getAccount());
        assertEquals(acct.getName(), contact2.getAccount().getName());
        assertTrue(lastUpdated.getTime() <= contact2.getLastUpdated().getTime());

        // FETCH ALL CONTACTS
        List<ShortContact> contacts = contactController.listForTenantAsJson(
                TENANT_ID, null, null, null);
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

        Contact contactResults = contactController.findById(
                TENANT_ID, contact.getId().toString());
        assertContactEquals(contact, acct, contactResults);

        Contact contact2 = contactRepo.findOne(contactId);
        assertNotNull(contact2.getFirstContact());
        // TODO this should be null but is not
        // assertNull(contact2.getLastUpdated());

        List<CustomContactField> customFields = customContactRepo
                .findByContactId(contactId);
        for (CustomField field : customFields) {
            if ("eyeColor".equals(field.getName())) {
                assertEquals(contact.getCustomFieldValue("eyeColor"),
                        field.getValue());
            }
        }

        // UPDATE CONTACT
        contact.setFirstName(contact.getFirstName() + "Updated");
        contact.setLastName(contact.getLastName() + "Updated");
        contact.setEmail(contact.getEmail() + "Updated");
        acct.setName(acct.getName() + "Updated");
        contactController.update(TENANT_ID, contactId, contact);

        // CHECK UPDATED CONTACT
        List<ContactResource> contactList = contactController.searchByEmail(TENANT_ID,
                contact.getEmail());
        assertEquals(1, contactList.size());
        assertContactEquals(contact, acct, contactList.get(0));

        contact2 = contactRepo.findOne(contactId);
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
    
    private void assertContactEquals(Contact contact, Account acct,
            ContactResource contactResource) {
        assertEquals(contact.getFullName(), contactResource.getFullName());
        assertEquals(contact.getEmail(), contactResource.getEmail());
        assertEquals(1, contactResource.getCustomFields().size());
        assertEquals("blue",
                contactResource.getCustomFieldValue("eyeColor"));
        assertEquals(acct.getName(), contactResource.getAccount().getName());
    }

    protected Contact getContact() throws IOException {
        String contactJson = "{\"firstName\":\"Fred\","
                + "\"lastName\":\"Flintstone\","
                + "\"email\":\"fred@bedrockslateandgravel.com\","
                + "\"customFields\":{ \"eyeColor\": \"blue\" }}";

        Contact contact = objectMapper.readValue(contactJson,
                new TypeReference<Contact>() {
                });
        assertNotNull(contact);
        assertEquals("Fred", contact.getFirstName());
        assertEquals("Flintstone", contact.getLastName());
        assertEquals("fred@bedrockslateandgravel.com", contact.getEmail());
        assertEquals("blue", contact.getCustomFieldValue("eyeColor"));

        return contact;
    }

    protected Account getAccount() throws IOException {
        String acctJson = "{\"name\":\"trademark\","
                + "\"companyNumber\":null,\"aliases\":null,"
                + "\"businessWebsite\":\"\",\"shortDesc\":null,"
                + "\"description\":\"test\",\"incorporationYear\":null,"
                + "\"noOfEmployees\":null,\"tenantId\":\"firmgains\","
                + "\"firstContact\":null,\"lastUpdated\":null,"
                + "\"customFields\":{}}";

        Account acct = objectMapper.readValue(acctJson,
                new TypeReference<Account>() {
                });
        assertNotNull(acct);

        return acct;
    }
}
