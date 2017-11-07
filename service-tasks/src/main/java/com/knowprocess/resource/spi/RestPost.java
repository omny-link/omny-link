package com.knowprocess.resource.spi;

import java.io.InputStream;
import java.security.Principal;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
        Principal principal = null;
        String resource = null;
        try {
            principal = getPrincipal(execution);
            resource = evalExpr(execution,
                    lookup(execution, principal.getName(), globalResource));
            Map<String, String> requestHeaders = getRequestHeaders(execution,
                    getUsername(execution), (String) headers.getValue(execution));
            String[] responseHeadersSought = getResponseHeadersSought(execution);
            String contentType = requestHeaders.get("Content-Type");
            Map<String, Object> responses;
            if (contentType == null
                    || "application/x-www-form-urlencoded".equals(contentType)) {
                responses = postForm(getUrlResource(execution, principal.getName()),
                        resource, requestHeaders, responseHeadersSought,
                        getFormFields(execution, (String) data.getExpressionText()));
            } else {
                String resourceBodyKey = (String) (outputVar == null
                        ? "resource"
                        : outputVar.getValue(execution));
                responses = super.execute(
                        "POST", resource, requestHeaders,
                        evalExpression(data, execution),
                        responseHeadersSought, resourceBodyKey, principal);
            }

            for (Entry<String, Object> response : responses.entrySet()) {
                LOGGER.debug(String.format("Setting %1$s to %2$s",
                        response.getKey(), response));
                execution.setVariable(response.getKey(), response.getValue());
            }
        } catch (Exception e) {
            throwTaskException(execution, principal, "POST", resource, e);
        }
    }

    public Map<String, Object> postForm(UrlResource urlResource, String resource,
            Map<String, String> requestHeaders, String[] responseHeadersSought,
            Map<String, String> payload) throws Exception {
        LOGGER.info("postForm");

        Map<String, Object> responses = new HashMap<String, Object>();
        try (InputStream is = urlResource.getResource(
                resource, "POST", requestHeaders, payload)) {
            // TODO response headers

            if (outputVar == null) {
                LOGGER.debug("No response variable requested");
                responses.put("body",
                        new Scanner(is).useDelimiter("\\A").next());
            } else if (is == null) {
                LOGGER.warn(
                        "POST response contains no body, variable will be set to null");
                responses.put("body", null);
            } else {
                responses.put(outputVar.getExpressionText(),
                        new Scanner(is).useDelimiter("\\A").next());
            }
            // setVarsFromResponseHeaders();
        } catch (Exception e) {
            LOGGER.error(
                    String.format("Exception in %1$s", getClass().getName()),
                    e);
        }
        return responses;
    }

    /**
     * POST using BASIC authentication.
     */
    public Map<String, Object> post(String usr, String pwd, String resource,
            Map<String, String> requestHeaders, String[] responseHeadersSought,
            String payload) throws Exception {
        LOGGER.info(String.format("POSTing to %1$s as %2$s", resource, usr));

        UrlResource urlResource = new UrlResource(usr, pwd);
        Map<String, Object> responses = new HashMap<String, Object>();
        Map<String, List<String>> responseHeaders2 = new HashMap<String, List<String>>();
        try (InputStream is = urlResource.getResource(
                resource, "POST", requestHeaders, responseHeaders2, payload)) {
            LOGGER.debug(
                    String.format("ResponseHeaders: %1$s", responseHeaders2));

            for (String s : responseHeadersSought) {
                String hdr = s.substring(s.indexOf('=') + 1);
                LOGGER.debug("Seeking header: " + hdr);
                if (responseHeaders2.containsKey(hdr)) {
                    String hdrVal = s.indexOf('=') != -1
                            ? s.substring(0, s.indexOf('=')) : s;
                    LOGGER.debug(String.format("  ... setting: %1$s to %2$s",
                            hdrVal, responseHeaders2.get(hdr).get(0)));
                    responses.put(hdrVal, responseHeaders2.get(hdr).get(0));
                }
            }

            if (outputVar == null) {
                LOGGER.debug("No response variable requested");
            }
            if (is == null || Collections.singletonList("0")
                    .equals(responseHeaders2.get("Content-Length"))) {
                LOGGER.warn(
                        "POST response contains no body, variable will be set to null");
                responses.put("body", null);
            } else {
                responses.put("body",
                        new Scanner(is).useDelimiter("\\A").next());
            }
            // setVarsFromResponseHeaders();
        } catch (Exception e) {
            LOGGER.error(
                    String.format("Exception in %1$s", getClass().getName()),
                    e);
        }
        return responses;
    }

    private Map<String, String> getFormFields(DelegateExecution execution,
            String formFieldExpression) {
        List<String> ff = Arrays.asList(formFieldExpression.split(","));
        Map<String, String> data = new HashMap<String, String>();
        for (String field : ff) {
            LOGGER.debug(String.format("Field expression: %1$s", field));
            String tmp = (String) evalExpression(getExpression(field),
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
