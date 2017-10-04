package com.knowprocess.bpm.impl;

import java.security.Principal;

import org.activiti.engine.ActivitiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;

public class AuthenticationHelper {
    protected static final Logger LOGGER = LoggerFactory
            .getLogger(AuthenticationHelper.class);

    public static String getUserId(Authentication authentication) {
        if (authentication.getPrincipal() instanceof Principal) {
            String tmp = ((Principal) authentication.getPrincipal())
                    .getName();
            if (!tmp.contains("@")) { // i.e. not an email addr
                LOGGER.warn("Username '{}' is not an email address, ignoring, this may result in errors if the process author expected a username.", tmp);
            }
            return tmp;
        } else if (authentication.getPrincipal() instanceof String) {
            return (String) authentication.getPrincipal();
        } else {
            String msg = String.format("Authenticated but principal of unknown type {}",
            authentication.getPrincipal().getClass().getName());
            LOGGER.error(msg);
            throw new ActivitiException(msg);
        }
    }
}
