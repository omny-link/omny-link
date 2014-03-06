package com.knowprocess.beans;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.BeforeClass;
import org.junit.Test;

import com.google.code.linkedinapi.schema.Person;
import com.google.code.linkedinapi.schema.impl.PersonImpl;
import com.knowprocess.sugarcrm.api.SugarLead;

public class ConversionTaskTest {

	private static Person person;
	private static ConversionTask svc;

	@BeforeClass
	public static void setUpClass() {
		person = new PersonImpl();
		person.setFirstName("Alistair");
		person.setLastName("Cook");

		svc = new ConversionTask();
	}

	@Test
	public void testLinkedInPersonToSugar() {
		SugarLead lead = (SugarLead) new LinkedInPersonToSugarLead()
				.convert(person);
		assertNotNull(lead);
		assertEquals(person.getFirstName(), lead.getFirstName());
		assertEquals(person.getLastName(), lead.getLastName());
	}

	@Test
	public void testConversionTask() {
		SugarLead lead = svc.convert(person, SugarLead.class);
		assertNotNull(lead);
		assertEquals(person.getFirstName(), lead.getFirstName());
		assertEquals(person.getLastName(), lead.getLastName());
	}

}
