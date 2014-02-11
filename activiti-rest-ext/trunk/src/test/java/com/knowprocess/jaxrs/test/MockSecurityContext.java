package com.knowprocess.jaxrs.test;

import java.security.Principal;

import javax.ws.rs.core.SecurityContext;

public class MockSecurityContext implements SecurityContext {

	public class MockPrincipal implements Principal {

		private String username;

		public MockPrincipal(String usr) {
			this.username = usr;
		}

		public String getName() {
			return username;
		}

	}

	private Principal principal;

	public MockSecurityContext(String usr) {
		principal = new MockPrincipal(usr);
	}

	public Principal getUserPrincipal() {
		return principal;
	}

	public boolean isUserInRole(String role) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isSecure() {
		// TODO Auto-generated method stub
		return false;
	}

	public String getAuthenticationScheme() {
		// TODO Auto-generated method stub
		return null;
	}

}
