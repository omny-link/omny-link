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

import java.io.StringReader;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

import org.activiti.engine.IdentityService;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;

public class StoreUserInfo implements JavaDelegate {
	public static final String VAR_USER_INFO = "kp.userInfo";

	private IdentityService idSvc;

	public void setIdentityService(IdentityService identityService) {
		this.idSvc = identityService;
	}

	public void storeUserInfo(String username, String key, String value) {
		if (username == null || key == null) {
			throw new IllegalArgumentException(
					"Both username and key must be supplied");
		}
		idSvc.setUserInfo(username, key, value);
	}

	public void execute(DelegateExecution execution) {
        JsonObject obj = null;
        Object json = execution.getVariable(VAR_USER_INFO);
        if (json instanceof String) {
		JsonReader reader = Json.createReader(new StringReader((String) json));
            obj = reader.readObject();
		System.out.println("obj" + obj);
		System.out.println("parsed: " + this);
        } else if (json instanceof JsonObject) {
            obj = (JsonObject) json;
        }

        setIdentityService(execution.getEngineServices().getIdentityService());
        storeUserInfo(obj.getString("username"), obj.getString("key"),
                obj.getString("value"));
	}

}
