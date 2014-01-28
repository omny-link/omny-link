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

		System.out.println("Sending message to users with ids:"
				+ userIdsToMessage);
		client.sendMessage(userIdsToMessage, subject, message);
		System.out
				.println("Your message has been sent. Check the LinkedIn site for confirmation.");
	}

	@Override
	public void execute(DelegateExecution execution) throws Exception {
		setIdentityService(execution.getEngineServices().getIdentityService());
		sendMessage((String) execution.getVariable("initiator"),
				(String) execution.getVariable(SUBJECT_KEY),
				(String) execution.getVariable(MESSAGE_KEY),
				Arrays.asList(((String) execution.getVariable(ID_LIST_KEY))
						.split(",")));
	}

}
