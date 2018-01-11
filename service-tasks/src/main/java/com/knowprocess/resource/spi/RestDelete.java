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
package com.knowprocess.resource.spi;

import java.io.IOException;
import java.security.Principal;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.knowprocess.resource.spi.model.PasswordUserPrincipal;

public class RestDelete extends RestService implements JavaDelegate {
    protected static final Logger LOGGER = LoggerFactory
            .getLogger(RestDelete.class);

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        Principal principal = null;
        String resource = null;
        try {
            principal = getPrincipal(execution);
            resource = evalExpr(execution,
                lookup(execution, principal.getName(), globalResource));

            delete(resource, (String) headers.getValue(execution), principal);
        } catch (Exception e) {
            throwTaskException(execution, principal, "PUT", resource, e);
        }
    }

    /**
     * @deprecated
     */
    public void delete(String resource, String usr,
            String pwd, String headers, Object data) throws IOException {
        super.execute("DELETE", resource, headers, null, new String[0],
                new PasswordUserPrincipal(usr, pwd));
    }

    public void delete(String resource, String headerStr, Principal principal)
            throws Exception {
        super.execute("DELETE", resource, headerStr, null, new String[0],
                principal);
    }

}
