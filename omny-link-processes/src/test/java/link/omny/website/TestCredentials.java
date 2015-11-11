package link.omny.website;

import org.activiti.engine.IdentityService;
import org.activiti.engine.identity.User;

public class TestCredentials {

    public static final String BOT_USERNAME = "tstephen";
    public static final String BOT_PWD = "tstephen";
    public static final String CUST_MGMT_URL = "http://localhost:8082";

    public static void initBot(IdentityService idSvc, String tenantId) {
        User botUser = idSvc.newUser(BOT_USERNAME);
        botUser.setFirstName(tenantId.toLowerCase());
        botUser.setLastName("Bot");
        idSvc.saveUser(botUser);
        idSvc.setUserInfo(BOT_USERNAME, "cust-mgmt-secret", BOT_PWD);
        idSvc.setUserInfo(BOT_USERNAME, "cust-mgmt-url", CUST_MGMT_URL);

        if (System.getProperty("consumerKey") != null) {
            idSvc.setUserInfo(BOT_USERNAME, "twitter-consumer-key",
                    System.getProperty("consumerKey"));
        }
        if (System.getProperty("consumerSecret") != null) {
            idSvc.setUserInfo(BOT_USERNAME, "twitter-consumer-secret",
                    System.getProperty("consumerSecret"));
        }
        if (System.getProperty("accessToken") != null) {
            idSvc.setUserInfo(BOT_USERNAME, "twitter-access-token",
                    System.getProperty("accessToken"));
        }
        if (System.getProperty("accessSecret") != null) {
            idSvc.setUserInfo(BOT_USERNAME, "twitter-access-secret",
                    System.getProperty("accessSecret"));
        }

    }

    public static void removeBot(IdentityService idSvc, String tenantId) {
        idSvc.deleteUser(BOT_USERNAME);
    }
}
