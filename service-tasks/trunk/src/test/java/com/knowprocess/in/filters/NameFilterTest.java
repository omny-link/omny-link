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
