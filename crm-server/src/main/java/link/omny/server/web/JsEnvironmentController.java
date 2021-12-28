/*******************************************************************************
 * Copyright 2015-2022 Tim Stephenson and contributors
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class JsEnvironmentController {

    protected static final Logger LOGGER = LoggerFactory
            .getLogger(JsEnvironmentController.class);

    public static final String ENV = "var $env = (function () {\n" +
            "  var me = {\n" +
            "    appName: '%1$s',\n" +
            "    server: '%2$s',\n" +
            "    tagLine: '%3$s'\n" +
            "  };\n" +
            "\n" +
            "  return me;\n" +
            "}());\n";

    @Value("${spring.application.name:CRM}")
    protected String appName;

    @Value("${crm.application.tag-line:}")
    protected String tagLine;

    @Value("${server.port:8080}")
    protected String serverPort;

    @Value("${crm.application.base-uri:}")
    protected String restBaseUri;

    public String getRestBaseUri() {
        if (restBaseUri.length() == 0) {
            restBaseUri = "http://localhost:" + serverPort;
        }
        return restBaseUri;
    }

    @RequestMapping(path = "/js/env.js", method = RequestMethod.GET)
    public final ResponseEntity<String> getEnvironment() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "text/javascript");
        return new ResponseEntity<String>(
                String.format(ENV, appName, getRestBaseUri(), tagLine),
                headers,
                HttpStatus.OK);
    }

}
