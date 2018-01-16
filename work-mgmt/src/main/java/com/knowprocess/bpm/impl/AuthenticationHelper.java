/*******************************************************************************
 *Copyright 2011-2018 Tim Stephenson and contributors
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
