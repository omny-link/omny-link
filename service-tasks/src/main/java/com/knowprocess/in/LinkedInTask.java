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

import org.activiti.engine.IdentityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	public static final String CONSUMER_KEY_KEY = "linkedIn.consumerKey";
	/**
	 * Consumer Secret
	 */
	public static final String CONSUMER_SECRET_KEY = "linkedIn.consumerSecret";
	/**
	 * Access Token
	 */
	public static final String ACCESS_TOKEN_KEY = "linkedIn.token";
	/**
	 * Access Token Secret
	 */
	public static final String ACCESS_TOKEN_SECRET_KEY = "linkedIn.secret";
	/**
	 * Key identifying comma separated list of LinkedIn ids to message.
	 */
	public static final String ID_LIST_KEY = "ids";
	/**
	 * Key identifying message subject.
	 */
	public static final String SUBJECT_KEY = "subject";
	/**
	 * Key identifying message body,
	 */
	public static final String MESSAGE_KEY = "message";

	protected static final Logger LOGGER = LoggerFactory
			.getLogger(LinkedInTask.class);

	protected LinkedInApiClientFactory factory;
	protected IdentityService identityService;
	public static final String ID_LIST_PREFIX = "id in";
	public static final String FILTER_KEY = "filter";

	/**
	 * Key identifying process variable containing list of LinkedIn connection
	 * objects.
	 */
	public static final String CONNECTION_LIST_KEY = "connections";

	public void setIdentityService(IdentityService svc) {
		LOGGER.debug("Injected identity service.");
		identityService = svc;
		final String consumerKeyValue = svc.getUserInfo(APP_USER_ID,
				CONSUMER_KEY_KEY);
		final String consumerSecretValue = svc.getUserInfo(APP_USER_ID,
				CONSUMER_SECRET_KEY);
		factory = LinkedInApiClientFactory.newInstance(consumerKeyValue,
				consumerSecretValue);
		LOGGER.debug("LinkedIn factory created ok.");
	}

	protected LinkedInApiClient getClient(String userId) {
		LOGGER.debug(String.format("Creating LinkedIn client for %1$s", userId));
		if (identityService == null) {
			String msg = "Must have injected identity service before here.";
			LOGGER.error(msg);
			throw new IllegalStateException(msg);
		}
		try {
			final String accessTokenValue = identityService.getUserInfo(userId,
					ACCESS_TOKEN_KEY);
			final String tokenSecretValue = identityService.getUserInfo(userId,
					ACCESS_TOKEN_SECRET_KEY);
			if (accessTokenValue == null || tokenSecretValue == null) {
				// evidence is that we don't get here, but just in case...
				throw new Exception();
			}
			return factory.createLinkedInApiClient(accessTokenValue,
					tokenSecretValue);
		} catch (Exception e) {
			LOGGER.error(String.format(
					"No LinkedIn credentials stored for %1$s", userId));
			throw new IllegalStateException(String.format(
					"No LinkedIn credentials stored for %1$s", userId));
		}
	}

}
