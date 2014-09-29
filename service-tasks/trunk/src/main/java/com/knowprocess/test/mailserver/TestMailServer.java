package com.knowprocess.test.mailserver;

import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.activation.DataHandler;
import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.junit.rules.ExternalResource;
import org.subethamail.wiser.Wiser;
import org.subethamail.wiser.WiserMessage;

public class TestMailServer extends ExternalResource {
	private static final List<String> EMPTY_LIST = Collections.emptyList();
	protected Wiser wiser = new Wiser();

    @Override
    protected void before() throws Throwable {
        super.before();
        wiser.setPort(5025); // Default is 25
        wiser.start();
    }

    @Override
    protected void after() {
        super.after();
        wiser.stop();
    }

    public Wiser getWiser() {
        return wiser;
    }

    public void dumpMailSent()
            throws MessagingException, IOException {
        List<WiserMessage> messages = wiser.getMessages();
		System.out.println(String.format(
				"*********** Sent %1$s messages ***********", messages.size()));
        for (WiserMessage wiserMessage : messages) {
            System.out.println("  from: " + wiserMessage.getEnvelopeSender());
            System.out.println("  to: " + wiserMessage.getEnvelopeReceiver());
            System.out.println("  subject: "
                    + wiserMessage.getMimeMessage().getSubject());
            Object content = wiserMessage.getMimeMessage().getContent();
			if (content instanceof String) {
				System.out.println("  content: " + content);
			} else {
				dumpBodyPart(content);
			}
        }
    }

	private void dumpBodyPart(Object content) throws MessagingException,
			IOException {
		MimeMultipart mm = (MimeMultipart) content;
		for (int i = 0; i < mm.getCount(); i++) {
			BodyPart part = mm.getBodyPart(i);
			if (part.getContent() instanceof MimeMultipart) {
				dumpBodyPart(part.getContent());
			} else {
				System.out.println("  " + i + ": " + part.getContent());
			}
		}
	}

	public void assertEmailSend(int idx, boolean htmlMail, String subject,
			String txtMessage, String from, String to, String cc)
			throws IOException, MessagingException {
		assertEmailSend(wiser.getMessages().get(idx).getMimeMessage(),
				htmlMail, subject, txtMessage, from,
				Arrays.asList(new String[] { to }),
				cc == null ? null : Arrays.asList(new String[] { cc }));
	}

	public void assertEmailSend(int idx, boolean htmlMail, String subject,
			String txtMessage, String from, List<String> to, List<String> cc)
			throws IOException, MessagingException {
		assertEmailSend(wiser.getMessages().get(idx).getMimeMessage(),
				htmlMail, subject, txtMessage, from, to, cc);
	}

	public void assertEmailSend(int idx, boolean htmlMail, String subject,
			String txtMessage, String from, List<String> to)
			throws IOException, MessagingException {
		assertEmailSend(idx, htmlMail, subject, txtMessage, from, to,
				EMPTY_LIST);
	}

	protected void assertEmailSend(MimeMessage mimeMessage, boolean htmlMail,
            String subject, String txtMessage, String from, List<String> to,
			List<String> cc) throws IOException, MessagingException {
		if (htmlMail) {
			assertTrue(mimeMessage.getContentType().contains("multipart/mixed"));
		} else {
			assertTrue(mimeMessage.getContentType().contains("text/plain"));
		}

		assertTrue("Message does not have expected subject: " + subject,
				mimeMessage.getHeader("Subject", null).contains(subject));
		// Test from is either long or short form of sender
		String longFrom = "\"" + from + "\" <" + from.toString() + ">";
		assertTrue(
                "Message not from the expected sender, expected: " + from
                        + " but was: " + mimeMessage.getHeader("From", null),
				longFrom.equals(mimeMessage.getHeader("From", null))
				|| from.equals(mimeMessage.getHeader("From", null)));
		System.out.println("Msg body: " + getMessage(mimeMessage));
		assertTrue("Message does not contain expected content: " + txtMessage,
				getMessage(mimeMessage).contains(txtMessage));

		for (String t : to) {
			assertTrue("Message does not contain expected recipient: " + t,
					mimeMessage.getHeader("To", null).contains(t));
		}

		if (cc != null) {
			for (String c : cc) {
				assertTrue("Message does not contain expected cc recipient: "
						+ c, mimeMessage.getHeader("Cc", null).contains(c));
            }
        }
    }

    protected String getMessage(MimeMessage mimeMessage)
            throws MessagingException, IOException {
        DataHandler dataHandler = mimeMessage.getDataHandler();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        dataHandler.writeTo(baos);
        baos.flush();
        String msg = baos.toString();
        return msg;
    }
}
