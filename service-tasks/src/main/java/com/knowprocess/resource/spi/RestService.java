package com.knowprocess.resource.spi;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.Principal;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Scanner;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.delegate.JavaDelegate;
import org.activiti.engine.impl.context.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.knowprocess.core.internal.BaseUserAwareTask;
import com.knowprocess.resource.internal.UrlResource;
import com.knowprocess.resource.spi.model.JwtUserPrincipal;
import com.knowprocess.resource.spi.model.PasswordUserPrincipal;
import com.knowprocess.test.mock.MockUrlResource;

public abstract class RestService extends BaseUserAwareTask implements
        JavaDelegate {
    protected static final Logger LOGGER = LoggerFactory
            .getLogger(RestService.class);

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

    /**
     * To support unit testing set this to the string response expected from the
     * URL in the real case.
     */
    private Expression mockResponse;

    static final String USER_AGENT = "KnowProcess Agent";

    private ThreadLocal<String> threadLocalJwtToken = new ThreadLocal<String>();

    private ThreadLocal<Integer> threadLocalRetry = new ThreadLocal<Integer>();

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

    public void setMockResponse(Expression mockResponse) {
        this.mockResponse = mockResponse;
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
        if (expr != null && expr.contains("${")) {
            expr = getStringFromExpression(getExpression(expr), execution);
            LOGGER.debug("  : " + expr);
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
            for (Entry<String, List<String>> h : connection.getRequestProperties().entrySet()) {
                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info(String.format("  %1$s: %2$s", h.getKey(),
                            h.getValue()));
                }
            }
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

    private void setAuthorization(Principal principal,
            HttpURLConnection connection) throws IOException {
        if (principal == null) {
            LOGGER.warn("Process is executing anonymously (no username), this may be a problem depending on the process.");
        } else if (principal instanceof JwtUserPrincipal) {
            connection.setRequestProperty("X-Requested-With", "XMLHttpRequest");
            if (connection.getURL().toExternalForm().equals(((JwtUserPrincipal) principal).getJwtLoginUrl())) {
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("Accept", "application/json");
            } else if (threadLocalJwtToken.get() == null) {
                String jwtToken = login((JwtUserPrincipal) principal);
                threadLocalJwtToken.set(jwtToken);
            } else {
                connection.setRequestProperty("X-Authorization", "Bearer " + threadLocalJwtToken.get());
            }
        } else if (principal instanceof PasswordUserPrincipal) {
            setAuthorization(principal.getName(), ((PasswordUserPrincipal) principal).getPassword(), connection);
        }
    }

    private String login(JwtUserPrincipal principal) throws IOException {
        final String headers = new StringBuilder()
                .append("X-Requested-With: XMLHttpRequest")
                .append("Content-Type: application/json")
                .append("Accept: application/json").toString();
        final String data = String.format("{\"username\":\"%1$s\",\"password\":\"%2$s\"}", principal.getName(), principal.getPassword());
        Map<String, Object> responses = execute("POST", principal.getJwtLoginUrl(), headers, data, null, principal);
        String response = (String) responses.get("body");
        LOGGER.debug("Response: " + response);
        int start = response.indexOf("\"", response.indexOf("\"token\"")+7);
        return response.substring(start+1, response.indexOf(',', start)-1);
    }

    protected void setHeaders(HttpURLConnection connection, String headers) {
        connection.setRequestProperty("User-Agent", RestService.USER_AGENT);

        Map<String, String> headerMap = getRequestHeaders(headers);
        for (Entry<String, String> h : headerMap.entrySet()) {
            connection.setRequestProperty(h.getKey(), h.getValue());
        }
    }

    protected Map<String, String> getRequestHeaders(DelegateExecution execution) {
        if (headers == null) {
            return new HashMap<String, String>();
        } else {
            return getRequestHeaders((String) headers.getValue(execution));
        }
    }

    protected String[] getResponseHeadersSought(DelegateExecution execution) {
        if (responseHeaders == null) {
            return new String[0];
        } else {
            return ((String) responseHeaders.getValue(execution)).split(",");
        }
    }

    /**
     * @deprecated See getUrlResource(DelegateExecution execution, String usr)
     */
    protected UrlResource getUrlResource(String usr, String pwd) throws IOException {
        return new UrlResource(usr, pwd);
    }

    /* package private */ UrlResource getUrlResource(DelegateExecution execution, String usr)
            throws IOException {
        UrlResource ur = null;
        if (mockResponse != null) {
            ur = new MockUrlResource(mockResponse.getExpressionText());
        } else if (usr == null) {
            ur = new UrlResource();
        } else if (jwtLoginResource == null) {
            ur = new UrlResource(usr, getPassword(execution, usr));
        } else {
            String jwtLoginUrl = evalExpr(execution, lookup(execution, usr, jwtLoginResource));;
            ur = new UrlResource(new JwtUserPrincipal(usr,getPassword(execution, usr),jwtLoginUrl));
        }
        return ur;
    }

    protected Principal getPrincipal(DelegateExecution execution)
            throws IOException {
        Principal principal = null;
        String usr = getUsername(execution);
        if (usr == null) {
            LOGGER.warn("Process is executing anonymously (no username), this may be a problem depending on the process. {}",
                    getProcessContextMessage(execution));
        } else if (jwtLoginResource == null) {
            principal = new PasswordUserPrincipal(usr, getPassword(execution, usr));
        } else {
            String jwtLoginUrl = evalExpr(execution, lookup(execution, usr, jwtLoginResource));;
            principal = new JwtUserPrincipal(usr,getPassword(execution, usr), jwtLoginUrl);
        }
        return principal;
    }

    protected void throwRestException(HttpURLConnection connection, int code)
            throws ActivitiException {
        try (Scanner scanner = new Scanner(connection.getErrorStream())) {
            String error = scanner.useDelimiter("\\A").next();
            String msg = "Response code: " + code + ", details: "
                    + error;
            LOGGER.error(msg);
            throw new ActivitiException(msg);
        } catch (NullPointerException e) {
            String msg = "Response code: " + code + ", no details";
            LOGGER.error(msg);
            throw new ActivitiException(msg);
        }
    }

    protected Map<String, Object> execute(String method, String resource, String headers,
            Object payload, String[] responseHeadersSought, Principal principal)
            throws IOException {
        return execute(method, resource, headers, payload, responseHeadersSought, null, principal);
    }

    protected Map<String, Object> execute(String method, String resource, String headers,
            Object payload, String[] responseHeadersSought, String responseBodyKey, Principal principal)
            throws IOException {
        URL url;
        HttpURLConnection connection = null;
        try {
            url = UrlResource.getUrl(resource);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(method);

            setHeaders(connection, headers);
            setAuthorization(principal, connection);

            connection.setUseCaches(false);
            connection.setDoInput(true);
            connection.setDoOutput(true);

            LOGGER.info("{} to {} as {}", method, resource, principal == null ? "anonymous" : principal.getName());
            sendData(connection, payload);

            int code = connection.getResponseCode();
            if (code == 401 && principal instanceof JwtUserPrincipal && threadLocalRetry.get() == null) {
                // try again after login
                threadLocalRetry.set(1);
                login((JwtUserPrincipal) principal);
                return execute( method, resource,  headers,
                        payload, responseHeadersSought, responseBodyKey, principal);
            } else if (code >= HttpURLConnection.HTTP_BAD_REQUEST) {
                unsetAuthorization(principal, connection);
                throwRestException(connection, code);
            } else {
                unsetAuthorization(principal, connection);
                LOGGER.info("Response code: " + code);
            }

            return extractResponses(connection, responseHeadersSought, responseBodyKey);
        } catch (URISyntaxException e) {
            String msg = String.format("Invalid URL: %1$s, cause: ", resource, e.getMessage());
            LOGGER.error(msg);
            throw new ActivitiException(msg);
        } finally {
            connection.disconnect();
        }
    }

    private void unsetAuthorization(Principal principal,
            HttpURLConnection connection) {
        if (principal instanceof JwtUserPrincipal
                && !connection.getURL().toExternalForm().equals(((JwtUserPrincipal) principal).getJwtLoginUrl())){
            threadLocalJwtToken.set(null);
            threadLocalRetry.set(null);
        }
    }

    private Map<String, Object> extractResponses(HttpURLConnection connection,
            String[] responseHeadersSought, String responseBodyKey) throws IOException {
        Map<String, Object> responses = new HashMap<String, Object>();
        Map<String, List<String>> headerFields = connection.getHeaderFields();
        logHeaderArrays(headerFields);
        if (responseHeadersSought != null) {
            for (String s : responseHeadersSought) {
                String hdr = s.substring(s.indexOf('=') + 1);
                LOGGER.debug("Seeking header: " + hdr);
                if (headerFields.containsKey(hdr)) {
                    String hdrVal = s.indexOf('=') != -1
                            ? s.substring(0, s.indexOf('=')) : s;
                    LOGGER.debug(String.format("  ... setting: %1$s to %2$s",
                            hdrVal, headerFields.get(hdr).get(0)));
                    responses.put(hdrVal, headerFields.get(hdr).get(0));
                }
            }
        }

        if (responseBodyKey == null) {
            LOGGER.warn("No response variable key specified, storing as 'body'");
            responseBodyKey = "body";
        }

        try (Scanner scanner = new Scanner(connection.getInputStream())) {
            responses.put(responseBodyKey , scanner.useDelimiter("\\A").next());
        } catch (NoSuchElementException e) {
            LOGGER.warn(e.getMessage());
        }
        return responses;
    }

    /* package private*/ void logHeaders(Map<String, String> headerFields) {
        if (LOGGER.isInfoEnabled()) {
            for (Entry<String, String> header : headerFields.entrySet()) {
                LOGGER.info(String.format("  %1$s:%2$s", header.getKey(),
                        header.getValue()));
            }
        }
    }

    /* package private*/ void logHeaderArrays(Map<String, List<String>> headerFields) {
        if (LOGGER.isInfoEnabled()) {
            for (Entry<String, List<String>> header : headerFields
                    .entrySet()) {
                LOGGER.info(String.format("  %1$s:%2$s", header.getKey(),
                        header.getValue()));
            }
        }
    }

    protected String getProcessContextMessage(DelegateExecution execution) {
        String msg = String
                .format("Process def: %1$s, instance: %2$s; activity id: %3$s",
                        execution.getProcessDefinitionId(),
                        execution.getProcessInstanceId(),
                        execution.getCurrentActivityId());
        return msg;
    }

    protected void throwTaskException(DelegateExecution execution,
            Principal principal, String method, String resource, Exception e) {
        String msg = String.format(
                "Exception during REST %1$s to '%2$s' for '%3$s'. %4$s:\n  %5$s",
                method, resource, principal == null ? "anonymous" : principal.getName(),
                getProcessContextMessage(execution), e.getMessage());
        LOGGER.error(msg);
        if (e.getCause() != null) {
            LOGGER.error(String.format("  caused by:%1s - %2$s",
                    e.getCause().getClass().getName(),
                    e.getCause().getMessage()));
        }
        if (e instanceof ActivitiException) {
            throw (ActivitiException) e;
        } else {
            throw new ActivitiException(msg);
        }
    }
}
