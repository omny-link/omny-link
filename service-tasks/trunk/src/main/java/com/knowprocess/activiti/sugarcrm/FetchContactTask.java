package com.knowprocess.activiti.sugarcrm;

import java.util.List;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;

import com.knowprocess.crm.CrmRecord;
import com.knowprocess.sugarcrm.api.SugarSession;

/**
 * Create Sugar CRM Lead from an Activiti service task.
 * 
 * @author tstephen
 */
public class FetchContactTask extends SugarTask implements JavaDelegate {

	@Override
	public void execute(DelegateExecution execution) throws Exception {
		System.out.println("entering FetchContactTask.execute");
		try {
			SugarSession session = doSugarUserLogin(execution, svc);

			for (String var : execution.getVariableNames()) {
				System.out.println(var + " = " + execution.getVariable(var));
			}
			String msg = (String) execution.getVariable("query");
			CrmRecord query = CrmRecord.parseFromJson((String) execution
					.getVariable(msg));
			System.out.println("contact:" + query.toJson());
			List<CrmRecord> contacts = svc.searchContacts(session, query,
					RESULT_SIZE, RESULT_START);
			if (contacts.size() == 1) {
				execution.setVariable("sugarContact", contacts.get(0).toJson());
			} else {
				execution.setVariable("sugarContacts", svc.toJson(contacts));
			}
		} catch (ActivitiException e) {
			e.printStackTrace();
			throw e;
		} catch (Exception e) {
			e.printStackTrace();
			throw new ActivitiException(e.getMessage(), e.getCause());
		}
	}

}
