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
package com.knowprocess.bpm;

import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.cfg.MailServerInfo;
import org.activiti.spring.boot.ActivitiProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component
@Primary
@ConfigurationProperties(prefix = "spring.activiti.multiTenant")
public class MultiTenantActivitiProperties extends ActivitiProperties {
    private Map<String, MailServerInfo> servers = new HashMap<String, MailServerInfo>();

    public Map<String, MailServerInfo> getServers() {
        return this.servers;
    }

}
