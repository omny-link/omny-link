package com.knowprocess.test.mock;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import com.knowprocess.resource.internal.UrlResource;

public class MockUrlResource extends UrlResource {

    private String response;

    public MockUrlResource(String response) {
        super();
        this.response = response;
    }

    @Override
    public InputStream getResource(String sUrl, String method,
            Map<String, String> headers,
            Map<String, List<String>> responseHeaders, String data)
            throws IOException {
        return new ByteArrayInputStream(response.getBytes());
    }

}
