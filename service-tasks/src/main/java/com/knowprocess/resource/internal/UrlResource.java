/*******************************************************************************
 *Copyright 2011-2018 Tim Stephenson and contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package com.knowprocess.resource.internal;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Scanner;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.knowprocess.resource.spi.Resource;
import com.knowprocess.resource.spi.RestService;
import com.knowprocess.resource.spi.model.JwtUserPrincipal;

public class UrlResource implements Resource {
    public static final String HEADER_CONTENT_TYPE = "Content-Type";

    private static final Map<String, String> EMPTY_MAP = new HashMap<String, String>();

    protected static final Logger LOGGER = LoggerFactory
            .getLogger(UrlResource.class);

    // creates a unique boundary for POST form encoding based on time stamp
    private final String boundary = "===" + System.currentTimeMillis() + "===";

    private String password;
    private String username;

    private String authMethod;
    private String jwtLoginUrl;
    private String jwtToken;


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
        this.authMethod = "BASIC";
    }

    /**
     * Represents a URL resource protected with JWT authentication .
     * @param principal TODO
     *
     * @throws IOException If unable to authenticate
     */
    public UrlResource(JwtUserPrincipal principal) {
        this();
        this.username = principal.getName();
        this.password = principal.getPassword();
        this.authMethod = "JWT";
        this.jwtLoginUrl = principal.getJwtLoginUrl();
    }

    protected void applyAuthMethod(final Map<String, String> headers) throws IOException {
        switch (authMethod.toUpperCase()) {
        case "BASIC":
            headers.put("Authorization",
                    getBasicAuthorizationHeader(username, password));
            break;
        case "JWT":
            headers.put("X-Requested-With", "XMLHttpRequest");
            if (jwtToken == null) {
                login();
                headers.put("X-Authorization", "Bearer " + jwtToken);
            }
            break;
        default:
            LOGGER.warn("unknown authentication method %1$s", authMethod);
        }
    }

    protected void login() throws IOException {
        final Map<String, String> headers = new HashMap<String, String>();
        headers.put("X-Requested-With", "XMLHttpRequest");
        headers.put("Content-Type", "application/json");
        headers.put("Accept", "application/json");
        final String data = String.format("{\"username\":\"%1$s\",\"password\":\"%2$s\"}", username, password);
        final Map<String, List<String>> responseHeaders = new HashMap<String, List<String>>();
        try (InputStream is = getResource(jwtLoginUrl, "POST", headers, responseHeaders, data)) {
            @SuppressWarnings("resource")
            String response = new Scanner(is).useDelimiter("\\A").next();
            LOGGER.debug("Response: " + response);
            int start = response.indexOf("\"", response.indexOf("\"token\"")+7);
            jwtToken = response.substring(start+1, response.indexOf(',', start)-1);
        }
    }

    private String urlEncode(Map<String, String> data)
            throws UnsupportedEncodingException {
        StringBuilder sb = new StringBuilder("");
        for (Map.Entry<String, String> d : data.entrySet()) {
            if (sb.length() > 0) {
                sb.append("&");
            }
            sb.append(d.getKey() + "="
                    + URLEncoder.encode(d.getValue(), "UTF-8"));
        }
        return sb.toString();
    }

    public InputStream getResource(String sUrl) throws IOException {
        return getResource(sUrl, "GET", EMPTY_MAP, EMPTY_MAP);
    }

    /**
     * @deprecated Use {@link #getResource(String, String, Map, Map)

     */
    public InputStream getResource(String sUrl, String method,
            String contentType,
            String accept, Map<String, String> data) throws IOException {
        Map<String, String> headers = EMPTY_MAP;
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
        Scanner scanner = null;
        int code = -1;
        try {
            // Create connection
            LOGGER.debug("  " + method + ": " + sUrl);
            url = getUrl(sUrl);
            connection = (HttpURLConnection) url.openConnection();
            if (sUrl.startsWith("https://")) {
                ((HttpsURLConnection) connection)
                        .setSSLSocketFactory((SSLSocketFactory) SSLSocketFactory
                                .getDefault());
            }
            connection.setRequestMethod(method);
            String contentType = headers.get(HEADER_CONTENT_TYPE);
            if ("multipart/form-data".equals(contentType)) {
                contentType += ("; boundary=" + boundary);
                headers.put(HEADER_CONTENT_TYPE, contentType);
            }

            if (!headers.containsKey("User-Agent")) {
				headers.put("User-Agent", RestService.USER_AGENT);
            }

            if (username != null && !sUrl.equals(jwtLoginUrl)) {
                applyAuthMethod(headers);
            }

            logHeaders(headers);
            for (Entry<String, String> h : headers.entrySet()) {
                connection.setRequestProperty(h.getKey(), h.getValue());
            }

            connection.setUseCaches(false);
            connection.setDoInput(true);
            connection.setDoOutput(true);

            // Send request
            if (data != null && data.length() > 0) {
                String contentLength = Integer.toString(data.length());
                LOGGER.debug("  Content-Length: " + contentLength);
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

            code = connection.getResponseCode();
            Map<String, List<String>> headerFields = connection
                    .getHeaderFields();
            LOGGER.info(String.format("Response code: %1$d", code));
            logHeaderArrays(headerFields);

            responseHeaders.putAll(headerFields);
            if (code >= HttpURLConnection.HTTP_BAD_REQUEST) {
                is = connection.getInputStream();
                scanner = new Scanner(is);
                String response = scanner.useDelimiter("\\A").next();
                LOGGER.error("Response: " + response);
                throw new IOException(String.valueOf(code) + " " + response);
            }
            is = connection.getInputStream();
        } catch (IOException e) {
            throwException(connection, code);
        } catch (Exception e) {
            LOGGER.error(e.getClass().getName() + ": " + e.getMessage(), e);
        } finally {
            // if (connection != null) {
            // connection.disconnect();
            // }
            try {
                scanner.close();
            } catch (Exception e) {
                ;
            }
        }
        return is;
    }

    protected void throwException(HttpURLConnection connection, int code)
            throws IOException {
        Scanner scanner = null;
        try {
            scanner = new Scanner(connection.getErrorStream());
            String error = scanner.useDelimiter("\\A").next();
            String msg = "Response code: " + code + ", details: " + error;
            LOGGER.error(msg);
            throw new IOException(msg);
        } catch (NullPointerException | NoSuchElementException e) {
            String msg = "Response code: " + code + ", no details";
            LOGGER.error(msg);
            throw new IOException(msg);
        } finally {
            try {
                scanner.close();
            } catch (Exception e) {
                ;
            }
        }
    }

    public static URL getUrl(final String sUrl) throws MalformedURLException,
            URISyntaxException {
        URL u = new URL(sUrl);
        return new URI(
                u.getProtocol(),
                u.getAuthority(),
                u.getPath(),
                u.getQuery(),
                u.getRef()).
                toURL();
    }

    private void logHeaders(Map<String, String> headerFields) {
        if (LOGGER.isDebugEnabled()) {
            for (Entry<String, String> header : headerFields.entrySet()) {
                LOGGER.debug(String.format("  %1$s:%2$s", header.getKey(),
                        header.getValue()));
            }
        }
    }

    private void logHeaderArrays(Map<String, List<String>> headerFields) {
        if (LOGGER.isDebugEnabled()) {
            for (Entry<String, List<String>> header : headerFields
                    .entrySet()) {
                LOGGER.debug(String.format("  %1$s:%2$s", header.getKey(),
                        header.getValue()));
            }
        }
    }

    public static String getBasicAuthorizationHeader(String username,
            String password) {
        String userpass = username.trim() + ":" + password.trim();
        String basicAuth = "Basic "
                + new String(new Base64().encode(userpass.getBytes())).trim()
                        .replace("\n", "").replace("\r", "");
        return basicAuth;
    }

}
