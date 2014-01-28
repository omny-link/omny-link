package com.knowprocess.in;

import org.activiti.engine.IdentityService;

import com.google.code.linkedinapi.client.LinkedInApiClient;
import com.google.code.linkedinapi.client.LinkedInApiClientFactory;

/**
 * Base class for all LinkedIn tasks.
 * 
 * @author timstephenson
 * 
 */
public abstract class LinkedInTask {

	public static final String APP_USER_ID = "APP";
	/**
	 * Consumer Key
	 */
	public static final String CONSUMER_KEY_KEY = "consumerKey";
	/**
	 * Consumer Secret
	 */
	public static final String CONSUMER_SECRET_KEY = "consumerSecret";
	/**
	 * Access Token
	 */
	public static final String ACCESS_TOKEN_KEY = "token";
	/**
	 * Access Token Secret
	 */
	public static final String ACCESS_TOKEN_SECRET_KEY = "tokenSecret";
	/**
	 * Key identifying comma separated list of LinkedIn ids to message.
	 */
	protected static final String ID_LIST_KEY = "id";
	/**
	 * Key identifying message subject.
	 */
	protected static final String SUBJECT_KEY = "subject";
	/**
	 * Key identifying message body,
	 */
	protected static final String MESSAGE_KEY = "message";
	protected LinkedInApiClientFactory factory;
	protected IdentityService identityService;

	public void setIdentityService(IdentityService svc) {
		identityService = svc;
		final String consumerKeyValue = svc.getUserInfo(APP_USER_ID,
				CONSUMER_KEY_KEY);
		final String consumerSecretValue = svc.getUserInfo(APP_USER_ID,
				CONSUMER_SECRET_KEY);
		factory = LinkedInApiClientFactory.newInstance(consumerKeyValue,
				consumerSecretValue);
	}

	protected LinkedInApiClient getClient(String userId) {
		final String accessTokenValue = identityService.getUserInfo(userId,
				ACCESS_TOKEN_KEY);
		final String tokenSecretValue = identityService.getUserInfo(userId,
				ACCESS_TOKEN_SECRET_KEY);
	
		return factory.createLinkedInApiClient(accessTokenValue,
				tokenSecretValue);
	}

}
