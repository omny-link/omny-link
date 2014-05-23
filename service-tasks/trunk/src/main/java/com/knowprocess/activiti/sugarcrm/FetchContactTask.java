package com.knowprocess.activiti.sugarcrm;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.delegate.JavaDelegate;

import com.knowprocess.crm.CrmRecord;
import com.knowprocess.sugarcrm.api.SugarContact;
import com.knowprocess.sugarcrm.api.SugarSession;

/**
 * Create Sugar CRM Lead from an Activiti service task.
 * 
 * @author tstephen
 */
public class FetchContactTask extends SugarTask implements JavaDelegate {

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

            CrmRecord record = svc.getContact(session, query.getId());
            SugarContact contact = null;
            if (record instanceof SugarContact) {
                contact = (SugarContact) record;
            } else {
                contact = new SugarContact(record);
            }

            if (mimeType != null
                    && "application/json".equalsIgnoreCase(mimeType
                            .getExpressionText())) {
                if (trgtVar == null) {
                    execution.setVariable("sugarContact", contact.toJson());
                } else {
                    execution.setVariable(trgtVar.getExpressionText(),
                            contact.toJson());
                }
            } else {
                if (trgtVar == null) {
                    execution.setVariable("sugarContact", contact);
                } else {
                    execution.setVariable(trgtVar.getExpressionText(), contact);
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
