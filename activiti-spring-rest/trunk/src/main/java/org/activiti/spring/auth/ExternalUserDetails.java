package org.activiti.spring.auth;

import java.util.Collection;
import java.util.LinkedList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * 
 * @author tstephen
 */
public class ExternalUserDetails implements UserDetails {
	private static final long serialVersionUID = -7329960048878769841L;
	
	protected static final Logger LOGGER = LoggerFactory
			.getLogger(ExternalUserDetails.class);

	private String userName;
	private String password;
	private String forename;
	private String surname;
	private String email;
	private Collection<GrantedAuthority> authorities = new LinkedList<GrantedAuthority>();

	public ExternalUserDetails(String userName, String password) {
		LOGGER.debug(String.format("Constructing user details for %1$s",
				userName));
		this.userName = userName;
		this.password = password;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return authorities;
	}

	@Override
	public String getPassword() {
		return password;
	}

	protected void setUsername(String s) {
		userName = s; 
	}
	
	@Override
	public String getUsername() {
		return userName;
	}

	public String getForename() {
		return forename;
	}

	public void setForename(String forename) {
		this.forename = forename;
	}

	public String getSurname() {
		return surname;
	}

	public void setSurname(String surname) {
		this.surname = surname;
	}

	public String getName() {
		LOGGER.debug(String.format("get name returns %1$s", userName));
		return userName;
	}

	public String getFullName() {
		return String.format("%1$s %2$s", this.forename, this.surname);
	}

	public String getEmail() {
		LOGGER.debug(String.format("get email returns %1$s", email));
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

}
