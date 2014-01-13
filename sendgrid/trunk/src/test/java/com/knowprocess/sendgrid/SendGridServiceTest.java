package com.knowprocess.sendgrid;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.junit.Ignore;
import org.junit.Test;

import com.knowprocess.sendgrid.SendGridService.ActionType;
import com.knowprocess.sendgrid.SendGridService.FormatType;

public class SendGridServiceTest {

	@Test
	@Ignore
	public void testAddAndGetTemplate() {
		SendGridService svc = new SendGridService();
		try {
			String name = "SendGrid_Test";
			String params = "identity=Sender_Address&name="
					+ name
					+ "&subject=testsubject&text=testtextbody"
					+ "&html=%3Chtml%3E%3Cp%3Etest_html_body%3C%2Fp%3E%3C%2Fhtml%3E";
			String response = svc.addTemplate(FormatType.JSON, params);
			params = "name=" + name;
			response = svc.getTemplate(ActionType.GET, FormatType.JSON, params);
			System.out.println(response);
			assertNotNull(response);
		} catch (IOException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	@Test
	public void testGetBlocks() {
		SendGridService svc = new SendGridService();
		try {
			String response = svc.getBlocks(/* days */14, /* records */20,
					/* start record */0);
			System.out.println(response);
			assertNotNull(response);
			
			Calendar cal = new GregorianCalendar();
			cal.add(Calendar.DAY_OF_MONTH, -7); 
			Date start = cal.getTime(); 
			response = svc.getBlocks(
					/* startDate */start,
					/* endDate */new Date(),
					/* records */20,
					/* start record */0);
			System.out.println(response);
			assertNotNull(response);
		} catch (IOException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	@Test
	public void testGetBounces() {
		SendGridService svc = new SendGridService();
		try {
			String response = svc.getBounces(/* days */14, /* records */20,
			/* start record */0);
			System.out.println(response);
			assertNotNull(response);

			Calendar cal = new GregorianCalendar();
			cal.add(Calendar.DAY_OF_MONTH, -7);
			Date start = cal.getTime();
			response = svc.getBounces(
			/* startDate */start,
			/* endDate */new Date(),
			/* records */20,
			/* start record */0);
			System.out.println(response);
			assertNotNull(response);
		} catch (IOException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	@Test
	public void testGetInvalidEmails() {
		SendGridService svc = new SendGridService();
		try {
			String response = svc.getInvalidEmails(/* days */14, /* records */
					20,
					/* start record */0);
			System.out.println(response);
			assertNotNull(response);

			Calendar cal = new GregorianCalendar();
			cal.add(Calendar.DAY_OF_MONTH, -7);
			Date start = cal.getTime();
			response = svc.getInvalidEmails(
			/* startDate */start,
			/* endDate */new Date(),
			/* records */20,
			/* start record */0);
			System.out.println(response);
			assertNotNull(response);
		} catch (IOException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	@Test
	public void testGetUnsubscribes() {
		SendGridService svc = new SendGridService();
		try {
			String response = svc.getUnsubscribes(/* days */14, /* records */20,
			/* start record */0);
			System.out.println(response);
			assertNotNull(response);

			Calendar cal = new GregorianCalendar();
			cal.add(Calendar.DAY_OF_MONTH, -7);
			Date start = cal.getTime();
			response = svc.getUnsubscribes(
			/* startDate */start,
			/* endDate */new Date(),
			/* records */20,
			/* start record */0);
			System.out.println(response);
			assertNotNull(response);
		} catch (IOException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}
}
