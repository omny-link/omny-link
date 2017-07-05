package com.knowprocess.resource.spi;

import java.io.IOException;
import java.security.Principal;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.knowprocess.resource.spi.model.PasswordUserPrincipal;

public class RestDelete extends RestService implements JavaDelegate {
    protected static final Logger LOGGER = LoggerFactory
            .getLogger(RestDelete.class);

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        Principal principal = null;
        String resource = null;
        try {
            principal = getPrincipal(execution);
            resource = evalExpr(execution,
                lookup(execution, principal.getName(), globalResource));

            delete(resource, (String) headers.getValue(execution), principal);
        } catch (Exception e) {
            throwTaskException(execution, principal, "PUT", resource, e);
        }
    }

    /**
     * @deprecated
     */
    public void delete(String resource, String usr,
            String pwd, String headers, Object data) throws IOException {
        super.execute("DELETE", resource, headers, null, new String[0],
                new PasswordUserPrincipal(usr, pwd));
    }

    public void delete(String resource, String headerStr, Principal principal)
            throws Exception {
        super.execute("DELETE", resource, headerStr, null, new String[0],
                principal);
    }

}
