package com.knowprocess.resource.spi;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.knowprocess.resource.internal.UrlResource;

// Initially supports Twilio URL Encoded POST but some prelim. support for Form 
// encoded that requires testing. 
public class RestPut extends RestService implements JavaDelegate {
    protected static final Logger LOGGER = LoggerFactory
            .getLogger(RestPut.class);

    public void execute(String usr, String pwd, String resource,
            String headerStr, Object payload) throws Exception {
        LOGGER.info(String.format("PUTing to %1$s as %2$s", resource, usr));

        URL url;
        HttpURLConnection connection = null;
        try {
            url = UrlResource.getUrl(resource);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("PUT");

            setHeaders(connection, headerStr);
            setAuthorization(usr, pwd, connection);

            connection.setUseCaches(false);
            connection.setDoInput(true);
            connection.setDoOutput(true);

            sendData(connection, payload);

            int code = connection.getResponseCode();
            if (code >= HttpURLConnection.HTTP_BAD_REQUEST) {
                throwException(connection, code);
            } else {
                LOGGER.debug("Response code: " + code);
            }
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            String msg = "Exception during PUT to " + resource + " as " + usr;
            LOGGER.error(msg, e);
            throw new IOException(msg, e);
        }
    }

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        String usr = getUsername(execution);
        String resource = evalExpr(execution,
                lookup(execution, usr, globalResource));
        String pwd = getPassword(execution, usr);

        String headerStr = (String) headers.getValue(execution);
        Object payload = data.getValue(execution);

        execute(usr, pwd, resource, headerStr, payload);
    }
}
