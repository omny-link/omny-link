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
