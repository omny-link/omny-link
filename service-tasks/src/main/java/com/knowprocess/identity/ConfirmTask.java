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
package com.knowprocess.identity;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.IdentityService;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;

public class ConfirmTask implements JavaDelegate {

    private static final String VAR_CONFIRMATION = "CONFIRMATION";

    private static final String VAR_CONFIRMATION_CODE = "code";

    private static DatatypeFactory datatypeFactory;

    public ConfirmTask() {
        try {
            datatypeFactory = DatatypeFactory.newInstance();
        } catch (DatatypeConfigurationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            throw new ActivitiException(
                    "Configuration error with javax.xml.datatype", e);
        }
    }

    public void confirm(IdentityService idSvc, String username,
            String confirmationCode) {
        idSvc.setUserInfo(username, VAR_CONFIRMATION, datatypeFactory
                .newXMLGregorianCalendar().toXMLFormat());
    }

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        confirm(execution.getEngineServices().getIdentityService(),
                (String) execution.getVariable(IdentityTask.VAR_USERNAME),
                (String) execution.getVariable(VAR_CONFIRMATION_CODE));
    }

}
