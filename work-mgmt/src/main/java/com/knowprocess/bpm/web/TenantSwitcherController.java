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
package com.knowprocess.bpm.web;

import org.activiti.engine.IdentityService;
import org.activiti.engine.ProcessEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.knowprocess.bpm.model.UserInfoKeys;

@Controller
@RequestMapping("/admin/tenant")
public class TenantSwitcherController {

    protected static final Logger LOGGER = LoggerFactory
            .getLogger(TenantSwitcherController.class);

    @Autowired(required = true)
    ProcessEngine processEngine;

    @RequestMapping(value = "/{username}/{tenantId}", method = RequestMethod.PUT)
    @ResponseBody
    public void switchAccountToTenant(
            @PathVariable("username") String username,
            @PathVariable("tenantId") String tenantId) {
        LOGGER.info(String.format("Changing %1$s to tenant %2$s", username,
                tenantId));

        IdentityService idSvc = processEngine.getIdentityService();
        idSvc.setUserInfo(username, UserInfoKeys.TENANT.toString(), tenantId);
    }
}
