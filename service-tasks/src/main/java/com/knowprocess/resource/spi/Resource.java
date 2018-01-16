/*******************************************************************************
 *Copyright 2011-2018 Tim Stephenson and contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package com.knowprocess.resource.spi;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

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
	 * @throws IOException
	 *             If cannot access resource, includes host not found.
	 */
	InputStream getResource(String uri) throws IOException;

    InputStream getResource(String sUrl, String method,
            Map<String, String> headers, Map<String, String> data)
            throws IOException;
}
