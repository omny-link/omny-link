package com.knowprocess.activiti.sugarcrm;

import org.activiti.engine.IdentityService;
import org.activiti.engine.delegate.DelegateExecution;

import com.knowprocess.crm.CrmService;
import com.knowprocess.sugarcrm.api.SugarSession;

public class SugarTask {

	protected SugarSession doSugarLogin(DelegateExecution execution,
			CrmService svc) {
		SugarSession session = (SugarSession) execution
				.getVariable("sugarSession");
		if (session == null) {
			session = new SugarSession(
					(String) execution.getVariable("sugarUsername"),
					(String) execution.getVariable("sugarPassword"),
					(String) execution.getVariable("sugarBaseUrl"));
		}
		svc.login(session);
		System.out.println("session id: " + session.getSessionId());
		return session;
	}

	protected SugarSession doSugarUserLogin(DelegateExecution execution,
			CrmService svc) {
		SugarSession session = (SugarSession) execution
				.getVariable("sugarSession");
		if (session == null) {
			String userId = (String) execution.getVariable("initiator");
			System.out
					.println("Logging into Sugar using credentials attached to "
							+ userId);
			IdentityService idSvc = execution.getEngineServices()
					.getIdentityService();
			String usr = idSvc.getUserInfo(userId, "sugarUsername");
			String pwd = idSvc.getUserInfo(userId, "sugarPassword");
			String url = idSvc.getUserInfo(userId, "sugarUrl");
			session = new SugarSession(usr, pwd, url);
		}
		svc.login(session);
		System.out.println("session id: " + session.getSessionId());
		return session;
	}
}
