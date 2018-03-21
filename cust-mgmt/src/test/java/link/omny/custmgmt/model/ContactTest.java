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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.Scanner;

import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

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

        contact.addCustomField(field2);
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
    public void testCalculatedActivityFields() {
        Contact contact = new Contact();
        assertEquals(null, contact.getLastActivityOfType(ActivityType.LOGIN));
    }

    @Test
    public void testParseJsonContactWithOptIn() {
        String jsonInString = readFromClasspath("/omny.enquiry.json");
        assertNotNull(jsonInString);

        ObjectMapper mapper = new ObjectMapper();

        try {
            Contact contact = mapper.readValue(jsonInString, Contact.class);

            assertNotNull(contact);
            assertEquals("Bart", contact.getFirstName());
            assertEquals("Simpson", contact.getLastName());
            assertEquals("google", contact.getSource());
            assertEquals(true, contact.getEmailOptIn());

        } catch (IOException e) {
            e.printStackTrace();
            fail("Unable to parse JSON");
        }
    }

    @Test
    public void testParseJsonContactWithOptOut() {
        String jsonInString = readFromClasspath("/omny.enquiry-opt-out.json");
        assertNotNull(jsonInString);

        ObjectMapper mapper = new ObjectMapper();

        try {
            Contact contact = mapper.readValue(jsonInString, Contact.class);

            assertNotNull(contact);
            assertEquals("Bart", contact.getFirstName());
            assertEquals("Simpson", contact.getLastName());
            assertEquals("google", contact.getSource());
            assertEquals(false, contact.getEmailOptIn());

        } catch (IOException e) {
            e.printStackTrace();
            fail("Unable to parse JSON");
        }
    }

    @Test
    public void testParseJsonContactWithoutOptIn() {
        String jsonInString = readFromClasspath("/omny.enquiry-without-opt-in.json");
        assertNotNull(jsonInString);

        ObjectMapper mapper = new ObjectMapper();

        try {
            Contact contact = mapper.readValue(jsonInString, Contact.class);

            assertNotNull(contact);
            assertEquals("Bart", contact.getFirstName());
            assertEquals("Simpson", contact.getLastName());
            assertEquals("google", contact.getSource());
            assertEquals(null, contact.getEmailOptIn());

        } catch (IOException e) {
            e.printStackTrace();
            fail("Unable to parse JSON");
        }
    }

    @Test
    public void testToCsv() throws IOException {
        Date now = new Date();
        Contact contact = new Contact();
        contact.setId(1l);
        contact.setAccountId(1l);
        contact.setFirstName("Fred");
        contact.setLastName("Flintstone");
        contact.addNote(new Note(1l, "tim@knowprocess.com", now,
                "A single-line note", true, false));
        contact.addNote(new Note(2l, "tim@knowprocess.com", now,
                "A note\nthat spans multiple lines", true, false));
        assertEquals(2,  contact.getNotes().size());
        System.out.println(contact.toCsv());
        String csv = contact.toCsv();
        assertTrue(csv.startsWith("1,1,Fred,Flintstone,"));
        assertTrue(csv.contains("tim@knowprocess.com: A single-line note"));
        assertTrue(csv.contains("tim@knowprocess.com: A note\n"
                + "that spans multiple lines;"));
    }

    private static String readFromClasspath(String resourceName) {
        try (Scanner scanner = new Scanner(ContactTest.class.getResourceAsStream(resourceName))) {
            return scanner.useDelimiter("\\A").next();
        } catch (Exception e) {
            throw e;
        }
    }
}
