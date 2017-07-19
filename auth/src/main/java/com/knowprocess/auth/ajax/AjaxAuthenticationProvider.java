package com.knowprocess.auth.ajax;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.knowprocess.auth.model.UserContext;
import com.knowprocess.auth.user.model.User;
import com.knowprocess.auth.user.service.DatabaseUserService;

/**
 *
 */
@Component
public class AjaxAuthenticationProvider implements AuthenticationProvider {
    // private final BCryptPasswordEncoder encoder;
    private final DatabaseUserService userService;

    @Autowired
    public AjaxAuthenticationProvider(final DatabaseUserService userService/*
                                                                            * ,
                                                                            * final
                                                                            * BCryptPasswordEncoder
                                                                            * encoder
                                                                            */) {
        this.userService = userService;
        // this.encoder = encoder;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        Assert.notNull(authentication, "No authentication data provided");

        String username = (String) authentication.getPrincipal();
        String password = (String) authentication.getCredentials();

        User user = userService.getByUsername(username).orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        // if (!encoder.matches(password, user.getPassword())) {
        // throw new
        // BadCredentialsException("Authentication Failed. Username or Password not valid.");
        // }

        if (user.getRoles() == null) throw new InsufficientAuthenticationException("User has no roles assigned");

        // TODO check
        List<GrantedAuthority> authorities = user.getRoles().stream()
                .map(authority -> new SimpleGrantedAuthority(authority.getId()))
                .collect(Collectors.toList());

        UserContext userContext = UserContext.create(user.getUsername(), authorities);

        return new UsernamePasswordAuthenticationToken(userContext, null, userContext.getAuthorities());
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return (UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication));
    }
}
