/*******************************************************************************
 *Copyright 2018 Tim Stephenson and contributors
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
