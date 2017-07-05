package com.knowprocess.bpm.api;

import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.ProcessEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.knowprocess.bpm.model.UserRecord;

@Deprecated
public class ActivitiUserDetailsService implements UserDetailsService
/* , ApplicationListener<InteractiveAuthenticationSuccessEvent> */{

    protected static final Logger LOGGER = LoggerFactory
            .getLogger(ActivitiUserDetailsService.class);
    @Autowired
    private ProcessEngine processEngine;

    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException, DataAccessException {
        LOGGER.info("loadUserByUsername: " + username);
        new UserRecord().setProcessEngine(processEngine);

        try {
            return UserRecord.findUserRecord(username);
        } catch (ActivitiObjectNotFoundException e) {
            LOGGER.warn(String.format(
                    "Attempt to authenticate as unknown user: %1$s", username));
            throw new UsernameNotFoundException(username, e);
        }
    }

}
