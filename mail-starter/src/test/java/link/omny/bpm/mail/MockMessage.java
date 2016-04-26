package link.omny.bpm.mail;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.activation.DataHandler;
import javax.mail.Address;
import javax.mail.Flags;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.InternetAddress;

public class MockMessage extends Message {

    private Address[] from;
    private Object content;
    private String contentType;
    private Map<RecipientType, Address[]> recipientMap;
    private String subject;
    private Date sentDate;

    @Override
    public int getSize() throws MessagingException {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getLineCount() throws MessagingException {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public String getContentType() throws MessagingException {
        return contentType;
    }

    @Override
    public boolean isMimeType(String mimeType) throws MessagingException {
        return contentType.equals(mimeType);
    }

    @Override
    public String getDisposition() throws MessagingException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setDisposition(String disposition) throws MessagingException {
        // TODO Auto-generated method stub

    }

    @Override
    public String getDescription() throws MessagingException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setDescription(String description) throws MessagingException {
        // TODO Auto-generated method stub

    }

    @Override
    public String getFileName() throws MessagingException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setFileName(String filename) throws MessagingException {
        // TODO Auto-generated method stub

    }

    @Override
    public InputStream getInputStream() throws IOException, MessagingException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public DataHandler getDataHandler() throws MessagingException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object getContent() throws IOException, MessagingException {
        return content;
    }

    @Override
    public void setDataHandler(DataHandler dh) throws MessagingException {
        // TODO Auto-generated method stub

    }

    @Override
    public void setContent(Object obj, String type) throws MessagingException {
        this.content = obj;
        this.contentType = type;
    }

    @Override
    public void setText(String text) throws MessagingException {
        // TODO Auto-generated method stub

    }

    @Override
    public void setContent(Multipart mp) throws MessagingException {
        // TODO Auto-generated method stub

    }

    @Override
    public void writeTo(OutputStream os) throws IOException, MessagingException {
        // TODO Auto-generated method stub

    }

    @Override
    public String[] getHeader(String header_name) throws MessagingException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setHeader(String header_name, String header_value)
            throws MessagingException {
        // TODO Auto-generated method stub

    }

    @Override
    public void addHeader(String header_name, String header_value)
            throws MessagingException {
        // TODO Auto-generated method stub

    }

    @Override
    public void removeHeader(String header_name) throws MessagingException {
        // TODO Auto-generated method stub

    }

    @Override
    public Enumeration getAllHeaders() throws MessagingException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Enumeration getMatchingHeaders(String[] header_names)
            throws MessagingException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Enumeration getNonMatchingHeaders(String[] header_names)
            throws MessagingException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Address[] getFrom() throws MessagingException {
        return from;
    }

    @Override
    public void setFrom() throws MessagingException {
        setFrom(new InternetAddress("tim@omny.link"));
    }

    @Override
    public void setFrom(Address address) throws MessagingException {
        addFrom(new Address[] { address });
    }

    @Override
    public void addFrom(Address[] addresses) throws MessagingException {
        this.from = addresses;
    }

    protected Map<RecipientType, Address[]> getRecipientMap() {
        if (recipientMap == null) {
            recipientMap = new HashMap<RecipientType, Address[]>();
        }
        return recipientMap;
    }

    @Override
    public Address[] getRecipients(RecipientType type)
            throws MessagingException {

        return getRecipientMap().get(type);
    }

    @Override
    public void setRecipients(RecipientType type, Address[] addresses)
            throws MessagingException {
        getRecipientMap().put(type, addresses);
    }

    @Override
    public void addRecipients(RecipientType type, Address[] addresses)
            throws MessagingException {
        // TODO Auto-generated method stub

    }

    @Override
    public String getSubject() throws MessagingException {
        return subject;
    }

    @Override
    public void setSubject(String subject) throws MessagingException {
        this.subject = subject;
    }

    @Override
    public Date getSentDate() throws MessagingException {
        return sentDate;
    }

    @Override
    public void setSentDate(Date date) throws MessagingException {
        this.sentDate = date;
    }

    @Override
    public Date getReceivedDate() throws MessagingException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Flags getFlags() throws MessagingException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setFlags(Flags flag, boolean set) throws MessagingException {
        // TODO Auto-generated method stub

    }

    @Override
    public Message reply(boolean replyToAll) throws MessagingException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void saveChanges() throws MessagingException {
        // TODO Auto-generated method stub

    }

}
