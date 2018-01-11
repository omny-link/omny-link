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
package link.omny.acctmgmt.web;

import link.omny.acctmgmt.model.SystemConfig;
import link.omny.acctmgmt.repositories.TenantRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/configuration")
public class SystemConfigController {

    protected static final Logger LOGGER = LoggerFactory
            .getLogger(SystemConfigController.class);

    @Autowired
    private SystemConfig systemConfig;

    @Autowired
    protected TenantRepository tenantRepo;

    @RequestMapping(method = RequestMethod.GET)
    public final @ResponseBody SystemConfig getSystemConfig() {
        LOGGER.info("getSystemConfig");

        return systemConfig;
    }
}
