package link.omny.custmgmt.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;

import link.omny.custmgmt.model.Contact;

import org.apache.commons.io.input.BOMInputStream;
import org.junit.Test;

public class CsvImporterTest {

    private CsvImporter importer = new CsvImporter();

    @Test
    public void testImportMixedContactAndAccount() {
        InputStream is = null;
        String[] headers = { "account.name", "address1", "address2",
                "countyOrCity", "postCode", "phone1", "fullName", "doNotEmail",
                "email", "facsimile", "account.pCode", "account.doctors" };
        try {

            is = getClass().getResourceAsStream(
                    "/data/contacts-accounts-and-custom.csv");
            final Reader in = new InputStreamReader(new BOMInputStream(is),
                    "UTF-8");

            List<Contact> contacts = importer.readContacts(in, headers);
            assertEquals(3, contacts.size());
            Contact contact = contacts.get(1);
            // assertEquals("M i c h e l l e   K e l l y", contact.getFullName()
            // .toString());
            // assertTrue(" 4   O s w a l d   R o a d ".equals(contact
            // .getAddress1()));
            // assertEquals(" 4   O s w a l d   R o a d ",
            // contact.getAddress1());
            // assertEquals("C h o r l t o n ",
            // contact.getAddress2().toString());

            // assertEquals("m i c h e l l e . k e l l y @ n h s . n e t ",
            // contact.getEmail());
            assertNotNull(contact.getEmail());
            assertEquals(Boolean.FALSE, contact.isDoNotEmail());
            System.out.println("  custom:" + contact.getCustomFields());
            assertNotNull(contact.getCustomFieldValue("facsimile"));

            // check Account
            // assertEquals(" O s w a l d   M e d i c a l   P r a c t i c e ",
            // contact.getAccount().getName());
            assertNotNull(contact.getAccount().getName());
            assertNotNull(contact.getAccount().getCustomFieldValue("doctors"));
        } catch (Exception e) {
            e.printStackTrace();
            fail("exception: " + e.getMessage());
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                fail(e.getMessage());
            }
        }
    }
}
