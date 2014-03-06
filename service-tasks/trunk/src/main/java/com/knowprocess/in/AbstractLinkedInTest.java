package com.knowprocess.in;

import org.activiti.engine.IdentityService;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Rule;

import com.knowprocess.test.activiti.ExtendedRule;

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
				LinkedInTask.CONSUMER_KEY_KEY,
				consumerKey,
				LinkedInTask.CONSUMER_SECRET_KEY,
				consumerSecret);
	
		createTestUser(
				INITIATOR,
				LinkedInTask.ACCESS_TOKEN_KEY,
				accessToken,
				LinkedInTask.ACCESS_TOKEN_SECRET_KEY,
				accessSecret);
	}

	private void createTestUser(String userId, String key, String keyValue,
			String secret, String secretValue) {
				IdentityService svc = activitiRule.getIdentityService();
				svc.saveUser(svc.newUser(userId));
				svc.setUserInfo(userId, key, keyValue);
				svc.setUserInfo(userId, secret, secretValue);
			}

}
