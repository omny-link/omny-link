package com.knowprocess.resource.internal;

import java.io.InputStream;

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

}
