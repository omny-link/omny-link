package org.activiti.spring.rest.cors;

import java.security.Principal;

import org.activiti.spring.auth.ExternalUserDetails;
import org.springframework.security.core.Authentication;

public class PreAuthenticatedAuthentication extends ExternalUserDetails
		implements Authentication, Principal {

	private static final long serialVersionUID = 123456780453475486L;

	public PreAuthenticatedAuthentication(String extractPrincipal) {
		super(extractPrincipal, "");
	}

	public void setName(String e) {
		super.setUsername(e);
	}

	@Override
	public Object getCredentials() {
		LOGGER.debug("Call to get credentials returns n/a");
		return "N/A";
	}

	@Override
	public Object getDetails() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PreAuthenticatedAuthentication getPrincipal() {
		LOGGER.debug(String.format("get principal returns %1$s", getUsername()));
		return this;
	}

	@Override
	public boolean isAuthenticated() {
		LOGGER.debug(String.format("is authenticated returns %1$s", true));
		return true;
	}

	@Override
	public void setAuthenticated(boolean isAuthenticated)
			throws IllegalArgumentException {
	}

}
