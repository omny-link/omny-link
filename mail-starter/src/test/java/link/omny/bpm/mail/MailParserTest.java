package link.omny.bpm.mail;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;

import org.junit.BeforeClass;
import org.junit.Test;

public class MailParserTest {

    private static final String CONTENT = "Dear Yogi,\n\n Hope you enjoy the picnic!\n\n Cheers, BooBoo";
    private static final String SUBJECT = "Your sandwich order";
    private static final String TO = "yogi@yellowstone.org";
    private static final String FROM = "booboo@yellostone.org";

    private static MailParser mailParser;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        mailParser = new MailParser();
    }

    @Test
    public void testParseAdHocTaskInPlainTextMessage()
            throws MessagingException, IOException {
        MockMessage msg = new MockMessage();
        msg.setFrom(new InternetAddress(FROM));
        msg.setRecipient(RecipientType.TO, new InternetAddress(TO));
        msg.setRecipient(RecipientType.CC, new InternetAddress(
                "do@knowprocess.com"));
        msg.setSubject(SUBJECT);
        msg.setContent(CONTENT, "text/plain");
        String json = mailParser.toJson(msg);
        assertNotNull(json);

        assertTrue(json.contains("\"initiator\":\"" + FROM + "\""));
        assertTrue(json.contains("\"message\":\"" + CONTENT + "\""));
        assertTrue(json.contains("\"contactEmail\":\"" + TO + "\""));
        assertTrue(json.contains("\"subject\":\"" + SUBJECT + "\""));

        System.out.println(json);
    }

}
