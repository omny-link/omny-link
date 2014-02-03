package com.knowprocess.mail;

import static org.junit.Assert.assertEquals;

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

}
