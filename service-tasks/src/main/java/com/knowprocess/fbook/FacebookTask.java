package com.knowprocess.fbook;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;

public class FacebookTask implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        String msg = (String) execution.getVariable("facebookMsg");
        String username = (String) execution.getVariable("initiator");
        System.out
                .println("Hey dude imagine this really got posted to your facebook wall: ");
        System.out.println("  msg: " + msg);
        System.out.println("  on behalf of: " + username);
    }

}
