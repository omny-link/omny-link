package com.knowprocess.resource.spi;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.knowprocess.resource.internal.UrlResource;

public class RestDelete extends RestService implements JavaDelegate {
    protected static final Logger LOGGER = LoggerFactory
            .getLogger(RestDelete.class);

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        String usr = getUsername(execution);
        String resource = evalExpr(execution,
                lookup(execution, usr, globalResource));
        String pwd = getPassword(execution, usr);

        delete(resource, usr, pwd, (String) headers.getValue(execution), data.getValue(execution));
    }

    public void delete(String resource, String usr,
            String pwd, String headers, Object data) throws IOException {
        LOGGER.info(String.format("DELETEing to %1$s as %2$s", resource, usr));

        URL url;
        HttpURLConnection connection = null;
        try {
            url = UrlResource.getUrl(resource);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("DELETE");

            setHeaders(connection, headers);
            setAuthorization(usr, pwd, connection);

            connection.setUseCaches(false);
            connection.setDoInput(true);
            connection.setDoOutput(true);

            sendData(connection, data);

            int code = connection.getResponseCode();
            LOGGER.debug("Response code = " + code);
            if (code >= HttpURLConnection.HTTP_BAD_REQUEST) {
                LOGGER.error("Response code: " + code);
                throw new IOException(String.valueOf(code));
            }
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            String msg = "Exception during DELETE of " + resource + " as "
                    + usr;
            LOGGER.error(msg, e);
            throw new IOException(msg, e);
        }
    }
}
