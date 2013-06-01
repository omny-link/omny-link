package com.knowprocess.opml.api;

import java.io.InputStream;

/**
 * Service provider interface for parsing an OPML feed into a model bean.
 * 
 * @author tstephen
 */
public interface OpmlInput {

    OpmlFeed build(InputStream is);

}