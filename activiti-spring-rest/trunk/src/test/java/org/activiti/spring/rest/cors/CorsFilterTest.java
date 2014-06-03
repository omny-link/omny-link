package org.activiti.spring.rest.cors;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

public class CorsFilterTest {

    protected CorsFilter filter;

    @Before
    public void setUp() {
        filter = new CorsFilter();
    }

    @Test
    public void testAllowedOrigins() {
        try {
            assertTrue(filter.isAllowed("null"));
            assertTrue(filter.isAllowed("http://localhost"));
            assertTrue(filter.isAllowed("http://localhost:80"));
            assertTrue(filter.isAllowed("https://localhost:443"));
            assertTrue(filter.isAllowed("http://localhost:8080"));
            assertTrue(filter.isAllowed("https://localhost:8443"));
            assertTrue(filter.isAllowed("http://localhost:8888"));
            assertTrue(filter.isAllowed("https://www.knowprocess.com"));
            assertTrue(filter.isAllowed("http://knowprocess.com"));
            assertTrue(filter.isAllowed("http://foo.knowprocess.com"));
            assertTrue(filter.isAllowed("https://bar.knowprocess.com:8888"));
        } catch (IOException e) {
            e.printStackTrace();
            fail();
        }
    }

}
