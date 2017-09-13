package com.knowprocess.resource.spi;

import java.security.Principal;
import java.util.Map;
import java.util.Map.Entry;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RestGet extends RestService implements JavaDelegate {
    protected static final Logger LOGGER = LoggerFactory
            .getLogger(RestGet.class);

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        Principal principal = null;
        String resource = null;
        String[] responsesSought = getResponseHeadersSought(execution);
        String resourceBodyKey = (String) (responseVar == null ? "resource"
                : responseVar.getValue(execution));
        try {
            principal = getPrincipal(execution);
            resource = principal == null
                    ? evalExpr(execution, globalResource.getExpressionText())
                    : evalExpr(execution, lookup(execution, principal.getName(),
                            globalResource));

            LOGGER.warn(String.format("Seeking %1$s", resourceBodyKey));
            Map<String, Object> responses = super.execute(
                    "GET", resource,
                    getRequestHeaders(execution, getUsername(execution),
                            (String) headers.getValue(execution)),
                    evalExpression(data, execution),
                    responsesSought, resourceBodyKey, principal);

            for (Entry<String, Object> response : responses.entrySet()) {
                LOGGER.debug(String.format("Setting %1$s to %2$s",
                        response.getKey(), response.getValue()));
                execution.setVariable(response.getKey(), response.getValue());
            }
        } catch (Exception e) {
            throwTaskException(execution, principal, "GET", resource, e);
        }
    }
}
