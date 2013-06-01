package com.knowprocess.resource.spi;

import java.io.InputStream;

/**
 * Interface providing an executable specification.
 * 
 * The specification should be an HTML string fragment containing:
 * <ul>
 * <li>A table.
 * <li>A table header row containing zero or more columns. Each column should
 * start with a single character to denote the type of parameter:
 * <ul>
 * <li>I = Input
 * <li>R = Response code
 * <li>O = Output
 * </ul>
 * <li>One table body row for each example specification.
 * 
 * @author timstephenson
 * 
 */
public interface Resource {

	    /**
     * Fetch the executable specification.
     * 
     * @param uri
     *            The URI of the specification. Different implementations may
     *            interpret this as a URL or some other identifier by which to
     *            locate the specification table.
     * @return HTML string as described at the interface level.
     */
	InputStream getResource(String uri);
}
