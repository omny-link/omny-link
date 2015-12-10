package link.omny.custmgmt.model;

import static org.junit.Assert.assertEquals;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.GregorianCalendar;

import org.junit.BeforeClass;
import org.junit.Test;

public class MemoDistributionTest {

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @Test
    public void testSetRecipients() {
        MemoDistribution dist = new MemoDistribution();
        dist.setRecipients("john,paul,george,ringo");
        assertEquals(4, dist.getRecipientList().size());
    }

    @Test
    public void testSetRecipientList() {
        MemoDistribution dist = new MemoDistribution();
        dist.setRecipientList(Arrays.asList(new String[] { "john", "paul",
                "george", "ringo" }));
        assertEquals(4, dist.getRecipientList().size());
        assertEquals("john,paul,george,ringo", dist.getRecipients());
    }

    @Test
    public void testGetRecipientList() {
        MemoDistribution dist = new MemoDistribution();
        dist.setRecipientList(Arrays.asList(new String[] { "john", "paul",
                "\"george\"", "ringo" }));
        assertEquals(4, dist.getRecipientList().size());
        assertEquals("john,paul,george,ringo", dist.getRecipients());
    }

    @Test
    public void testSendAt() throws ParseException {
        MemoDistribution dist = new MemoDistribution();
        SimpleDateFormat dateTimeParser = new SimpleDateFormat(
                "yyyy-MM-dd'T'HH:mm Z");
        SimpleDateFormat dateParser = new SimpleDateFormat("yyyy-MM-dd");
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(dateParser.parse("2015-12-13"));

        dist.setSendAtDate("2015-12-13");
        dist.setSendAtTime("12:34");
        dist.setSendAtTZ("GMT-08:00");

        assertEquals(dateTimeParser.parse("2015-12-13T20:34 GMT").getTime(),
                dist.getSendAt().getTime());
    }
}
