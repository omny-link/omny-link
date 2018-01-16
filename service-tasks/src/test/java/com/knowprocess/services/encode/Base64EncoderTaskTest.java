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
package com.knowprocess.services.encode;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Scanner;

import org.junit.Test;

public class Base64EncoderTaskTest {

    private static final String HTML = "<h1>Lorem Ipsum</h1>\n\n"
            + "<blockquote>\n"
            + "\"Neque porro quisquam est qui dolorem ipsum quia dolor sit amet, consectetur, adipisci velit...\"\n"
            + "<em>\"There is no one who loves pain itself, who seeks after it and wants to have it, simply because it is pain...\"</em>\n"
            + "</blockquote>";

    private static final String HTML_ENCODED_RESOURCE = "/docs/test.html.b64";

    private static final String PDF_RESOURCE = "/docs/test.pdf";

    private static final String PDF_ENCODED_RESOURCE = "/docs/test.pdf.b64";

    @Test
    public void testSimple() throws UnsupportedEncodingException {
        Base64EncoderTask svc = new Base64EncoderTask();
        String encoded = svc.execute("HelloWorld!".getBytes());
        // produced with 'echo -n HelloWorld! | base64'
        // where -n is needed to avoid new line at end
        assertEquals("SGVsbG9Xb3JsZCE=", encoded);
    }

    @Test
    public void testMultiLineHtml() {
        Base64EncoderTask svc = new Base64EncoderTask();
        try {
            String encoded = svc.execute(HTML.getBytes("UTF-8"));
            assertEquals(getClasspathResource(HTML_ENCODED_RESOURCE)
                    .replace("\r", ""), encoded.replace("\r", ""));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    @Test
    public void testPdf() throws UnsupportedEncodingException {
        Base64EncoderTask svc = new Base64EncoderTask();
        String encoded = svc
                .execute(getClasspathResource(PDF_RESOURCE).getBytes("UTF-8"));
        String expected = getClasspathResource(PDF_ENCODED_RESOURCE);
        assertEquals(expected.replace("\r", ""), encoded.replace("\r", ""));
    }

    @SuppressWarnings("resource")
    protected String getClasspathResource(String resource) {
        try (InputStream is = getClass().getResourceAsStream(resource)) {
            return new Scanner(is, "UTF-8").useDelimiter("\\A").next();
        } catch (Exception e) {
            e.printStackTrace();
            fail(String.format("Unable to read resource from %1$s",
                    resource));
        }
        return "";
    }
}
