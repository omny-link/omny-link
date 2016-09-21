package link.omny.custmgmt.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Scanner;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.junit.BeforeClass;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.knowprocess.bpm.model.ProcessDefinition;

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
    public void testUK7DigitCompanyNumber() {
        Account acct = new Account();
        acct.setCompanyNumber("1234567");

        Set<ConstraintViolation<Account>> violations = validator
                .validateProperty(acct, "companyNumber");
        assertEquals(0, violations.size());
        assertEquals("01234567", acct.getCompanyNumber());
    }

    @Test
    public void testUK6DigitCompanyNumber() {
        Account acct = new Account();
        acct.setCompanyNumber("123456");

        Set<ConstraintViolation<Account>> violations = validator
                .validateProperty(acct, "companyNumber");
        assertEquals(0, violations.size());
        assertEquals("00123456", acct.getCompanyNumber());
    }

    @Test
    public void testScottishCompanyNumber() {
        Account acct = new Account();
        acct.setCompanyNumber("SC345678");

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

        c1.setCustomField(new CustomContactField("favouriteColour", "orange"));
        c2.setCustomField(new CustomContactField("favouriteColour", "orange"));

        CustomContactField colour1 = c1.getCustomFields().get(0);
        colour1.setContact(c1);
        colour1.hashCode();
        c2.getCustomFields().get(0).setContact(c2);

        c1.hashCode();
        assertEquals(c1, c2);
    }

    @Test
    public void testMergeCustomFields() {
        Contact contact = new Contact();
        CustomContactField field1 = new CustomContactField("field1", "foo");
        field1.setId(1l);
        contact.addCustomField(field1);

        CustomContactField field2 = new CustomContactField("field1", "foo");
        assertNull(field2.getId());

        contact.setCustomFields(Collections.singletonList(field2));

        assertEquals(1, contact.getCustomFields().size());
        assertEquals(field1.getId(), contact.getCustomFields().get(0).getId());
    }

    @Test
    public void testIsLastNameDefault() {
        Contact contact = new Contact();
        assertTrue(contact.isLastNameDefault());

        contact.setLastName("Simpson");
        assertTrue(!contact.isLastNameDefault());
    }

    @Test
    public void testIsFirstNameDefault() {
        Contact contact = new Contact();
        assertTrue(contact.isFirstNameDefault());

        contact.setFirstName("Bart");
        assertTrue(!contact.isFirstNameDefault());
    }

    @Test
    public void testSetUtmFields() {
        Contact contact = new Contact();

        assertNull(contact.getSource());
        assertNull(contact.getMedium());
        assertNull(contact.getCampaign());
        assertNull(contact.getKeyword());

        contact.setUtm_source("Google");
        contact.setUtm_medium("CPC");
        contact.setUtm_campaign("Find a business");
        contact.setUtm_keyword("Self-employment");

        assertEquals("Google", contact.getSource());
        assertEquals("CPC", contact.getMedium());
        assertEquals("Find a business", contact.getCampaign());
        assertEquals("Self-employment", contact.getKeyword());
    }

    @Test
    public void testParseJsonContact() {
        String jsonInString = readFromClasspath("/omny.enquiry.json");
        assertNotNull(jsonInString);

        ObjectMapper mapper = new ObjectMapper();

        try {
            Contact contact = mapper.readValue(jsonInString, Contact.class);

            assertNotNull(contact);
            assertEquals("Bart", contact.getFirstName());
            assertEquals("Simpson", contact.getLastName());
            assertEquals("google", contact.getSource());
            assertEquals("google", contact.getSource());

        } catch (IOException e) {
            e.printStackTrace();
            fail("Unable to parse JSON");
        }
    }

    private static String readFromClasspath(String resourceName) {
        InputStream is = null;
        try {
            is = ProcessDefinition.class.getResourceAsStream(resourceName);
            return new Scanner(is).useDelimiter("\\A").next();
        } catch (Exception e) {
            throw e;
        }
    }
}
