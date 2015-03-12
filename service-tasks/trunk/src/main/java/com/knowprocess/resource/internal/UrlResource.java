package com.knowprocess.resource.internal;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.knowprocess.resource.spi.Resource;

public class UrlResource implements Resource {
    public static final String HEADER_CONTENT_TYPE = "Content-Type";
    private static final String LINE_FEED = "\r\n";

    protected static final Logger LOGGER = LoggerFactory
            .getLogger(UrlResource.class);

    // creates a unique boundary for POST form encoding based on time stamp
    private final String boundary = "===" + System.currentTimeMillis() + "===";

    private String password;
    private String username;
    private static final Map<String, String> EMPTY_MAP = Collections.emptyMap();;

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
        return getResource(sUrl, "GET", "text/html", "text/html", EMPTY_MAP);
    }

    /**
     * @deprecated Use {@link #getResource(String, String, Map, Map)

     */
    public InputStream getResource(String sUrl, String method,
            String contentType,
            String accept, Map<String, String> data) throws IOException {
        Map<String, String> headers = new HashMap<String, String>();
        return getResource(sUrl, method, headers, data);
    }

    public InputStream getResource(String sUrl, String method,
            Map<String, String> headers, Map<String, String> data)
            throws IOException {
        Map<String, List<String>> responseHeaders = new HashMap<String, List<String>>();
        return getResource(sUrl, method, headers, responseHeaders,
                urlEncode(data));
    }

    public InputStream getResource(final String sUrl, final String method,
            final Map<String, String> headers,
            final Map<String, List<String>> responseHeaders, final String data)
            throws IOException {
        URL url;
        HttpURLConnection connection = null;
        InputStream is = null;
        try {
            // Create connection
            url = new URL(sUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(method);
            String contentType = headers.get("Content-Type");
            if ("multipart/form-data".equals(contentType)) {
                contentType += ("; boundary=" + boundary);
                headers.put("Content-Type", contentType);
            }

            for (Entry<String, String> h : headers.entrySet()) {
                connection.setRequestProperty(h.getKey(), h.getValue());
            }

            connection.setRequestProperty("User-Agent", "KnowProcess Agent");

            if (username != null) {
                connection.setRequestProperty("Authorization",
                        getBasicAuthorizationHeader(username, password));
            }

            connection.setUseCaches(false);
            connection.setDoInput(true);
            connection.setDoOutput(true);

            // Send request
            if (data != null && data.length() > 0) {
                String contentLength = Integer.toString(data.length());
                LOGGER.debug("Content-Length: " + contentLength);
                connection.setRequestProperty("Content-Length", contentLength);
                // connection.setRequestProperty("Content-Language", "en-US");
                LOGGER.debug("=================== Data ======================");
                LOGGER.debug(data);

                DataOutputStream wr = new DataOutputStream(
                        connection.getOutputStream());
                wr.writeBytes(data);
                wr.flush();
                wr.close();
            }

            int code = connection.getResponseCode();
            Map<String, List<String>> headerFields = connection
                    .getHeaderFields();
            LOGGER.info(String.format("Response code: %1$d", code));
            if (LOGGER.isDebugEnabled()) {
                for (Entry<String, List<String>> header : headerFields
                        .entrySet()) {
                    LOGGER.debug(String.format("  %1$s:%2$s", header.getKey(),
                            header.getValue()));
                }
            }
            responseHeaders.putAll(headerFields);
            if (code >= HttpURLConnection.HTTP_BAD_REQUEST) {
                is = connection.getInputStream();
                String response = new Scanner(is).useDelimiter("\\A").next();
                LOGGER.error("Response: " + response);
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

    public static String getBasicAuthorizationHeader(String username,
            String password) {
        String userpass = username.trim() + ":" + password.trim();
        String basicAuth = "Basic "
                + new String(new Base64().encode(userpass.getBytes())).trim();
        System.out.println(String.format("Authorization: '%1$s'", basicAuth));
        return basicAuth;
    }

}
