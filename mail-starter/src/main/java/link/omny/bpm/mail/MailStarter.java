package link.omny.bpm.mail;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.Properties;

import javax.mail.FetchProfile;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.search.FlagTerm;

import com.knowprocess.resource.spi.RestGet;

public class MailStarter {

    private static Properties props = new Properties();;

    private static MailStarter me;

    private MailParser parser;

    private RestGet get;

    private String usr;

    private String pwd;

    private String host;

    private String tenantId;

    private String protocol;

    public MailStarter(String host, String usr, String pwd, String protocol,
            String tenantId) {
        this.usr = usr.trim();
        this.pwd = pwd.trim();
        this.protocol = protocol.trim();
        this.host = host.trim();
        this.tenantId = tenantId.trim();
    }

    public static void main(String[] args) {
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(new File("omny.properties"));
            props.load(fis);

            getInstance().processFolder(props.getProperty("omny.mail.host"),
                    props.getProperty("omny.mail.username"),
                    props.getProperty("omny.mail.password"),
                    props.getProperty("omny.mail.folder"));
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ":" + e.getMessage());
        } finally {
            try {
                fis.close();
            } catch (IOException e) {
            }
        }
    }

    protected static MailStarter getInstance() {
        if (me == null) {
            me = new MailStarter(props.getProperty("omny.bpm.host"),
                    props.getProperty("omny.bpm.username"),
                    props.getProperty("omny.bpm.password"),
                    props.getProperty("omny.bpm.protocol"),
                    props.getProperty("omny.bpm.tenant"));
        }
        return me;
    }

    protected MailParser getParser() {
        if (parser == null) {
            parser = new MailParser();
        }
        return parser;
    }

    protected RestGet getRestGet() {
        if (get == null) {
            get = new RestGet();
        }
        return get;
    }

    protected void processFolder(String mailHost, String mailUsr,
            String mailPwd, String folder) {

        Store store = null;
        Folder inbox = null;
        try {
            Session session = Session.getInstance(props, null);
            store = session.getStore();
            store.connect(mailHost, mailUsr, mailPwd);
            inbox = store.getFolder(folder);
            inbox.open(Folder.READ_ONLY);
            // get last message
            // Message msg = inbox.getMessage(inbox.getMessageCount());
            Message messages[] = inbox.search(new FlagTerm(new Flags(
                    Flags.Flag.SEEN), false));
            System.out.println("No. of Unread Messages : " + messages.length);

            /* Use a suitable FetchProfile */
            FetchProfile fp = new FetchProfile();
            fp.add(FetchProfile.Item.ENVELOPE);
            fp.add(FetchProfile.Item.CONTENT_INFO);

            inbox.fetch(messages, fp);

            for (Message message : messages) {
                // parser.printMessage(message);
                String json = URLEncoder.encode(getParser().toJson(message),
                        "UTF-8");
                System.out.println(json);
                String resourceUrl = protocol + "://" + usr + ":" + pwd + "@"
                        + host + "/msg/" + tenantId + "/omny.adHoc.json?query="
                        + json;
                System.out.println("URL: " + resourceUrl);
                String result = getRestGet().fetchToString(resourceUrl);
                System.out.println(result);
            }

        } catch (Exception mex) {
            mex.printStackTrace();
        } finally {
            try {
                inbox.close(true);
            } catch (Exception e) {
            }
            try {
                store.close();
            } catch (Exception e) {
            }
        }
    }

}