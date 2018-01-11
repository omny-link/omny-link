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
package com.knowprocess.services;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class PasswordGenerator implements JavaDelegate {

    protected static final Logger LOGGER = LoggerFactory
            .getLogger(PasswordGenerator.class);

    /**
     * Variable name (or expression resolving to variable) to store result.
     */
    protected Expression outputVariable;

    public String generate() {
        return java.util.UUID.randomUUID().toString();
    }

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        execution.setVariable(
                (String) outputVariable.getValue(execution), generate());
    }
    
}
