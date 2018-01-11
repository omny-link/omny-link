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
package com.knowprocess.in.filters;

import static org.junit.Assert.assertTrue;

import org.junit.BeforeClass;
import org.junit.Test;

import com.google.code.linkedinapi.schema.Person;
import com.google.code.linkedinapi.schema.impl.PersonImpl;

public class NameFilterTest {

	private static Person psn;

	@BeforeClass
	public static void setUpClass() {
		psn = new PersonImpl();
		psn.setFirstName("Tim");
		psn.setLastName("Stephenson");
	}

	@Test
	public void testMatchAny() {
		NameFilter filter = new NameFilter(".*");
		boolean match = filter.match(psn);
		assertTrue(match);

		filter = new NameFilter(null);
		match = filter.match(psn);
		assertTrue(match);

		filter = new NameFilter("");
		match = filter.match(psn);
		assertTrue(match);
	}

	@Test
	public void testMatchFirstName() {
		NameFilter filter = new NameFilter("Tim");
		boolean match = filter.match(psn);
		assertTrue(match);
	}

	@Test
	public void testMatchLastName() {
		NameFilter filter = new NameFilter("Stephenson");
		boolean match = filter.match(psn);
		assertTrue(match);
	}

	@Test
	public void testNoMatch() {
		NameFilter filter = new NameFilter("Fred");
		boolean match = filter.match(psn);
		assertTrue(!match);
	}
}
