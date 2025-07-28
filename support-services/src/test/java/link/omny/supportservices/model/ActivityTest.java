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
package link.omny.supportservices.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

public class ActivityTest {

    @Test
    public void testHashCodeForStackOverflow() {
        Date now = new Date();
        Activity c1 = new Activity(ActivityType.EMAIL, now, "A test");
        Activity c2 = new Activity(ActivityType.EMAIL, now, "A test");
        assertEquals(c1, c2);

        c1.hashCode();
        assertEquals(c1, c2);
    }

    @Test
    public void testParseJsonActivity() {
        String jsonInString = readFromClasspath("/activity.json");
        assertNotNull(jsonInString);

        ObjectMapper mapper = new ObjectMapper();
        SimpleDateFormat isoFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");

        try {
            Activity activity = mapper.readValue(jsonInString, Activity.class);

            assertNotNull(activity);
            assertEquals(ActivityType.EMAIL.name(), activity.getType());
            assertEquals("2022-12-02T03:00:00+0000", isoFormatter.format(activity.getOccurred()));
            assertEquals("A test", activity.getContent());

        } catch (IOException e) {
            e.printStackTrace();
            fail("Unable to parse JSON");
        }
    }

    @Test
    public void testToCsv() throws IOException {
        Activity activity = new Activity();
        activity.setId(1l);
        activity.setType(ActivityType.EMAIL.name());
        activity.setContent("Content\nthat\nspans\nmultiple\nlines");

        System.out.println(activity.toCsv());
        String csv = activity.toCsv();
        assertTrue(csv.startsWith("1,EMAIL,\"Content\nthat\nspans\nmultiple\nlines\""));
    }

    private static String readFromClasspath(String resourceName) {
        try (Scanner scanner = new Scanner(ActivityTest.class.getResourceAsStream(resourceName))) {
            return scanner.useDelimiter("\\A").next();
        } catch (Exception e) {
            throw e;
        }
    }
}
