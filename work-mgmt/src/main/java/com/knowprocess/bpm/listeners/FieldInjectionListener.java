package com.knowprocess.bpm.listeners;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.ExecutionListener;
import org.activiti.engine.delegate.Expression;

import com.knowprocess.core.internal.UserInfoHelper;

public class FieldInjectionListener implements ExecutionListener {

    private static final long serialVersionUID = 5254413459550266486L;

    private UserInfoHelper userInfoHelper;

    private Expression varName;

    private Expression dynamicValue;

    protected UserInfoHelper getUserInfoHelper() {
        if (userInfoHelper == null) {
            userInfoHelper = new UserInfoHelper();
        }
        return userInfoHelper;
    }

    public void notify(DelegateExecution execution) throws Exception {
        if (execution.getVariable(varName.getValue(execution).toString()) == null) {
            String usr = getUserInfoHelper().lookupBotName(execution);
            String val = getUserInfoHelper().lookup(execution, usr,
                    dynamicValue);
            execution.setVariable(varName.getValue(execution).toString(), val);
        }
    }
}
