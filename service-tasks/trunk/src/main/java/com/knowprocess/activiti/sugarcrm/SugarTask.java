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

	public static final String SUGAR_URL = "sugarUrl";
	public static final String SUGAR_PASSWORD = "sugarPassword";
	public static final String SUGAR_USERNAME = "sugarUsername";
	protected static final int RESULT_START = 0;
	protected static final int RESULT_SIZE = 20;

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
					(String) execution.getVariable(SUGAR_USERNAME),
					(String) execution.getVariable(SUGAR_PASSWORD),
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
			String usr = idSvc.getUserInfo(userId, SUGAR_USERNAME);
			String pwd = idSvc.getUserInfo(userId, SUGAR_PASSWORD);
			String url = idSvc.getUserInfo(userId, SUGAR_URL);
			System.out.println("usr:" + usr + ", pwd null?:" + (pwd == null)
					+ ", url:" + url);
			session = new SugarSession(usr, pwd, url);
		}
		svc.login(session);
		System.out.println("session id: " + session.getSessionId());
		return session;
	}
}
