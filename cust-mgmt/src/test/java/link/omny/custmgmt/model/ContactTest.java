package link.omny.custmgmt.model;

import static org.junit.Assert.assertEquals;

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

}
