/*******************************************************************************
 *Copyright 2018 Tim Stephenson and contributors
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
package com.knowprocess.in;

import org.activiti.bdd.test.activiti.ExtendedRule;
import org.activiti.engine.IdentityService;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Rule;

public class AbstractLinkedInTest {

    protected static final String INITIATOR = "timatthestephensons@gmail.com";

    @Rule
    public ExtendedRule activitiRule = new ExtendedRule("test-activiti.cfg.xml");

    @Before
    public void setUp() {
        String consumerKey = System.getProperty(LinkedInTask.CONSUMER_KEY_KEY);
	String consumerSecret = System.getProperty(LinkedInTask.CONSUMER_SECRET_KEY);
        String accessToken = System.getProperty(LinkedInTask.ACCESS_TOKEN_KEY);
        String accessSecret = System
                .getProperty(LinkedInTask.ACCESS_TOKEN_SECRET_KEY);

        if (consumerKey == null || consumerSecret == null
                || accessSecret == null || accessToken == null) {
            Assume.assumeTrue(
                    "No credentials supplied to connect to LinkedIn. Assume test pass.",
                    false);
        }
	createTestUser(LinkedInTask.APP_USER_ID,
	        LinkedInTask.CONSUMER_KEY_KEY, consumerKey,
		LinkedInTask.CONSUMER_SECRET_KEY, consumerSecret);

	createTestUser(INITIATOR,
	        LinkedInTask.ACCESS_TOKEN_KEY, accessToken,
	        LinkedInTask.ACCESS_TOKEN_SECRET_KEY, accessSecret);
    }

    private void createTestUser(String userId, String key, String keyValue,
            String secret, String secretValue) {
        IdentityService svc = activitiRule.getIdentityService();
        try {
            svc.saveUser(svc.newUser(userId));
            svc.setUserInfo(userId, key, keyValue);
            svc.setUserInfo(userId, secret, secretValue);
        } catch (Exception e) {
            System.err
                    .println("Exception whilst setting up system user, could be already there so continuing...");
        }
    }

}
