package com.knowprocess.bpm.web;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.junit.BeforeClass;
import org.junit.Test;

public class DeferTest {

	private static TaskController svc;

	@BeforeClass
	public static void setUpClass() {
		svc = new TaskController();
	}

	@Test
	public void testDeferUntilTomorrow() {
		GregorianCalendar now = new GregorianCalendar();
		Date relativeDate = svc.getRelativeDate("PT24H"); 
		assertEquals(now.getTimeInMillis() + (24 * 60 * 60 * 1000),
				relativeDate.getTime());

		relativeDate = svc.getRelativeDate("P1D");
		System.out.println("diff: "
				+ (now.getTimeInMillis() + (24 * 60 * 60 * 1000) - relativeDate
						.getTime()));
		// A 'good enough' assertion since execution time may be interferring
		// and days are not in fact 24 h exactly
        assertTrue(now.getTimeInMillis() + (24 * 60 * 60 * 1000) - 150 < relativeDate
				.getTime());
        assertTrue(now.getTimeInMillis() + (24 * 60 * 61 * 1000) + 150 > relativeDate
				.getTime());
	}

    @Test
    public void testDeferUntilNextMonday() {
        GregorianCalendar now = new GregorianCalendar();

        Date relativeDate = svc.getRelativeDate("Monday");

        GregorianCalendar otherCal = new GregorianCalendar();
        otherCal.setTime(relativeDate);
        assertEquals(Calendar.MONDAY, otherCal.get(Calendar.DAY_OF_WEEK));
        if (now.get(Calendar.WEEK_OF_YEAR) != 52) {
            assertEquals(now.get(Calendar.WEEK_OF_YEAR) + 1,
                    otherCal.get(Calendar.WEEK_OF_YEAR));
        }
    }

}
