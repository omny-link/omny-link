package link.omny.custmgmt.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

public class ContactTest {


    @Test
    public void testSettingFullName() {
        Contact contact = new Contact();
        contact.setFullName("Tim Stephenson");
        assertEquals("Tim", contact.getFirstName());
        assertEquals("Stephenson", contact.getLastName());

        contact.setFullName("James Sansome-Standerwick");
        assertEquals("James", contact.getFirstName());
        assertEquals("Sansome-Standerwick", contact.getLastName());

        contact.setFullName("Mr Sandhu");
        assertEquals("Mr", contact.getTitle());
        assertEquals("Sandhu", contact.getLastName());

        contact.setFullName("Mr W Bell");
        assertEquals("Mr", contact.getTitle());
        assertEquals("W", contact.getFirstName());
        assertEquals("Bell", contact.getLastName());

        contact.setLastName(null);
        contact.setFullName("??");
        assertEquals("??", contact.getFirstName());
        assertEquals(null, contact.getLastName());

        // This is obviously not a desirable behaviour but for now is a
        // tolerable edge case
        contact.setFullName("Mr and Mrs Rutherford");
        assertEquals("Mr", contact.getTitle());
        assertEquals("and", contact.getFirstName());
        assertEquals("Mrs Rutherford", contact.getLastName());
    }

    @Test
    public void testGettingIncompleteFullName() {
        Contact contact = new Contact();
        contact.setFirstName("Tim");
        assertEquals("Tim", contact.getFirstName());
        assertEquals("Tim", contact.getFullName());

        try {
            String s = contact.toString();
            assertTrue(!s.contains("fullName"));
        } catch (Exception e) {
            fail(e.getClass().getName() + ":" + e.getMessage());
        }
    }

    @Test
    public void testEmailConfirmation() {
        Contact contact = new Contact("Fred", "Flintstone",
                "fred@bedrockslate.com", "omny");
        assertTrue(!contact.isEmailConfirmed());

        String code = contact.getEmailConfirmationCode();
        assertNotNull(code);

        contact.confirmEmail(code);
        assertTrue(contact.isEmailConfirmed());
    }
}
