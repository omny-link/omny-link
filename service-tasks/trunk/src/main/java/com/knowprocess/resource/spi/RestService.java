package com.knowprocess.resource.spi;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.delegate.JavaDelegate;
import org.activiti.engine.impl.context.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.knowprocess.resource.internal.UrlResource;

public abstract class RestService implements JavaDelegate {
    protected static final Logger LOGGER = LoggerFactory
            .getLogger(RestService.class);

    protected Expression resourceUsername;
    protected Expression resourcePassword;
    protected Expression globalResource;
    protected Expression headers;
    /**
     * Comma-separated list of key-value pairs. Key is variable to populate,
     * value is response header name.
     */
    protected Expression responseHeaders;
    protected Expression responseVar;
    /**
     * Comma-separated set of fields to POST to the REST resource in the form
     * key=value. May contain expressions.
     */
    protected Expression data;
    static final String USER_AGENT = "KnowProcess Agent";

    public void setGlobalResource(Expression globalResource) {
        this.globalResource = globalResource;
    }

    public void setResourceUsername(Expression resourceUsername) {
        this.resourceUsername = resourceUsername;
    }

    public void setResourcePassword(Expression resourcePassword) {
        this.resourcePassword = resourcePassword;
    }

    public void setHeaders(Expression headers) {
        this.headers = headers;
    }

    // public String getOutputVar() {
    // return outputVar;
    // }

    /** @deprecated Use #setResponseVar */
    public void setOutputVar(Expression outputVar) {
        this.responseVar = outputVar;
    }

    public void setResponseVar(Expression responseVar) {
        this.responseVar = responseVar;
    }

    protected Expression getExpression(String variable) {
        return Context.getProcessEngineConfiguration().getExpressionManager()
                .createExpression(variable);
    }

    protected String getStringFromExpression(Expression expression,
            DelegateExecution execution) {
        if (expression != null) {
            Object value = expression.getValue(execution);
            if (value != null) {
                return value.toString();
            }
        }
        return null;
    }

    protected Map<String, String> getRequestHeaders(String headers) {
        if (headers != null && headers.length() > 0) {
            List<String> headerList = Arrays.asList(headers.split(","));
            Map<String, String> headerMap = new HashMap<String, String>();
            for (String h : headerList) {
                headerMap.put(h.substring(0, h.indexOf(':')),
                        h.substring(h.indexOf(':') + 1));
            }
            return headerMap;
        } else {
            return Collections.emptyMap();
        }
    }

    protected String evalExpr(DelegateExecution execution, String expr) {
        // TODO This is a bit of a hack
        if (expr.contains("${")) {
            expr = getStringFromExpression(getExpression(expr), execution);
            System.out.println("  : " + expr);
        }
        return expr;
    }

    protected void sendData(HttpURLConnection connection, Object data) throws IOException {
        // Send request
        if (data != null) {
            // String bytes = URLEncoder.encode(
            // (String) data.getValue(execution), "UTF-8");
            String bytes = data.toString();
            LOGGER.debug("  Content-Length: "
                    + Integer.toString(bytes.length()));
            connection.setRequestProperty("Content-Length",
                    "" + Integer.toString(bytes.length()));
            // connection.setRequestProperty("Content-Language", "en-US");
            LOGGER.debug("==================== Data =======================");
            LOGGER.debug(bytes);
    
            DataOutputStream wr = new DataOutputStream(
                    connection.getOutputStream());
            wr.writeBytes(bytes);
            wr.flush();
            wr.close();
        }
    }

    protected void setAuthorization(String usr, String pwd, HttpURLConnection connection) {
        if (usr != null) {
            connection.setRequestProperty("Authorization",
                    UrlResource.getBasicAuthorizationHeader(usr, pwd));
        }
    }

    protected void setHeaders(HttpURLConnection connection, String headers) {
        connection.setRequestProperty("User-Agent", RestService.USER_AGENT);

        Map<String, String> headerMap = getRequestHeaders(headers);
        for (Entry<String, String> h : headerMap.entrySet()) {
            if (LOGGER.isDebugEnabled()) { 
                LOGGER.debug(String.format("  %1$s: %2$s", h.getKey(),
                        h.getValue()));
            }
            connection.setRequestProperty(h.getKey(), h.getValue());
        }
    }

}
