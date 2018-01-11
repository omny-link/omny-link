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

import java.security.Principal;
import java.util.Map;
import java.util.Map.Entry;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RestGet extends RestService implements JavaDelegate {
    protected static final Logger LOGGER = LoggerFactory
            .getLogger(RestGet.class);

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        Principal principal = null;
        String resource = null;
        String[] responsesSought = getResponseHeadersSought(execution);
        String resourceBodyKey = (String) (outputVar == null ? "resource"
                : outputVar.getValue(execution));
        try {
            principal = getPrincipal(execution);
            resource = principal == null
                    ? evalExpr(execution, globalResource.getExpressionText())
                    : evalExpr(execution, lookup(execution, principal.getName(),
                            globalResource));

            LOGGER.warn(String.format("Seeking %1$s", resourceBodyKey));
            String sHeaders = headers == null ? null : (String) headers.getValue(execution);
            Map<String, Object> responses = super.execute(
                    "GET", resource,
                    getRequestHeaders(execution, getUsername(execution), sHeaders),
                    evalExpression(data, execution),
                    responsesSought, resourceBodyKey, principal);

            for (Entry<String, Object> response : responses.entrySet()) {
                LOGGER.debug(String.format("Setting %1$s to %2$s",
                        response.getKey(), response.getValue()));
                execution.setVariable(response.getKey(), response.getValue());
            }
        } catch (Exception e) {
            throwTaskException(execution, principal, "GET", resource, e);
        }
    }
}
