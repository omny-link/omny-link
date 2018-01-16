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
package com.knowprocess.in;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;

import com.google.code.linkedinapi.client.LinkedInApiClient;

public class PostLinkedInStatusTask extends LinkedInTask implements
		JavaDelegate {

	public void postStatus(String userId, String status) {
		final LinkedInApiClient client = getClient(userId);
		client.updateCurrentStatus(status);
		LOGGER.debug("Your status has been posted. Check the LinkedIn site for confirmation.");
	}

	public void removeCurrentStatus(String userId) {
		final LinkedInApiClient client = getClient(userId);
		client.deleteCurrentStatus();
		LOGGER.debug("Your status has been deleted. Check the LinkedIn site for confirmation.");
	}

	@Override
	public void execute(DelegateExecution arg0) throws Exception {
		LOGGER.debug("Called post LinkedIn message task, but unfortunately it's not yet implemented!");
	}

}
