package link.omny.custmgmt.web.fg;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import link.omny.custmgmt.model.Activity;
import link.omny.custmgmt.model.Contact;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.knowprocess.mail.MailData;

@Component
public class FollowUpDecision {

    static final String FIELD_MID_VALUATION = "mediumQuote";
    private static final int _7_DAYS_AS_MILLIS = 1000 * 60 * 60 * 24 * 7;
    protected static final Logger LOGGER = LoggerFactory
            .getLogger(FollowUpDecision.class);
    static final String ACTIVITY_VALUATION = "valuation";
    static final String ACTIVITY_BIZ_PLAN = "business-plan";
    static final String REGISTRATION = "registration";
    static final String ACTIVITY_REGISTRATION = "registration";
    static final String ACTIVITY_EMAIL = "email";

    public MailData execute(Contact contact) {
        if (contact.getStage() != null
                && (contact.getStage().equalsIgnoreCase("Placed")
                || contact.getStage().equalsIgnoreCase("Complete")
                || contact
                        .getStage().equalsIgnoreCase("Deleted"))) {
            LOGGER.warn(String.format(
                    "ignoring contact %1$s %2$s in stage %3$s",
                    contact.getId(), contact.getFullName(), contact.getStage()));
            return null;
        }

        MailData mailData = null;

        Activity lastValuation = contact
                .getLastActivityOfType(ACTIVITY_VALUATION);
        Activity lastBizPlan = contact.getLastActivityOfType(ACTIVITY_BIZ_PLAN);
        Activity registration = contact
                .getLastActivityOfType(ACTIVITY_REGISTRATION);
        
        Calendar cal = new GregorianCalendar();
        cal.roll(Calendar.HOUR_OF_DAY, false);
        Date anHourAgo = cal.getTime();

        cal.roll(Calendar.HOUR_OF_DAY, true);
        cal.roll(Calendar.WEEK_OF_YEAR, -4);
        Date fourWeeksAgo = cal.getTime();
        cal.roll(Calendar.WEEK_OF_YEAR, -1);
        Date fiveWeeksAgo = cal.getTime();
        cal.roll(Calendar.WEEK_OF_YEAR, -1);
        Date sixWeeksAgo = cal.getTime();
        cal.roll(Calendar.WEEK_OF_YEAR, -4);
        Date tenWeeksAgo = cal.getTime();
        cal.roll(Calendar.WEEK_OF_YEAR, -1);
        Date twelveWeeksAgo = cal.getTime();
        cal.roll(Calendar.WEEK_OF_YEAR, -12);
        Date twentyFourWeeksAgo = cal.getTime();
        cal = new GregorianCalendar();
        cal.roll(Calendar.YEAR, -1);
        Date oneYearAgo = cal.getTime();
        
        if (contact.timeSinceEmail() < _7_DAYS_AS_MILLIS
                && lastValuation != null
                && lastValuation.getOccurred().before(anHourAgo)
                && !contact.haveSentEmail("valuation-detail")) {
            mailData = new MailData("valuation-detail",
                    "About Your Business Valuation: Reasonable or Risible?",
                    contact.getEmail());
        } else if (contact.timeSinceEmail() >= _7_DAYS_AS_MILLIS
                && lastValuation != null
                && lastValuation.getOccurred().before(anHourAgo)
                && Double.valueOf((String) contact.getAccount().getField(
                        FIELD_MID_VALUATION)) < 100000
                && contact.haveSentEmail("valuation-detail")
                && !contact.haveSentEmail("low-valuation-email")) {
            mailData = new MailData("low-valuation-email",
                    "We’ve taken a close look at your Firm Gains valuation",
                    contact.getEmail());
        } else if (contact.timeSinceEmail() >= _7_DAYS_AS_MILLIS
                && lastValuation != null
                && lastValuation.getOccurred().before(anHourAgo)
                && Double.valueOf((String) contact.getAccount().getField(
                        FIELD_MID_VALUATION)) >= 100000
                && Double.valueOf((String) contact.getAccount().getField(
                        FIELD_MID_VALUATION)) < 600000
                && contact.haveSentEmail("valuation-detail")
                && !contact.haveSentEmail("mid-valuation-email")) {
            mailData = new MailData("mid-valuation-email",
                    "We’ve taken a close look at your Firm Gains valuation",
                    contact.getEmail());
        } else if (contact.timeSinceEmail() >= _7_DAYS_AS_MILLIS
                && lastValuation != null
                && lastValuation.getOccurred().before(anHourAgo)
                && Double.valueOf((String) contact.getAccount().getField(
                        FIELD_MID_VALUATION)) >= 600000
                && contact.haveSentEmail("valuation-detail")
                && !contact.haveSentEmail("high-valuation-email")) {
            mailData = new MailData("high-valuation-email",
                    "We’ve taken a close look at your Firm Gains valuation",
                    contact.getEmail());
        } else if (contact.timeSinceEmail() >= _7_DAYS_AS_MILLIS
                && lastValuation != null
                && lastValuation.getOccurred().before(fourWeeksAgo)
                && contact.haveSentEmail("valuation-detail")
                && (contact.haveSentEmail("low-valuation-email")
                        || contact.haveSentEmail("mid-valuation-email") || contact
                            .haveSentEmail("high-valuation-email"))) {
            mailData = new MailData("valuation-advice",
                    "Why Your Valuation Means Nothing...", contact.getEmail());
        } else if (lastBizPlan != null
                && lastBizPlan.getOccurred().after(fiveWeeksAgo)
                && !contact.haveSentEmail("plan-help")) {
            mailData = new MailData("plan-help",
                    "Get Your Business Sale Plans into Action (not inaction!)",
                    contact.getEmail());
        } else if (lastBizPlan != null
                && lastBizPlan.getOccurred().before(fiveWeeksAgo)
                && !contact.haveSentEmail("plan-next")) {
            mailData = new MailData("plan-next",
                    "Are you Fully Equipped for Your Business Sale?",
                    contact.getEmail());
        } else if (registration != null
                && registration.getOccurred().after(sixWeeksAgo)
                && !contact.haveSentEmail("discover")) {
            mailData = new MailData("discover",
                    "There’s More Under the Surface with Firm Gains",
                    contact.getEmail());
        } else if (registration != null
                && registration.getOccurred().before(sixWeeksAgo)
                && registration.getOccurred().after(tenWeeksAgo)
                && !contact.haveSentEmail("intro-services")) {
            mailData = new MailData("intro-services",
                    "Every Business Owner Needs a Helping Hand",
                    contact.getEmail());
        } else if (registration != null
                && registration.getOccurred().before(tenWeeksAgo)
                && registration.getOccurred().after(oneYearAgo)
                && !contact.haveSentEmail("business-sale-ideas")) {
            mailData = new MailData("business-sale-ideas",
                    "What Makes a ‘Good’ Business Sale?",
                    contact.getEmail());
        } else if (registration != null
                && registration.getOccurred().before(oneYearAgo)
                && !contact.haveSentEmail("anniversary")) {
            mailData = new MailData("anniversary",
                    "A Very Happy Anniversary... We Hope!",
                    contact.getEmail());

        }

        LOGGER.info(String.format("Mail to send is: %1$s", mailData));

        return mailData;
    }
}
