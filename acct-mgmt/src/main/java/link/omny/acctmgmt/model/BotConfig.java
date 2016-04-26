package link.omny.acctmgmt.model;

import lombok.Data;

@Data
public class BotConfig {

    private String apiKey;

    private String apiSecret;

    private String adminEmail;

    private String custMgmtUrl;

    private String custMgmtSecret;

    private boolean valid;

    public BotConfig(String id) {
        setApiKey(id);
    }

    public BotConfig(String id, String password, String email) {
        this(id);
        setApiSecret(password);
        setAdminEmail(email);
    }

}
