package com.knowprocess.identity;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.IdentityService;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;

public class ConfirmTask implements JavaDelegate {

    private static final String VAR_CONFIRMATION = "CONFIRMATION";

    private static final String VAR_CONFIRMATION_CODE = "code";

    private static DatatypeFactory datatypeFactory;

    public ConfirmTask() {
        try {
            datatypeFactory = DatatypeFactory.newInstance();
        } catch (DatatypeConfigurationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            throw new ActivitiException(
                    "Configuration error with javax.xml.datatype", e);
        }
    }

    public void confirm(IdentityService idSvc, String username,
            String confirmationCode) {
        idSvc.setUserInfo(username, VAR_CONFIRMATION, datatypeFactory
                .newXMLGregorianCalendar().toXMLFormat());
    }

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        confirm(execution.getEngineServices().getIdentityService(),
                (String) execution.getVariable(IdentityTask.VAR_USERNAME),
                (String) execution.getVariable(VAR_CONFIRMATION_CODE));
    }

}
