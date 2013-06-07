package com.knowprocess.test.mailserver;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import javax.activation.DataHandler;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.junit.rules.ExternalResource;
import org.subethamail.wiser.Wiser;
import org.subethamail.wiser.WiserMessage;

public class TestMailServer extends ExternalResource {
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
        System.out.println("found " + messages.size() + " messages.");
        for (WiserMessage wiserMessage : messages) {
            System.out.println("  from: " + wiserMessage.getEnvelopeSender());
            System.out.println("  to: " + wiserMessage.getEnvelopeReceiver());
            System.out.println("  subject: "
                    + wiserMessage.getMimeMessage().getSubject());
            System.out.println("  content: "
                    + wiserMessage.getMimeMessage().getContent());
        }
    }

    protected void assertEmailSend(MimeMessage mimeMessage, boolean htmlMail,
            String subject, String txtMessage, String from, List<String> to,
            List<String> cc) throws IOException {
        try {
            if (htmlMail) {
                assertTrue(mimeMessage.getContentType().contains(
                        "multipart/mixed"));
            } else {
                assertTrue(mimeMessage.getContentType().contains("text/plain"));
            }

            assertTrue(mimeMessage.getHeader("Subject", null).contains(subject));
            // Test from is either long or short form of sender
            String longFrom = "\"" + from + "\" <" + from.toString() + ">";
            assertTrue(longFrom.equals(mimeMessage.getHeader("From", null))
                    || from.equals(mimeMessage.getHeader("From", null)));
            System.out.println("Msg body: " + getMessage(mimeMessage));
            assertTrue(getMessage(mimeMessage).contains(txtMessage));

            for (String t : to) {
                assertTrue(mimeMessage.getHeader("To", null).contains(t));
            }

            if (cc != null) {
                for (String c : cc) {
                    assertTrue(mimeMessage.getHeader("Cc", null).contains(c));
                }
            }

        } catch (MessagingException e) {
            fail(e.getMessage());
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

    public void assertMessage(int idx, String recipient, String subject) {
        WiserMessage msg = wiser.getMessages().get(idx);
        assertEquals(recipient, msg.getEnvelopeReceiver());
        try {
            assertEquals(subject, msg.getMimeMessage().getSubject());
        } catch (MessagingException e) {
            fail("Subject not as expected, " + e.getMessage());
        }

    }
}
