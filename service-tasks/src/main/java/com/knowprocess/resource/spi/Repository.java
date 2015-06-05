package com.knowprocess.resource.spi;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

/**
 * Interface defining a repository to receive a resource.
 * 
 * @author timstephenson
 */
public interface Repository {

    void write(String resourceName, String mimeType, Date created,
            InputStream is)
            throws IOException;
}
