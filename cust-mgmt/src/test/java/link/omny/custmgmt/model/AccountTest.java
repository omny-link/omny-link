package link.omny.custmgmt.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Collections;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.junit.BeforeClass;
import org.junit.Test;

public class AccountTest {

    private static Validator validator;

    @BeforeClass
    public static void setUpClass() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    public void testMergeCustomFields() {
        Account Account = new Account();
        CustomAccountField field1 = new CustomAccountField("field1", "foo");
        field1.setId(1l);
        Account.addCustomField(field1);

        CustomAccountField field2 = new CustomAccountField("field1", "foo");
        assertNull(field2.getId());
        
        Account.setCustomFields(Collections.singletonList(field2));
        
        assertEquals(1, Account.getCustomFields().size());
        assertEquals(field1.getId(), Account.getCustomFields().get(0).getId());
    }

    @Test
    public void testConvertExponentForm() {
        Account Account = new Account();
        CustomAccountField field1 = new CustomAccountField("exponentValue",
                "1.12415E7");
        field1.setId(1l);
        Account.addCustomField(field1);

        Account.setCustomFields(Collections.singletonList(field1));

        assertEquals(1, Account.getCustomFields().size());
        assertEquals("11241500.00", Account.getCustomFields().get(0)
                .getValue());
    }

    @Test
    public void testExponentFormNotAffectStrings() {
        Account Account = new Account();
        CustomAccountField field1 = new CustomAccountField("exponentValue",
                "N/A");
        field1.setId(1l);
        Account.addCustomField(field1);

        Account.setCustomFields(Collections.singletonList(field1));

        assertEquals(1, Account.getCustomFields().size());
        assertEquals("N/A", Account.getCustomFields().get(0).getValue());
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
    public void testNorthernIrishCompanyNumber() {
        Account acct = new Account();
        acct.setCompanyNumber("NI016970");

        Set<ConstraintViolation<Account>> violations = validator
                .validateProperty(acct, "companyNumber");
        assertEquals(0, violations.size());
    }

    @Test
    public void testEdubaseUrn() {
        Account acct = new Account();
        acct.setCompanyNumber("136630");

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

}
