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

public class BpmPoster extends AbstractPoster implements Poster {

    private static final String API = "http://localhost:7273/task";
    private static final String SPACE = " ";
    private String pass;
    private String user;

    public BpmPoster(String user, String pass) {
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
            // String urlParameters = "url=" + status.getUrl() + "&description="
            // + status.getText() + "&tags=" + status.getTags();
            String body = status.getContext() + SPACE + status.getProject()
                    + SPACE + status.getTags() + SPACE + status.getText()
                    + SPACE + status.getUrl();
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            // conn.setRequestProperty("Content-Type",
            // "application/x-www-form-urlencoded");
            conn.setRequestProperty("charset", "utf-8");
            conn.setRequestProperty("Content-Length",
                    "" + Integer.toString(body.getBytes().length));
            conn.setUseCaches(false);

            DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
            wr.writeBytes(body);
            wr.flush();
            wr.close();

            // <-- The request will stop here if running in Android.
            status.setCode(conn.getResponseCode());
            System.out.println(status.getCode());
            switch (status.getCode()) {
            case 405:
            case 400:
            case 401:
            case 402:
            case 403:
            case 404:
                throw new RuntimeException(status.getCode() + ":"
                        + conn.getResponseMessage());
            }
            System.out.println("Location: " + conn.getHeaderField("Location"));

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
            ;
        }
        return status;
    }

}
