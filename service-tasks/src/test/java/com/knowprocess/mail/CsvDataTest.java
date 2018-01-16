/*******************************************************************************
 *Copyright 2011-2018 Tim Stephenson and contributors
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

import org.junit.Test;

public class CsvDataTest {

	@Test
	public void testParseCsv() {
		CsvData data = new CsvData()
				.parse("\"addressee.firstName\"\n\"Tim\"\n\"John\"");
		assertEquals("Tim", data.get(0, "addressee.firstName"));
		assertEquals("John", data.get(1, "addressee.firstName"));

		data = new CsvData().parse("addressee.firstName,addressee.lastName\n"
				+ "Tim P.,Stephenson\n" + "John,Smith");
		assertEquals("Tim P.", data.get(0, "addressee.firstName"));
		assertEquals("Stephenson", data.get(0, "addressee.lastName"));

		assertEquals("John", data.get(1, "addressee.firstName"));
		assertEquals("Smith", data.get(1, "addressee.lastName"));
	}
}
