/*******************************************************************************
 * Copyright 2015-2025 Tim Stephenson and contributors
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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.GregorianCalendar;

import org.junit.jupiter.api.Test;

public class MemoDistributionTest {

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

    // Useful to allow empty recipient list so partial distributions can be
    // saved
    @Test
    public void testSetEmptyRecipientList() {
        MemoDistribution dist = new MemoDistribution();
        dist.setRecipientList(Arrays.asList(new String[] {}));
        assertEquals(0, dist.getRecipientList().size());
        // assertEquals("john,paul,george,ringo", dist.getRecipients());
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

        dist.setSendAtDate(null);
        dist.setSendAtTime(null);
        dist.setSendAtTZ(null);

        assertEquals(null, dist.getSendAtDate());
        assertEquals(null, dist.getSendAtTime());
        assertEquals(null, dist.getSendAt());
    }
}

