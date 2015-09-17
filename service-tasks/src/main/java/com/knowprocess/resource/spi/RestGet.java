package com.knowprocess.resource.spi;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RestGet extends Fetcher implements JavaDelegate {
    protected static final Logger LOGGER = LoggerFactory
            .getLogger(RestGet.class);

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        String usr = getUsername(execution);
        String resource = null;
        if (globalResource != null) {
            resource = evalExpr(execution,
                lookup(execution, usr, globalResource));
        } else {
            resource = (String) execution.getVariable("resource");
        }
        LOGGER.info(String.format("GET %1$s as %2$s", resource, usr));

        String sel = selector == null ? null : (String) selector
                .getValue(execution);
        LOGGER.debug("Selector to use to extract part of result: " + sel);

        Map<String, String> requestHeaders = getRequestHeaders(execution);
        Map<String, List<String>> responseHeaders2 = new HashMap<String, List<String>>();

        InputStream is = null;
        try {
            is = getUrlResource(usr, getPassword(execution, usr)).getResource(
                    resource, "GET",
                    requestHeaders, responseHeaders2,
                    getStringFromExpression(data, execution));
            LOGGER.debug(String.format("ResponseHeaders: %1$s",
                    responseHeaders2));
            String[] sought = getResponseHeadersSought(execution);
            for (String s : sought) {
                String hdr = s.substring(s.indexOf('=') + 1);
                LOGGER.debug("Seeking header: " + hdr);
                if (responseHeaders2.containsKey(hdr)) {
                    LOGGER.debug(String.format("  ... setting: %1$s to %2$s", s
                            .substring(0, s.indexOf('=')), responseHeaders2
                            .get(hdr).get(0)));
                    execution.setVariable(s.substring(0, s.indexOf('=')),
                            responseHeaders2.get(hdr).get(0));
                }
            }

            if (responseVar == null) {
                LOGGER.debug("No response variable requested");
            } else if (is == null) {
                LOGGER.warn(String
                        .format("GET response contains no body, variable '%1$s' will be set to null",
                                responseVar.getExpressionText()));
                execution.setVariable(responseVar.getExpressionText(), null);
            } else {
                String response = new Scanner(is).useDelimiter("\\A").next();
                LOGGER.debug("resource:" + response);
                if (sel != null) {
                    response = extract(response, sel);
                }
                if (LOGGER.isDebugEnabled() && response != null) {
                    LOGGER.debug("Response starts: "
                            + response.substring(0,
                                    response.length() < 50 ? response.length()
                                            : 50));
                }

                execution
                        .setVariable(responseVar.getExpressionText(), response);
                LOGGER.debug(String.format("Setting %1$s to %2$s",
                        responseVar.getExpressionText(), response));
            }
        } finally {
            try {
                is.close();
            } catch (Exception e) {
                ;
            }
        }
    }
}
