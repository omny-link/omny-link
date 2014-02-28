package org.activiti.spring.rest.cors;

import java.security.Principal;
import java.util.Collection;
import java.util.Collections;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

public class PreAuthenticatedAuthentication implements Authentication,
		Principal {

	private static final long serialVersionUID = 123456780453475486L;

	private String email;

	public PreAuthenticatedAuthentication(String extractPrincipal) {
		this.email = extractPrincipal;
	}

	public void setName(String e) {
		email = e;
	}

	@Override
	public String getName() {
		return email;
	}

	public void setEmail(String e) {
		email = e;
	}

	public String getEmail() {
		return email;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return Collections.emptySet();
	}

	@Override
	public Object getCredentials() {
		return "N/A";
	}

	@Override
	public Object getDetails() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PreAuthenticatedAuthentication getPrincipal() {
		return this;
	}

	@Override
	public boolean isAuthenticated() {
		return true;
	}

	@Override
	public void setAuthenticated(boolean isAuthenticated)
			throws IllegalArgumentException {
	}

}
