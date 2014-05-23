package com.knowprocess.activiti.sugarcrm;

import org.activiti.engine.IdentityService;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.Expression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.knowprocess.crm.CrmRecord;
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

	protected static final Logger LOGGER = LoggerFactory
			.getLogger(SugarTask.class);

	public static final String SUGAR_URL = "sugarUrl";
	public static final String SUGAR_PASSWORD = "sugarPassword";
	public static final String SUGAR_USERNAME = "sugarUsername";
	protected static final int RESULT_START = 0;
	protected static final int RESULT_SIZE = 20;

	protected CrmService svc;

    protected Expression srcVar;

    protected Expression trgtVar;

	/**
	 * Default constructor. Used when executed as service task.
	 */
	public SugarTask() {
		svc = new SugarService();
	}

    public void setSourceVar(Expression srcVar) {
        this.srcVar = srcVar;
    }

    public void setTargetVar(Expression trgtVar) {
        this.trgtVar = trgtVar;
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
		LOGGER.debug("Sugar session id: " + session.getSessionId());
		return session;
	}

	protected SugarSession doSugarUserLogin(DelegateExecution execution,
			CrmService svc) {
		SugarSession session = (SugarSession) execution
				.getVariable("sugarSession");
		if (session == null) {
			String userId = (String) execution.getVariable("initiator");
			LOGGER.info("Logging into Sugar using credentials attached to "
							+ userId);
			IdentityService idSvc = execution.getEngineServices()
					.getIdentityService();
			String usr = idSvc.getUserInfo(userId, SUGAR_USERNAME);
			String pwd = idSvc.getUserInfo(userId, SUGAR_PASSWORD);
			String url = idSvc.getUserInfo(userId, SUGAR_URL);
			LOGGER.debug("usr:" + usr + ", pwd null?:" + (pwd == null)
					+ ", url:" + url);
			session = new SugarSession(usr, pwd, url);
		}
		svc.login(session);
		LOGGER.debug("session id: " + session.getSessionId());
		return session;
	}

    protected CrmRecord getSugarContact(DelegateExecution execution) {
        if (srcVar == null) {
            return (CrmRecord) execution.getVariable("sugarContact");
        } else {
            throw new RuntimeException(
                    "Implicit type conversion not yet implemented");
            // CrmRecord record = execution.getVariable(inputVarName);
            // return record;
        }
    }

    protected void putIdInContext(DelegateExecution execution, String id) {
        if (trgtVar != null) {
            execution.setVariable(trgtVar.getExpressionText(), id);
        }
    }
}
