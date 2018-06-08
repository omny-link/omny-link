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
package link.omny.server.web;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class JsEnvironmentController {

    protected static final Logger LOGGER = LoggerFactory
            .getLogger(JsEnvironmentController.class);

    @Value("${spring.application.name:Know Process}")
    protected String appName;

    @Value("${kp.application.tag-line:}")
    protected String tagLine;

    @Value("${server.port:8080}")
    protected String serverPort;

    @Value("${kp.application.base-uri:}")
    protected String restBaseUri;

    @Value("${kp.application.tenant-config-url:}")
    protected String tenantConfigUrl;

    public String getRestBaseUri() {
        if (restBaseUri.length() == 0) {
            restBaseUri = "http://localhost:" + serverPort;
        }
        return restBaseUri;
    }

    public String getTenantConfigUrl() {
        if (tenantConfigUrl.length() == 0) {
            tenantConfigUrl = getRestBaseUri() + "/tenants/omny.json";
        }
        return tenantConfigUrl;
    }

    @RequestMapping(path = "/js/env.js", method = RequestMethod.GET)
    public final String getEnvironment(HttpServletResponse resp, Model model)
            throws Exception {
        resp.setContentType("text/javascript");
        model.addAttribute("appName", appName);
        model.addAttribute("tagLine", tagLine);
        model.addAttribute("restBaseUri", getRestBaseUri());
        model.addAttribute("tenantConfig", getTenantConfigUrl());
        return "/js-env";
    }

}
