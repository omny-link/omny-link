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
package com.knowprocess.bpm.listeners;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.ExecutionListener;
import org.activiti.engine.delegate.Expression;

import com.knowprocess.core.internal.UserInfoHelper;

public class FieldInjectionListener implements ExecutionListener {

    private static final long serialVersionUID = 5254413459550266486L;

    private UserInfoHelper userInfoHelper;

    private Expression varName;

    private Expression dynamicValue;

    protected UserInfoHelper getUserInfoHelper() {
        if (userInfoHelper == null) {
            userInfoHelper = new UserInfoHelper();
        }
        return userInfoHelper;
    }

    public void notify(DelegateExecution execution) throws Exception {
        if (execution.getVariable(varName.getValue(execution).toString()) == null) {
            String usr = getUserInfoHelper().lookupBotName(execution);
            Object val = dynamicValue.getValue(execution);
            if (val instanceof String && ((String) val).length() > 0 && ((String) val).contains("userInfo")) {
                val = getUserInfoHelper().lookup(execution, usr, dynamicValue);
            }
            execution.setVariable(varName.getValue(execution).toString(), val);
        }
    }
}
