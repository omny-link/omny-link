/*******************************************************************************
 *Copyright 2015-2018 Tim Stephenson and contributors
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
package link.omny.custmgmt.web;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import org.springframework.web.util.UriComponentsBuilder;

public abstract class BaseTenantAwareController {
    static final Logger LOGGER = LoggerFactory
            .getLogger(BaseTenantAwareController.class);
    
    protected URI getGlobalUri(Object id) {
        try {
            UriComponentsBuilder builder = MvcUriComponentsBuilder
                    .fromController(getClass());
            String uri = builder.build().toUriString()
                    .replace("{tenantId}/", "");
            return new URI(uri
                    + "/" + id);
        } catch (URISyntaxException e) {
            LOGGER.error(e.getMessage(), e);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    protected URI getTenantBasedUri(String tenantId, Object id) {
        UriComponentsBuilder builder = MvcUriComponentsBuilder
                .fromController(getClass());
        HashMap<String, String> vars = new HashMap<String, String>();
        vars.put("tenantId", tenantId);
        vars.put("id", id.toString());
        return builder.path("/{id}").buildAndExpand(vars).toUri();
    }

}
