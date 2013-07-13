package com.knowprocess.collector.internal;

import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import net.iharder.Base64;

import com.knowprocess.collector.api.Poster;
import com.knowprocess.collector.api.Status;


public class DeliciousPoster extends AbstractPoster implements Poster {

    private static final String API = "https://api.del.icio.us/v1/posts/add";
    private String pass;
    private String user;

    public DeliciousPoster(String user, String pass) {
        if (user == null || pass == null) {
            throw new IllegalArgumentException(
                    "Both user and pass parameters are required.");
        }
        this.user = user;
        this.pass = pass;
    }

    public Status post(Status status) throws Exception {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(API);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty(
                    "Authorization",
                    "Basic "
                            + Base64.encodeBytes((user + ":" + pass).getBytes()));
            String urlParameters = "url=" + status.getUrl() + "&description="
                    + status.getText() + "&tags=" + status.getTags();
            System.out.println("urlParameters: " + urlParameters);
            // TODO map project or context to 'Stack'?
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type",
                    "application/x-www-form-urlencoded");
            conn.setRequestProperty("charset", "utf-8");
            conn.setRequestProperty("Content-Length",
                    "" + Integer.toString(urlParameters.getBytes().length));
            conn.setUseCaches(false);

            DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
            wr.writeBytes(urlParameters);
            wr.flush();
            wr.close();

            // <-- The request will stop here if running in Android.
            status.setCode(conn.getResponseCode());
            System.out.println(status.getCode());
            if (status.getCode() > 400) {
                throw new RuntimeException(status.getCode() + ":"
                        + conn.getResponseMessage());
            }

            InputStream is = conn.getInputStream();

            InputStreamReader isr = new InputStreamReader(is);
            char[] cbuf = new char[1024];
            StringWriter writer = new StringWriter();
            while (isr.read(cbuf) != -1) {
                writer.write(cbuf);
            }
            System.out.println("response: " + writer.getBuffer().toString());
        } catch (Exception e) {
            // TODO
            e.printStackTrace();
            throw e;
        } finally {
            conn.disconnect();
        }
        return status;
    }

}
