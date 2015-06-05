package com.knowprocess.resource.internal;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import com.knowprocess.resource.spi.Fetcher;
import com.knowprocess.resource.spi.Resource;

public class ClasspathResource implements Resource {

    public InputStream getResource(String uri) {
        if (uri.startsWith(Fetcher.PROTOCOL)) {
            uri = uri.substring(uri.indexOf(Fetcher.PROTOCOL) + Fetcher.PROTOCOL.length());
        }
        InputStream is = getClass().getResourceAsStream(uri);
        return is;
	}

    @Override
    public InputStream getResource(String sUrl, String method,
            Map<String, String> headers, Map<String, String> data)
            throws IOException {
        System.err
                .println("Ignoring method, headers and parameters - those are not yet implemented");
        return getResource(sUrl);
    }

}
