package org.activiti.spring.auth;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.dao.DataAccessException;
import org.springframework.security.authentication.event.InteractiveAuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.openid.OpenIDAttribute;
import org.springframework.security.openid.OpenIDAuthenticationToken;
//import org.springframework.security.oauth2.OAuth2AuthenticationToken;
//import org.springframework.security.openid.OpenIDAttribute;
//import org.springframework.security.openid.OpenIDAuthenticationToken;

public class ExternalUserDetailsService implements UserDetailsService, ApplicationListener<InteractiveAuthenticationSuccessEvent>{

	protected static final Logger LOGGER = LoggerFactory
			.getLogger(ExternalUserDetailsService.class);

	public UserDetails loadUserByUsername(String username)
			throws UsernameNotFoundException, DataAccessException {
		LOGGER.info("loadUserByUsername: " + username);
		return new ExternalUserDetails(username, "");
	}
	/** This method is called whenever authentication with a third party
	 * authentication provider (such as OpenID and Facebook) succeeded. We
	 * copy user information from the authentication provider into our system.
	 */
	@Override
	public void onApplicationEvent(InteractiveAuthenticationSuccessEvent event) {
		Authentication auth = event.getAuthentication();
		if(auth instanceof OpenIDAuthenticationToken) {
			// Make sure the details we have on records match the attributes we got from the provider
			OpenIDAuthenticationToken token = (OpenIDAuthenticationToken)auth;
			List<OpenIDAttribute> attributes = token.getAttributes();
			ExternalUserDetails userDetails = (ExternalUserDetails)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
			for(OpenIDAttribute attr : attributes) {
				// See sec:openid-attribute in spring config
				if(attr.getName().equals("email") && attr.getValues().get(0)!=null)
					userDetails.setEmail(attr.getValues().get(0));
				if(attr.getName().equals("forename")&& attr.getValues().get(0)!=null)
					userDetails.setForename(attr.getValues().get(0));
				if(attr.getName().equals("surname")&& attr.getValues().get(0)!=null)
					userDetails.setSurname(attr.getValues().get(0));
			}
			updateLocalUser(userDetails);
//		}else if(auth instanceof OAuth2AuthenticationToken) {
//			OAuth2AuthenticationToken oAuth2 = (OAuth2AuthenticationToken)auth;
//			if(oAuth2.getProviderAccountDetails() instanceof com.restfb.types.User) {
//				// This is a facebook user
//				com.restfb.types.User fbUser = (com.restfb.types.User)oAuth2.getProviderAccountDetails();
//				CustomUserDetails userDetails = (CustomUserDetails)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
//				String forename = null;
//				String surname = null;
//				if(!StringUtils.isBlank(fbUser.getFirstName()))
//					forename = fbUser.getFirstName();
//				if(!StringUtils.isBlank(fbUser.getLastName()))
//					surname = fbUser.getLastName();
//				// We don't use the email address from facebook, as it is a forwarding address
//				updateDbUser(userDetails, forename, surname, null);
//			}
		}
	}
	
	/**
	 * Override this method to provide the appropriate list of authorities
	 * (roles) and to store the authenticated user details locally.
	 * 
	 * The default implementation does not give any roles or store anything.
	 * 
	 * @param userDetails
	 *            Details received from the external authentication service.
	 */
	public void updateLocalUser(ExternalUserDetails userDetails) {
		;
	}
}
