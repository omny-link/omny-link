/*******************************************************************************
 *Copyright 2018 Tim Stephenson and contributors
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
        assertApproxEqual(now.getTimeInMillis() + (24 * 60 * 60 * 1000),
                relativeDate.getTime(), 50);

        relativeDate = svc.getRelativeDate("P1D");

        assertApproxEqual(now.getTimeInMillis() + (24 * 60 * 60 * 1000),
                relativeDate.getTime(), 50);
    }

    // A 'good enough' assertion since execution time may be interfering
    private void assertApproxEqual(long l, long m, int leeway) {
        System.out.println("diff: " + (l - m));
        assertTrue(l - leeway < m);
        assertTrue(l + leeway > m);
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
