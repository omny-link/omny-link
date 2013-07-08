package com.knowprocess.resource.spi;

import java.util.Set;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;

/**
 * The main entry point for the library.
 * 
 * <p>
 * Fundamentally this class expects a <code>Resource</code> to be fetched into a
 * <code>Repository</code>.
 * 
 * @author timstephenson
 */
public class Logger implements JavaDelegate {

    public Logger() {
        super();
    }


    @Override
    public void execute(DelegateExecution execution) throws Exception {
        Set<String> variableNames = execution.getVariableNames();
        for (String varName : variableNames) {
            System.out.println(varName + ":" + execution.getVariable(varName));
        }
    }
}
