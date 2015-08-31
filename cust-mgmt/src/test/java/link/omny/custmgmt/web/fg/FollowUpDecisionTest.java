package link.omny.custmgmt.web.fg;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import link.omny.custmgmt.model.Account;
import link.omny.custmgmt.model.Activity;
import link.omny.custmgmt.model.Contact;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.knowprocess.mail.MailData;

//@RunWith(SpringJUnit4ClassRunner.class)
//@SpringApplicationConfiguration(classes = Application.class)
public class FollowUpDecisionTest {

    private static final String ADDRESSEE = "tim@knowprocess.com";
    private FollowUpDecision decision;

    private ObjectMapper mapper = new ObjectMapper();

    @Before
    public void setUp() throws Exception {
        decision = new FollowUpDecision();
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testFollowUpBrandNewValuation() {
        Contact contact = getContact();
        contact.getActivities().add(
                new Activity(FollowUpDecision.ACTIVITY_VALUATION, new Date()));

        MailData mailData = decision.execute(contact);

        assertTrue(mailData == null);
    }

    @Test
    public void testFollowUpRecentValuation() {
        Contact contact = getContact();
        contact.getActivities().add(
                new Activity(FollowUpDecision.ACTIVITY_VALUATION,
                        getYesterday()));

        assertMailData(decision.execute(contact), "valuation-detail",
                "About Your Business Valuation: Reasonable or Risible?");

        // run again immediately should not generate another mail
        contact.getActivities().add(
                new Activity(FollowUpDecision.ACTIVITY_EMAIL, new Date(),
                        "Template: valuation-detail\nSubject: About Your Business Valuation: Reasonable or Risible?"));
        assertTrue(decision.execute(contact) == null);
    }

    @Test
    public void testLowValuationFollowUp() {
        Contact contact = getContact();
        contact.getAccount().setField(FollowUpDecision.FIELD_MID_VALUATION,
                "99000");
        contact.getActivities().add(
                new Activity(FollowUpDecision.ACTIVITY_VALUATION,
                        get8DaysAgo()));
        contact.getActivities().add(
                new Activity("email", get8DaysAgo(), "valuation-detail"));

        assertMailData(decision.execute(contact), "low-valuation-email",
                "We’ve taken a close look at your Firm Gains valuation");
    }

    @Test
    public void testMidValuationFollowUp() {
        Contact contact = getContact();
        contact.getActivities().add(
                new Activity(FollowUpDecision.ACTIVITY_VALUATION,
                        get8DaysAgo()));
        contact.getActivities().add(
                new Activity("email", get8DaysAgo(), "valuation-detail"));

        assertMailData(decision.execute(contact), "mid-valuation-email",
                "We’ve taken a close look at your Firm Gains valuation");
    }

    @Test
    public void testHighValuationFollowUp() {
        Contact contact = getContact();
        contact.getAccount().setField(FollowUpDecision.FIELD_MID_VALUATION,
                "600000");
        contact.getActivities().add(
                new Activity(FollowUpDecision.ACTIVITY_VALUATION,
                        get8DaysAgo()));
        contact.getActivities().add(
                new Activity("email", get8DaysAgo(), "valuation-detail"));

        assertMailData(decision.execute(contact), "high-valuation-email",
                "We’ve taken a close look at your Firm Gains valuation");
    }

    @Test
    public void testOlderValuationFollowUp() {
        Contact contact = getContact();
        contact.getActivities().add(
                new Activity(FollowUpDecision.ACTIVITY_VALUATION,
                        getNWeeksAgo(4)));
        contact.getActivities().add(
                new Activity("email", getNWeeksAgo(5), "valuation-detail"));
        contact.getActivities().add(
                new Activity("email", getNWeeksAgo(4), "mid-valuation-email"));

        assertMailData(decision.execute(contact), "valuation-advice",
                "Why Your Valuation Means Nothing...");
    }

    @Test
    public void testRecentBusinessPlanFollowUp() {
        Contact contact = getContact();
        contact.getActivities()
                .add(new Activity(FollowUpDecision.ACTIVITY_BIZ_PLAN,
                        getNWeeksAgo(1)));

        assertMailData(decision.execute(contact), "plan-help",
                "Get Your Business Sale Plans into Action (not inaction!)");
    }

    @Test
    public void testOlderBusinessPlanFollowUp() {
        Contact contact = getContact();
        contact.getActivities()
                .add(new Activity(FollowUpDecision.ACTIVITY_BIZ_PLAN,
                        getNWeeksAgo(6)));

        assertMailData(decision.execute(contact), "plan-next",
                "Are you Fully Equipped for Your Business Sale?");
    }

    @Test
    public void testRecentRegistrationFollowUp() {
        Contact contact = getContact();
        contact.getActivities().add(
                new Activity(FollowUpDecision.REGISTRATION, getNWeeksAgo(5)));

        assertMailData(decision.execute(contact), "discover",
                "There’s More Under the Surface with Firm Gains");
    }

    @Test
    public void testOlderRegistrationFollowUp() {
        Contact contact = getContact();
        contact.getActivities().add(
                new Activity(FollowUpDecision.REGISTRATION, getNWeeksAgo(7)));

        assertMailData(decision.execute(contact), "intro-services",
                "Every Business Owner Needs a Helping Hand");
    }

    @Test
    public void testOldRegistrationFollowUp() {
        Contact contact = getContact();
        contact.getActivities().add(
                new Activity(FollowUpDecision.REGISTRATION, getNWeeksAgo(11)));

        assertMailData(decision.execute(contact), "business-sale-ideas",
                "What Makes a ‘Good’ Business Sale?");
    }

    @Test
    public void testAncientRegistrationFollowUp() {
        Contact contact = getContact();
        contact.getActivities().add(
                new Activity(FollowUpDecision.REGISTRATION, getAYearAgo()));

        assertMailData(decision.execute(contact), "anniversary",
                "A Very Happy Anniversary... We Hope!");
    }

    @Test
    public void testMidValuationFollowUpFromJson() throws JsonParseException,
            JsonMappingException, IOException {
        InputStream testData = getClass().getResourceAsStream(
                "/data/FollowUpDecisionTest.contact1.json");
        assertNotNull("Cannot find expected test data", testData);
        Contact contact = mapper.readValue(
                testData,
                Contact.class);

        assertMailData(decision.execute(contact), "mid-valuation-email",
                "We’ve taken a close look at your Firm Gains valuation");
    }

    private void assertMailData(MailData mailData, String template,
            String subject) {
        assertNotNull(mailData);
        assertEquals(template, mailData.get("template"));
        assertEquals(subject, mailData.get("subject"));
        assertEquals(ADDRESSEE, mailData.get("contact.email"));
    }

    private Contact getContact() {
        Contact contact = new Contact();
        contact.setFirstName("Tim");
        contact.setLastName("Stephenson");
        contact.setEmail(ADDRESSEE);

        Account account = new Account();
        account.setField(FollowUpDecision.FIELD_MID_VALUATION, "599000");
        contact.setAccount(account);
        return contact;
    }

    private Date getYesterday() {
        Calendar cal = new GregorianCalendar();
        cal.roll(Calendar.DATE, false);
        return cal.getTime();
    }

    private Date get8DaysAgo() {
        Calendar cal = new GregorianCalendar();
        cal.roll(Calendar.DATE, -8);
        return cal.getTime();
    }

    private Date getNWeeksAgo(int noOfWeeks) {
        Calendar cal = new GregorianCalendar();
        cal.roll(Calendar.WEEK_OF_YEAR, -noOfWeeks);
        return cal.getTime();
    }

    private Date getAYearAgo() {
        Calendar cal = new GregorianCalendar();
        cal.roll(Calendar.YEAR, -1);
        return cal.getTime();
    }
}
