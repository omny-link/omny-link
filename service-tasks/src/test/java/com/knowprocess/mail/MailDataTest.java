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
package com.knowprocess.mail;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

public class MailDataTest {

	@Test
	public void testParseJson() {
		MailData data = new MailData()
				.fromJson("{\"addressee\":{\"firstName\":\"Tim\"}}");

		assertEquals("Tim", data.get("addressee.firstName"));
	}

	@Test
	public void testParseFlattenedJson() {
		MailData data = new MailData()
				.fromFlattenedJson("{ \"addressee.firstName\":\"Tim\",\"addressee.email\":\"tim@knowprocess.com\",\"addressee.accountName\":\"Know Process\",\"assignedTo.firstName\":\"Tim\",\"assignedTo.email\":\"tim@trakeo.com\",\"assignedTo.mobile\":\"07890 123456\"}");

		assertEquals("Tim", data.get("addressee.firstName"));
		assertEquals("tim@knowprocess.com", data.get("addressee.email"));
	}

    @Test
    public void testParseJsonArray() {
        List<MailData> array = MailData
                .fromJsonArray("[{\"username\":\"447798867607\",\"frequency\":\"Regular\"},{\"username\":\"tim@knowprocess.com\",\"firstName\":\"Tim\",\"frequency\":\"All\"}]");

        assertEquals(2, array.size());
        assertEquals(array.get(0).get("username"), "447798867607");
        assertEquals(array.get(1).get("username"), "tim@knowprocess.com");
    }

}
