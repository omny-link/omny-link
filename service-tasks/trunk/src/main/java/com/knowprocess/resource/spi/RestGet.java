package com.knowprocess.resource.spi;

import org.activiti.engine.delegate.JavaDelegate;


public class RestGet extends Fetcher implements JavaDelegate {

    // @Override
    // public void execute(DelegateExecution execution) throws Exception {
    // String resource = (String) globalResource.getValue(execution);
    // String usr = (String) (resourceUsername == null ? null
    // : resourceUsername.getValue(execution));
    // String pwd = (String) (resourcePassword == null ? null
    // : resourcePassword.getValue(execution));
    // System.out.println("PUTing to " + resource + " as " + usr);
    //
    // URL url;
    // HttpURLConnection connection = null;
    // // InputStream is = null;
    // try {
    // // Create connection
    // url = new URL(resource);
    // connection = (HttpURLConnection) url.openConnection();
    // connection.setRequestMethod("PUT");
    // connection.setRequestProperty("User-Agent", RestService.USER_AGENT);
    //
    // Map<String, String> headerMap = getRequestHeaders((String) headers
    // .getValue(execution));
    // for (Entry<String, String> h : headerMap.entrySet()) {
    // connection.setRequestProperty(h.getKey(), h.getValue());
    // }
    //
    // if (usr != null) {
    // String userpass = usr + ":" + pwd;
    // String basicAuth = "Basic "
    // + new String(new Base64().encode(userpass.getBytes()));
    // System.out.println("Authorization: " + basicAuth);
    // connection.setRequestProperty("Authorization", basicAuth);
    // }
    //
    // connection.setUseCaches(false);
    // connection.setDoInput(true);
    // connection.setDoOutput(true);
    //
    // // Send request
    // if (data != null) {
    // // String bytes = URLEncoder.encode(
    // // (String) data.getValue(execution), "UTF-8");
    // String bytes = (String) data.getValue(execution);
    // System.out.println("Content-Length: "
    // + Integer.toString(bytes.length()));
    // connection.setRequestProperty("Content-Length",
    // "" + Integer.toString(bytes.length()));
    // // connection.setRequestProperty("Content-Language", "en-US");
    // System.out
    // .println("==================== Data =======================");
    // System.out.println(bytes);
    //
    // DataOutputStream wr = new DataOutputStream(
    // connection.getOutputStream());
    // wr.writeBytes(bytes);
    // wr.flush();
    // wr.close();
    // }
    //
    // int code = connection.getResponseCode();
    // if (code >= HttpURLConnection.HTTP_BAD_REQUEST) {
    // System.out.println("Response code: " + code);
    // throw new IOException(String.valueOf(code));
    // }
    // // is = connection.getInputStream();
    // } catch (IOException e) {
    // throw e;
    // } catch (Exception e) {
    // // TODO log and potentially rethrow
    // e.printStackTrace();
    // } finally {
    // // if (connection != null) {
    // // connection.disconnect();
    // // }
    // }
    // }
}
