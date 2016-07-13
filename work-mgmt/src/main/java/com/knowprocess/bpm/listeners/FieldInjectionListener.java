package com.knowprocess.bpm.listeners;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.ExecutionListener;
import org.activiti.engine.delegate.Expression;

public class FieldInjectionListener implements ExecutionListener {

    private static final long serialVersionUID = 5254413459550266486L;

    private Expression varName;

    private Expression dynamicValue;

    public void notify(DelegateExecution execution) throws Exception {
        if (execution.getVariable(varName.getValue(execution).toString()) == null) {
            execution.setVariable(varName.getValue(execution).toString(),
                    dynamicValue.getValue(execution).toString());
        }
    }
}
