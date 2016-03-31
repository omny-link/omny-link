package link.omny.custmgmt.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.junit.BeforeClass;
import org.junit.Test;

public class ContactTest {

    private static Validator validator;

    @BeforeClass
    public static void setUpClass() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

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

    @Test
    public void testUKCompanyNumber() {
        Account acct = new Account();
        acct.setCompanyNumber("12345678");

        Set<ConstraintViolation<Account>> violations = validator
                .validateProperty(acct, "companyNumber");
        assertEquals(0, violations.size());
    }

    @Test
    public void testUKPartnershipNumber() {
        Account acct = new Account();
        acct.setCompanyNumber("OC345678");

        Set<ConstraintViolation<Account>> violations = validator
                .validateProperty(acct, "companyNumber");
        assertEquals(0, violations.size());

        acct.setCompanyNumber("CO345678");
        violations = validator.validateProperty(acct, "companyNumber");
        assertEquals(1, violations.size());
    }

    @Test
    public void testHashCodeForStackOverflow() {
        Contact c1 = new Contact("Fred", "Flintstone",
                "fred@bedrockslateandgravel.com", "omny");
        Contact c2 = new Contact("Fred", "Flintstone",
                "fred@bedrockslateandgravel.com", "omny");
        assertEquals(c1, c2);

        c1.setField("favouriteColour", "orange");
        c2.setField("favouriteColour", "orange");

        CustomContactField colour1 = c1.getCustomFields().get(0);
        colour1.setContact(c1);
        colour1.hashCode();
        c2.getCustomFields().get(0).setContact(c2);

        c1.hashCode();
        assertEquals(c1, c2);
    }
}
