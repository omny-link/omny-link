package com.knowprocess.logging;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;

public class LoggingService implements JavaDelegate {

	@Override
	public void execute(DelegateExecution execution) throws Exception {

		for (String name : execution.getVariableNames()) {
			System.out.println(name + " = " + execution.getVariable(name));
		}
	}

}
