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
