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

import java.util.Arrays;
import java.util.List;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;

import com.google.code.linkedinapi.client.LinkedInApiClient;

public class SendLinkedInMessageTask extends LinkedInTask implements
		JavaDelegate {

	public void sendMessage(String userId, String subject, String message,
			List<String> userIdsToMessage) {
		if (factory == null) {
			throw new IllegalStateException(
					"Must have called setIdentityService before sendMessage");
		}
		final LinkedInApiClient client = getClient(userId);

		if (userIdsToMessage.size() == 0) {
			userIdsToMessage.add("~"); // mail self
		}
		LOGGER.debug(String
				.format("Sending message '%1$s' to users with ids: %2$s, with credentials attached to %3$s",
						subject, userIdsToMessage, userId));
		client.sendMessage(userIdsToMessage, subject, message);
		LOGGER.debug("Your message has been sent. Check the LinkedIn site for confirmation.");
	}

	@Override
	public void execute(DelegateExecution execution) throws Exception {
		LOGGER.debug("Invoked send LinkedIn message task");
		setIdentityService(execution.getEngineServices().getIdentityService());
		Object ids = execution.getVariable(ID_LIST_KEY);
		if (ids instanceof String) {
			ids = Arrays.asList(((String) ids).split(","));
		} else if (!(ids instanceof List)) {
			throw new IllegalArgumentException(String.format(
					"Process variable %1$s must be either a String or List",
					ID_LIST_KEY));
		}
		sendMessage((String) execution.getVariable("initiator"),
				(String) execution.getVariable(SUBJECT_KEY),
				(String) execution.getVariable(MESSAGE_KEY), (List<String>) ids);
	}

}
