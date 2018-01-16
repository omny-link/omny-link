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
