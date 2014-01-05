package com.knowprocess.activiti.sugarcrm;

import org.activiti.engine.IdentityService;
import org.activiti.engine.delegate.DelegateExecution;

import com.knowprocess.crm.CrmService;
import com.knowprocess.sugarcrm.api.SugarService;
import com.knowprocess.sugarcrm.api.SugarSession;

/**
 * Base class for Activiti tasks accessing Sugar CRM.
 * 
 * @author tstephen
 * 
 */
public class SugarTask {

	protected CrmService svc;

	/**
	 * Default constructor. Used when executed as service task.
	 */
	public SugarTask() {
		svc = new SugarService();
	}

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
			System.out.println("usr:" + usr + ", pwd null?:" + (pwd == null)
					+ ", url:" + url);
			session = new SugarSession(usr, pwd, url);
		}
		svc.login(session);
		System.out.println("session id: " + session.getSessionId());
		return session;
	}
}
