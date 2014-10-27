package com.knowprocess.activiti.sugarcrm;

import java.util.List;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.delegate.JavaDelegate;

import com.knowprocess.crm.CrmRecord;
import com.knowprocess.sugarcrm.api.SugarSession;

/**
 * Create Sugar CRM Lead from an Activiti service task.
 * 
 * @author tstephen
 */
public class SearchContactsTask extends SugarTask implements JavaDelegate {

    private Expression mimeType;

    public void setTargetMimeType(Expression mime) {
        this.mimeType = mime;
    }

    @Override
	public void execute(DelegateExecution execution) throws Exception {
        LOGGER.debug("entering FetchContactTask.execute");
		try {
			SugarSession session = doSugarUserLogin(execution, svc);

			for (String var : execution.getVariableNames()) {
                LOGGER.debug(var + " = " + execution.getVariable(var));
			}
            String json = (String) srcVar.getValue(execution);
            LOGGER.debug("JSON query:" + json);

            CrmRecord query = CrmRecord.parseFromJson(json);
            LOGGER.debug("contact:" + query.toJson());

            List<CrmRecord> contacts = svc.searchContacts(session, query,
					RESULT_SIZE, RESULT_START);
            LOGGER.info(String.format("Found %1$s matching contacts.",
                    contacts.size()));

            if (mimeType != null
                    && "application/json".equalsIgnoreCase(mimeType
                    .getExpressionText())) {
                if (trgtVar != null && contacts.size() == 1) {
                    execution.setVariable(trgtVar.getExpressionText(), contacts
                            .get(0).toJson());
                } else if (trgtVar != null) {
                    execution.setVariable(trgtVar.getExpressionText(),
                            svc.toJson(contacts));
                } else if (contacts.size() == 1) {
                    execution.setVariable("sugarContact", contacts.get(0)
                            .toJson());
                } else {
                    execution
                            .setVariable("sugarContacts", svc.toJson(contacts));
                }
            } else {
                if (contacts.size() == 1) {
                    execution.setVariable("sugarContact", contacts.get(0));
                } else {
                    execution.setVariable("sugarContacts", contacts);
                }
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
