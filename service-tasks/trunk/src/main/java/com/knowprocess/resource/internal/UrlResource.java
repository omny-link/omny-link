package com.knowprocess.resource.internal;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.commons.codec.binary.Base64;

import com.knowprocess.resource.spi.Resource;

public class UrlResource implements Resource {

	private String password;
    private String username;

    public UrlResource() {
        super();
    }

    /**
     * Represents a URL resource protected with HTTP Basic authentication.
     * 
     * @param username
     * @param password
     */
    public UrlResource(String username, String password) {
        this();
        this.username = username.trim();
        this.password = password.trim();
    }

	public InputStream getResource(String sUrl) throws IOException {
        URL url;
        HttpURLConnection connection = null;
        InputStream is = null;
        try {
            // Create connection
            url = new URL(sUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            // connection.setRequestProperty("Content-Type", "text/html");
            // connection.setRequestProperty("Accept", "text/html");
            if (username != null) {
                String userpass = username + ":" + password;
                String basicAuth = "Basic "
                        + new String(new Base64().encode(userpass.getBytes()));
                connection.setRequestProperty("Authorization", basicAuth);
            }
            // connection.setRequestProperty("Content-Length",
            // "" + Integer.toString(data.getBytes().length));
            // connection.setRequestProperty("Content-Language", "en-US");

            connection.setUseCaches(false);
            connection.setDoInput(true);
            connection.setDoOutput(true);

            // Send request
            // DataOutputStream wr = new DataOutputStream(
            // connection.getOutputStream());
            // wr.writeBytes(data);
            // wr.flush();
            // wr.close();

            // int code = connection.getResponseCode();
            // if (code != 200) {
            // throw new IOException(String.valueOf(code));
            // }
            is = connection.getInputStream();
		} catch (IOException e) {
			throw e;
        } catch (Exception e) {
            // TODO log and potentially rethrow
            e.printStackTrace();
        } finally {
            // if (connection != null) {
            // connection.disconnect();
            // }
        }
        return is;
    }

}
