package com.knowprocess.resource.internal;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Date;

import com.knowprocess.resource.spi.Repository;

public class MemRepository implements Repository {

    private Object obj;

    @Override
    public void write(String resourceName, String mimeType, Date created,
            InputStream is) throws IOException {
        if (mimeType.startsWith("text") || mimeType.equals("application/json")) {
            Reader reader = null;
            StringBuffer sb = new StringBuffer();
            try {
                char[] buf = new char[1024];
                reader = new InputStreamReader(is, "UTF-8");
                while (reader.read(buf) != -1) {
                    sb.append(buf);
                    // need to reset to avoid carried over chars last time thru
                    buf = new char[1024];
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            this.obj = sb.toString();
        } else {
            throw new IllegalArgumentException(
                    "Only text/* mime types and application/json supported at this stage.");
        }
    }

    /**
     * @return The last object written to this repo cast to a String.
     */
    public String getString() {
        return (String) obj;
    }
}
