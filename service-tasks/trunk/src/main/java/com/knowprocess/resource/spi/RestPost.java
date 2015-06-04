package com.knowprocess.resource.spi;

import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Initially supports Twilio URL Encoded POST but some prelim. support for Form 
// encoded that requires testing. 
public class RestPost extends RestService implements JavaDelegate {
    protected static final Logger LOGGER = LoggerFactory
            .getLogger(RestPost.class);

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        String usr = getUsername(execution);
        String resource = evalExpr(execution,
                lookup(execution, usr, globalResource));
        LOGGER.info(String.format("POSTing to %1$s as %2$s", resource, usr));

        String response = null;
        InputStream is = null;
        try {
            Map<String, String> requestHeaders = getRequestHeaders(execution);
            String contentType = requestHeaders.get("Content-Type");
            if (contentType == null
                    || "application/x-www-form-urlencoded".equals(contentType)) {
                is = getUrlResource(usr, getPassword(execution, usr)).getResource(
                        resource,
                        "POST",
                        requestHeaders,
                        getFormFields(execution,
                                (String) data.getExpressionText()));
                // TODO response headers
            } else {
                Map<String, List<String>> responseHeaders2 = new HashMap<String, List<String>>();
                is = getUrlResource(usr, getPassword(execution, usr)).getResource(resource, "POST",
                        requestHeaders, responseHeaders2,
                        getStringFromExpression(data, execution));
                LOGGER.debug(String.format("ResponseHeaders: %1$s",
                        responseHeaders2));
                String[] sought = getResponseHeadersSought(execution);
                for (String s : sought) {
                    String hdr = s.substring(s.indexOf('=') + 1);
                    LOGGER.debug("Seeking header: " + hdr);
                    if (responseHeaders2.containsKey(hdr)) {
                        LOGGER.debug(String.format(
                                "  ... setting: %1$s to %2$s",
                                s.substring(0, s.indexOf('=')),
                                responseHeaders2.get(hdr).get(0)));
                        execution.setVariable(s.substring(0, s.indexOf('=')),
                                responseHeaders2.get(hdr).get(0));
                    }
                }
            }

            if (responseVar == null) {
                LOGGER.debug("No response variable requested");
            } else if (is == null || is.available() == 0) {
                LOGGER.warn("POST response contains no body, variable will be set to null");
                execution.setVariable(responseVar.getExpressionText(), null);
            } else {
                response = new Scanner(is).useDelimiter("\\A").next();
                execution
                        .setVariable(responseVar.getExpressionText(), response);
                LOGGER.debug(String.format("Setting %1$s to %2$s",
                        responseVar.getExpressionText(), response));
            }
            // setVarsFromResponseHeaders();
        } catch (Exception e) {
            LOGGER.error(String.format("Exception in %1$s", getClass()
                    .getName()), e);
        } finally {
            try {
                is.close();
            } catch (Exception e) {
                ;
            }
        }
    }

    private Map<String, String> getFormFields(DelegateExecution execution,
            String formFieldExpression) {
        List<String> ff = Arrays.asList(formFieldExpression.split(","));
        Map<String, String> data = new HashMap<String, String>();
        for (String field : ff) {
            LOGGER.debug(String.format("Field expression: %1$s", field));
            String tmp = getStringFromExpression(getExpression(field),
                    execution);
            LOGGER.debug(String.format("Field: %1$s", tmp));
            String name = tmp.substring(0, tmp.indexOf('='));
            String value = tmp.substring(tmp.indexOf('=') + 1);
            value = evalExpr(execution, value);

            data.put(name, value);
        }
        return data;
    }
}
