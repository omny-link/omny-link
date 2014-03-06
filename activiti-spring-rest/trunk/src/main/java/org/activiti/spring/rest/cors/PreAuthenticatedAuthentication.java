package org.activiti.spring.rest.cors;

import java.security.Principal;

import org.activiti.spring.rest.model.UserRecord;
import org.springframework.security.core.Authentication;

public class PreAuthenticatedAuthentication extends UserRecord
		implements Authentication, Principal {

	private static final long serialVersionUID = 123456780453475486L;

	public PreAuthenticatedAuthentication(String username) {
		super(username);
	}

	public void setName(String id) {
		super.setId(id);
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
		LOGGER.debug(String.format("get principal returns %1$s", getId()));
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

	@Override
	public String getName() {
		return getId();
	}

}
