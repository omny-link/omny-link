package com.knowprocess.resource.spi;

import java.security.Principal;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RestPut extends RestService implements JavaDelegate {
    protected static final Logger LOGGER = LoggerFactory
            .getLogger(RestPut.class);

    /* package private */ void put(String resource,
            String headerStr, Object payload, Principal principal) throws Exception {
        super.execute("PUT", resource, headerStr, payload, new String[0], principal);
    }

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        Principal principal = null;
        String resource = null;
        try {
            principal = getPrincipal(execution);
            resource = evalExpr(execution,
                    lookup(execution, principal.getName(), globalResource));

            String headerStr = (String) headers.getValue(execution);
            Object payload = data.getValue(execution);

            put(resource, headerStr, payload, principal);
        } catch (Exception e) {
            throwTaskException(execution, principal, "PUT", resource, e);
        }
    }
}
