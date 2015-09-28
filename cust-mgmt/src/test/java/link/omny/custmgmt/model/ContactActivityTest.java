package link.omny.custmgmt.model;

import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.GregorianCalendar;

import org.junit.Before;
import org.junit.Test;

public class ContactActivityTest {

    private static final long ONE_WEEK = 7 * 60 * 60 * 1000;
    private Contact contact;

    @Before
    public void setUpBeforeClass() throws Exception {
        contact = new Contact();
        contact.getActivities().add(
                new Activity("valuation", new GregorianCalendar(2015, 0, 2, 13,
                        0, 0).getTime()));
        contact.getActivities().add(new Activity("register", null));
        contact.getActivities().add(
                new Activity("login", new GregorianCalendar(2015, 1, 1, 13, 0,
                        0).getTime()));
    }


    @Test
    public void testTimeSinceLogin() {
        assertTrue(contact.getTimeSinceLogin() > ONE_WEEK);
    }

    @Test
    public void testTimeSinceFirstLogin() {
        contact.getActivities().add(
                new Activity("login", new GregorianCalendar(2026, 1, 1, 13, 0,
                        0).getTime()));
        assertTrue(contact.getTimeSinceLogin() > ONE_WEEK);
    }

    @Test
    public void testHaveSentEmail() {
        assertTrue(contact.notYetSentEmail("welcome"));
        contact.getActivities().add(
                new Activity("email", new Date(), "welcome"));
        assertTrue(contact.haveSentEmail("welcome"));
    }

    @Test
    public void testTimeSinceNonExistantRegistration() {
        assertTrue(contact.getTimeSinceRegistered() == -1);
    }
}
