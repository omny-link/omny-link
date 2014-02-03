package com.knowprocess.activiti.sugarcrm;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;

import com.knowprocess.crm.CrmRecord;
import com.knowprocess.sugarcrm.api.SugarAccount;
import com.knowprocess.sugarcrm.api.SugarSession;

/**
 * Create and link Sugar CRM Contact and Account from an Activiti service task.
 * 
 * @author tstephen
 */
public class CreateContactAndAccountTask extends SugarTask implements
		JavaDelegate {

	@Override
	public void execute(DelegateExecution execution) throws Exception {
		SugarSession session = doSugarUserLogin(execution, svc);

		CrmRecord contact = (CrmRecord) execution
				.getVariable("sugarContact");
		System.out.println("contact:" + contact.getNameValueListAsJson());

		SugarAccount acct = (SugarAccount) execution
				.getVariable("sugarAccount");
		System.out.println("acct:" + acct.getNameValueListAsJson());

		if (contact != null && acct != null) {
			svc.createAccountWithPrimeContact(session, contact, acct);
		} else { 
			if (contact != null) {
				svc.createContact(session, contact);
			}
			if (acct != null) {
				svc.createAccount(session, acct);
			}
		}
	}

}
