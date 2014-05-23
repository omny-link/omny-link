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

        CrmRecord contact = getSugarContact(execution);
        LOGGER.debug(String.format(
                "Contact: %1$s",
                contact == null ? "not found" : contact
                        .getNameValueListAsJson()));

		SugarAccount acct = (SugarAccount) execution
				.getVariable("sugarAccount");
        // Workaround for the fact Sugar does not create 1 to 1 associations.
        // TODO need to implement in JS script in ModeledConversionService
        if (acct == null) {
            acct = (SugarAccount) contact.getCustom("account");
        }
        LOGGER.debug(String.format("Account: %1$s", acct == null ? "not found"
                : acct.getNameValueListAsJson()));

		if (contact != null && acct != null) {
            CrmRecord newContact = svc.createAccountWithPrimeContact(session,
                    contact, acct);
            LOGGER.info(String
                    .format("Created contact with id %1$s and associated it to account",
                    newContact.getId()));
            putIdInContext(execution, newContact.getId());
		} else { 
			if (contact != null) {
                CrmRecord newContact = svc.createContact(session, contact);
                LOGGER.info(String.format("Created contact with id %1$s",
                        newContact.getId()));
                putIdInContext(execution, newContact.getId());
			}
			if (acct != null) {
                CrmRecord newAcct = svc.createAccount(session, acct);
                LOGGER.info(String.format("Created account with id %1$s",
                        newAcct.getId()));
                putIdInContext(execution, newAcct.getId());
			}
		}
	}

}
