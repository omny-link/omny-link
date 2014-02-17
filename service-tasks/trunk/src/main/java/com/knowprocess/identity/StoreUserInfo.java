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
		String json = (String) execution.getVariable(VAR_USER_INFO);
		JsonReader reader = Json.createReader(new StringReader(json));
		JsonObject obj = reader.readObject();
		System.out.println("obj" + obj);
		System.out.println("parsed: " + this);

		setIdentityService(execution.getEngineServices().getIdentityService());
		storeUserInfo(obj.getString("username"), obj.getString("key"),
				obj.getString("value"));
	}

}
