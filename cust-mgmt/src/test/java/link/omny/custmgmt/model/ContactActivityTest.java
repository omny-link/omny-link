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
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.GregorianCalendar;

import org.junit.Before;
import org.junit.Test;

public class ContactActivityTest {

    private static final long ONE_MINUTE = 60 * 1000;
    private static final long ONE_WEEK = 7 * 60 * ONE_MINUTE;
    private Contact contact;

    @Before
    public void setUpBeforeClass() throws Exception {
        contact = new Contact();
        contact.getActivities().add(new Activity(ActivityType.REGISTRATION, null));
        contact.getActivities().add(
                new Activity(ActivityType.LOGIN, new GregorianCalendar(2015, 1, 1, 13, 0,
                        0).getTime()));
    }


    @Test
    public void testTimeSinceLogin() {
        assertTrue(contact.getTimeSinceLogin() > ONE_WEEK);
    }

    @Test
    public void testTimeSinceFirstLogin() {
        contact.getActivities().add(new Activity(ActivityType.LOGIN, new Date()));
        assertTrue(contact.getTimeSinceFirstLogin() > ONE_WEEK);
    }

    @Test
    public void testHaveSentEmail() {
        assertTrue(contact.notYetSentEmail("welcome"));
        contact.getActivities().add(
                new Activity(ActivityType.EMAIL, new Date(), "welcome"));
        assertTrue(contact.haveSentEmail("welcome"));
    }

    @Test
    public void testTimeSinceNonExistantRegistration() {
        assertTrue(contact.getTimeSinceRegistered() == -1);
    }

    @Test
    public void testMailsSent() {
        assertEquals(contact.getEmailsSent(), 0);
        contact.getActivities().add(
                new Activity(ActivityType.EMAIL, new Date(), "welcome"));
        assertEquals(contact.getEmailsSent(), 1);
    }

    @Test
    public void testTimeSinceLastEmail() {
        contact.getActivities().add(
                new Activity(ActivityType.EMAIL, new GregorianCalendar(2015, 1, 1, 13, 0,
                        0).getTime()));
        contact.getActivities().add(new Activity(ActivityType.EMAIL, new Date()));
        assertTrue(contact.getTimeSinceEmail() < ONE_MINUTE);
    }
}
