/*******************************************************************************
 *Copyright 2015-2018 Tim Stephenson and contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
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
