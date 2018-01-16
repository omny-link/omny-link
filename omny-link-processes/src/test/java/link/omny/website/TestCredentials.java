/*******************************************************************************
 *Copyright 2011-2018 Tim Stephenson and contributors
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
package link.omny.website;

import org.activiti.engine.IdentityService;
import org.activiti.engine.identity.User;
import org.junit.Assume;

public class TestCredentials {

    public static final String BOT_USERNAME = "tstephen";
    public static final String BOT_PWD = "tstephen";
    public static final String BOT_EMAIL = "tim@knowprocess.com";
    public static final String CUST_MGMT_URL = "http://localhost:8082";

    public static void initBot(IdentityService idSvc, String tenantId) {
        User botUser = idSvc.newUser(BOT_USERNAME);
        botUser.setFirstName(tenantId.toLowerCase());
        botUser.setLastName("Bot");
        botUser.setEmail(BOT_EMAIL);
        idSvc.saveUser(botUser);
        idSvc.setUserInfo(BOT_USERNAME, "cust-mgmt-secret", BOT_PWD);
        idSvc.setUserInfo(BOT_USERNAME, "cust-mgmt-url", CUST_MGMT_URL);
        idSvc.setUserInfo(BOT_USERNAME, "cc_account", "");
    }

    public static void initBotWithTwitterIntegration(IdentityService idSvc,
            String tenantId) {
        initBot(idSvc, tenantId);
        initTwitterConfig(idSvc, tenantId);
    }

    protected static void initTwitterConfig(IdentityService idSvc,
            String tenantId) {
        Assume.assumeTrue("No credentials provided to allow twitter integration", 
                System.getProperty("consumerKey") != null
                || System.getProperty("consumerSecret") != null
                || System.getProperty("accessToken") != null
                || System.getProperty("accessSecret") != null);

        idSvc.setUserInfo(BOT_USERNAME, "twitter-consumer-key",
                System.getProperty("consumerKey"));
        idSvc.setUserInfo(BOT_USERNAME, "twitter-consumer-secret",
                System.getProperty("consumerSecret"));
        idSvc.setUserInfo(BOT_USERNAME, "twitter-access-token",
                System.getProperty("accessToken"));
        idSvc.setUserInfo(BOT_USERNAME, "twitter-access-secret",
                System.getProperty("accessSecret"));
    }

    public static void removeBot(IdentityService idSvc, String tenantId) {
        idSvc.deleteUser(BOT_USERNAME);
    }
}
