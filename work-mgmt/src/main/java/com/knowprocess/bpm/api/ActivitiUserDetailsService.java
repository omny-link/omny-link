/*******************************************************************************
 *Copyright 2018 Tim Stephenson and contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
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
