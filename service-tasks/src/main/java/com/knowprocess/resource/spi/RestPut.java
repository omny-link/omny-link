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
package com.knowprocess.resource.spi;

import java.security.Principal;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RestPut extends RestService implements JavaDelegate {
    protected static final Logger LOGGER = LoggerFactory
            .getLogger(RestPut.class);

    /* package private */ void put(String resource,
            String headerStr, Object payload, Principal principal) throws Exception {
        super.execute("PUT", resource, headerStr, payload, new String[0], principal);
    }

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        Principal principal = null;
        String resource = null;
        try {
            principal = getPrincipal(execution);
            resource = evalExpr(execution,
                    lookup(execution, principal.getName(), globalResource));

            String headerStr = (String) headers.getValue(execution);
            Object payload = data.getValue(execution);

            put(resource, headerStr, payload, principal);
        } catch (Exception e) {
            throwTaskException(execution, principal, "PUT", resource, e);
        }
    }
}
