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
package link.omny.custmgmt.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Collections;
import java.util.Date;
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
        Account account = new Account();
        CustomAccountField field1 = new CustomAccountField("field1", "foo");
        field1.setId(1l);
        account.addCustomField(field1);

        CustomAccountField field2 = new CustomAccountField("field1", "foo");
        assertNull(field2.getId());
        
        account.setCustomFields(Collections.singletonList(field2));
        assertEquals(1, account.getCustomFields().size());
        assertEquals(field1.getId(), account.getCustomFields().get(0).getId());
        
        account.addCustomField(field2);
        assertEquals(1, account.getCustomFields().size());
        assertEquals(field1.getId(), account.getCustomFields().get(0).getId());
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

    @Test
    public void testToCsv() throws IOException {
        Date now = new Date();
        Account acct = new Account();
        acct.setId(1l);
        acct.setName("ACME Inc.");
        acct.addNote(new Note(1l, "tim@knowprocess.com", now,
                "A single-line note", true, false));
        acct.addNote(new Note(2l, "tim@knowprocess.com", now,
                "A note\nthat spans multiple lines", true, false));
        assertEquals(2,  acct.getNotes().size());
        System.out.println(acct.toCsv());
        String csv = acct.toCsv();
        assertTrue(csv.startsWith("1,ACME Inc."));
        assertTrue(csv.contains("tim@knowprocess.com: A single-line note"));
        assertTrue(csv.contains("tim@knowprocess.com: A note\n"
                + "that spans multiple lines;"));
    }
}
