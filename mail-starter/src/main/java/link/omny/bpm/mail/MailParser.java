package link.omny.bpm.mail;

import java.io.IOException;

import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Multipart;

public class MailParser {
    protected void printMessage(Message msg) throws MessagingException,
            IOException {
        Address[] from = msg.getFrom();
        for (Address address : from) {
            System.out.println("FROM:" + address.toString());
        }
        Address[] to = msg.getAllRecipients();
        for (Address address : to) {
            System.out.println("TO:" + address.toString());
        }
        System.out.println("SENT DATE:" + msg.getSentDate());
        System.out.println("SUBJECT:" + msg.getSubject());

        String contentType = msg.getContentType();
        System.out.println("CONTENT TYPE: " + contentType);
        if (contentType.indexOf(';') != -1) {
            contentType = contentType.substring(0, contentType.indexOf(';'));
        }
        switch (contentType.toLowerCase()) {
        case "text/plain":
            String content = (String) msg.getContent();
            // BodyPart bp = mp.getBodyPart(0);
            System.out.println("CONTENT:" + content);
            break;
        case "multipart/alternative":
        case "multipart/related":
            Multipart mp = (Multipart) msg.getContent();
            BodyPart bp = mp.getBodyPart(0);
            System.out.println("CONTENT:" + bp.getContent());
            break;
        default:
            System.err.println("Unknown content type: " + contentType);
        }
    }

    public String toJson(Message message) throws MessagingException,
            IOException {

        StringBuilder sb = new StringBuilder();
        sb.append("{");

        Address[] from = message.getFrom();
        if (from.length > 0) {
            sb.append("\"initiator\":\"" + getSimpleAddress(from) + "\"").append(
                    ",");
        }

        Address[] to = message.getRecipients(RecipientType.TO);
        if (to.length > 0) {
            sb.append("\"contactEmail\":\"" + getSimpleAddress(to) + "\"")
                    .append(
                    ",");
        }

        String contentType = message.getContentType();
        System.out.println("CONTENT TYPE: " + contentType);
        if (contentType.indexOf(';') != -1) {
            contentType = contentType.substring(0, contentType.indexOf(';'));
        }
        String content = null;
        switch (contentType.toLowerCase()) {
        case "text/plain":
            content = (String) message.getContent();
            System.out.println("CONTENT:" + content);
            sb.append("\"message\":\"" + content + "\"").append(",");
            break;
        case "multipart/alternative":
        case "multipart/related":
            Multipart mp = (Multipart) message.getContent();
            BodyPart bp = mp.getBodyPart(0);
            System.out.println("CONTENT:" + bp.getContent());
            if (bp.getContent() instanceof String) {
                sb.append("\"message\":\"" + bp.getContent() + "\"")
                        .append(",");
            } else {
                sb.append(
                        "\"message\":\""
                                + ((Multipart) bp.getContent()).toString()
                                + "\"").append(",");
            }
            break;
        default:
            System.err.println("Unknown content type: " + contentType);
        }

        sb.append("\"subject\":\"" + message.getSubject() + "\"");

        sb.append("}");
        System.out.println("JSON: "
                + sb.toString().replaceAll("\n", "").replaceAll("\r", ""));
        return sb.toString().replaceAll("\n", "").replaceAll("\r", "");
    }

    private String getSimpleAddress(Address[] from) {
        String f = from[0].toString();
        if (f.indexOf('<') != -1) {
            f = f.substring(f.indexOf('<') + 1, f.indexOf('>'));
        }
        return f;
    }
}
