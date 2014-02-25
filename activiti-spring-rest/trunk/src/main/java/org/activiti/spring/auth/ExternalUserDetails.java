package org.activiti.spring.auth;

import java.util.Collection;
import java.util.LinkedList;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * 
 * @author tstephen
 */
public class ExternalUserDetails implements UserDetails {
	private static final long serialVersionUID = -7329960048878759841L;
	private String userName;
	private String password;
	private String forename;
	private String surname;
	private String email;
	private Collection<GrantedAuthority> authorities = new LinkedList<GrantedAuthority>();

	public ExternalUserDetails(String userName, String password) {
		this.userName = userName;
		this.password = password;
	}

	@Override
	public Collection<GrantedAuthority> getAuthorities() {
		return authorities;
	}

	@Override
	public String getPassword() {
		return password;
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
		return String.format("%1$s %2$s", this.forename, this.surname);
	}

	public String getEmail() {
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
