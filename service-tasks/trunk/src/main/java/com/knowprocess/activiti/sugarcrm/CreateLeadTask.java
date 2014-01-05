package com.knowprocess.activiti.sugarcrm;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;

import com.knowprocess.sugarcrm.api.SugarLead;
import com.knowprocess.sugarcrm.api.SugarSession;

/**
 * Create Sugar CRM Lead from an Activiti service task.
 * 
 * @author tstephen
 */
public class CreateLeadTask extends SugarTask implements JavaDelegate {

	@Override
	public void execute(DelegateExecution execution) throws Exception {
		SugarSession session = doSugarUserLogin(execution, svc);

		SugarLead lead = (SugarLead) execution.getVariable("sugarLead");
		System.out.println("lead:" + lead.getNameValueListAsJson());
		execution.setVariable("sugarLead", svc.createLead(session, lead));
	}

}
