package com.knowprocess.resource.internal;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.Scanner;

import org.apache.commons.codec.binary.Base64;

import com.knowprocess.resource.spi.Resource;

public class UrlResource implements Resource {
// creates a unique boundary for POST form encoding based on time stamp
private final String boundary = "===" + System.currentTimeMillis() + "===";
private static final String LINE_FEED = "\r\n";

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

    /**
     * Adds a form field to the request
     * 
     * @param name
     *            field name
     * @param value
     *            field value
     * @return
     */
    private String getFormField(String name, String value) {
        StringBuilder sb = new StringBuilder();
        sb.append("--" + boundary).append(LINE_FEED);
        sb.append("Content-Disposition: form-data; name=\"" + name + "\"")
                .append(LINE_FEED);
        // sb.append("Content-Type: text/plain; charset=" + charset).append(
        // LINE_FEED);
        sb.append(LINE_FEED);
        sb.append(value).append(LINE_FEED);
        return sb.toString();
    }


    private String toBytes(Map<String, String> data) {
        StringBuilder sb = new StringBuilder(); 
        for (Map.Entry<String, String> d : data.entrySet()) {
            sb.append(getFormField(d.getKey(), d.getValue()));
        }
        return sb.toString();
    }

    private String urlEncode(Map<String, String> data)
            throws UnsupportedEncodingException {
        StringBuilder sb = new StringBuilder("");
        for (Map.Entry<String, String> d : data.entrySet()) {
            if (sb.length() > 0) {
                sb.append("&");
            }
            sb.append(d.getKey() + "=" + d.getValue());
        }
        return sb.toString();
    }

    public InputStream getResource(String sUrl) throws IOException {
        return getResource(sUrl, "GET", "text/html", "text/html", null);
    }

    public InputStream getResource(String sUrl, String method,
            String contentType,
            String accept, Map<String, String> data) throws IOException {
        URL url;
        HttpURLConnection connection = null;
        InputStream is = null;
        try {
            // Create connection
            url = new URL(sUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(method);
            if ("multipart/form-data".equals(contentType)) {
                contentType += ("; boundary=" + boundary);
            }
            System.out.println("Content-Type: " + contentType);
            connection.setRequestProperty("Content-Type", contentType);
            System.out.println("User-Agent: KnowProcess Agent");
            connection.setRequestProperty("User-Agent", "KnowProcess Agent");
            // connection.setRequestProperty("Test", "Bonjour");
            System.out.println("Accept: " + accept);
            connection.setRequestProperty("Accept", accept);
            if (username != null) {
                String userpass = username + ":" + password;
                String basicAuth = "Basic "
                        + new String(new Base64().encode(userpass.getBytes()));
                System.out.println("Authorization: " + basicAuth);
                connection.setRequestProperty("Authorization", basicAuth);
            }

            connection.setUseCaches(false);
            connection.setDoInput(true);
            connection.setDoOutput(true);

            // Send request
            if (data != null) {
                // String bytes = toBytes(data);
                String bytes = urlEncode(data);
                System.out.println("Content-Length: "
                        + Integer.toString(bytes.length()));
                connection.setRequestProperty("Content-Length",
                        "" + Integer.toString(bytes.length()));
                // connection.setRequestProperty("Content-Language", "en-US");
                System.out
                        .println("==================== Data =======================");
                System.out.println(bytes);

                DataOutputStream wr = new DataOutputStream(
                        connection.getOutputStream());
                wr.writeBytes(bytes);
                wr.flush();
                wr.close();
            }

            int code = connection.getResponseCode();
            if (code >= HttpURLConnection.HTTP_BAD_REQUEST) {
                is = connection.getInputStream();
                String response = new Scanner(is).useDelimiter("\\A").next();
                System.out.println("Response: " + response);
                throw new IOException(String.valueOf(code));
            }
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
