package com.knowprocess.resource.spi;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.knowprocess.resource.internal.UrlResource;

// Initially supports Twilio URL Encoded POST but some prelim. support for Form 
// encoded that requires testing. 
public class RestPost extends RestService implements JavaDelegate {
    protected static final Logger LOGGER = LoggerFactory
            .getLogger(RestPost.class);

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        String resource = (String) globalResource.getValue(execution);
        String usr = (String) (resourceUsername == null ? null
                : resourceUsername.getValue(execution));
        String pwd = (String) (resourcePassword == null ? null
                : resourcePassword.getValue(execution));
        LOGGER.info(String.format("POSTing to %1$s as %2$s", resource, usr));

        String response = null;
        InputStream is = null;
        try {
            Map<String, String> requestHeaders = getRequestHeaders(execution);
            String contentType = requestHeaders.get("Content-Type");
            if (contentType == null
                    || "application/x-www-form-urlencoded".equals(contentType)) {
                is = getUrlResource(usr, pwd).getResource(
                        resource,
                        "POST",
                        requestHeaders,
                        getFormFields(execution,
                                (String) data.getExpressionText()));
                // TODO response headers
            } else {
                Map<String, List<String>> responseHeaders2 = new HashMap<String, List<String>>();
                is = getUrlResource(usr, pwd).getResource(resource, "POST",
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

    private String[] getResponseHeadersSought(DelegateExecution execution) {
        if (responseHeaders == null) {
            return new String[0];
        } else {
            return ((String) responseHeaders.getValue(execution)).split(",");
        }
    }

    private Map<String, String> getRequestHeaders(DelegateExecution execution) {
        if (headers == null) {
            return Collections.emptyMap();
        } else {
            return getRequestHeaders((String) headers.getValue(execution));
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

    private UrlResource getUrlResource(String usr, String pwd) {
        UrlResource ur = null;
        if (usr == null || pwd == null) {
            ur = new UrlResource();
        } else {
            ur = new UrlResource(usr, pwd);
        }
        return ur;
    }
}
