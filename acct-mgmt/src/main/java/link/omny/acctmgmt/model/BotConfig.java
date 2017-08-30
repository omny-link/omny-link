package link.omny.acctmgmt.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class BotConfig {

    private String apiKey;

    private String apiSecret;

    private String adminEmail;

    private String custMgmtUrl;

    private String custMgmtSecret;

    private String jwtAuthUrl;

    private String ccAccount;

    private boolean valid;

    public static final String KEY_CC_ACCOUNT = "cc_account";

    public static final String KEY_CUST_MGMT_SECRET = "cust-mgmt-secret";

    public static final String KEY_CUST_MGMT_URL = "cust-mgmt-url";

	public static final String KEY_JWT_AUTH_URL = "jwt-login-url";

    public BotConfig(String id) {
        setApiKey(id);
    }

    public BotConfig(String id, String password, String email) {
        this(id);
        setApiSecret(password);
        setAdminEmail(email);
    }

}
