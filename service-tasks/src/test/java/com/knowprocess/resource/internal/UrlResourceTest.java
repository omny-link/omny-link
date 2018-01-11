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
package com.knowprocess.resource.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;

import org.junit.Assume;
import org.junit.Test;

import com.knowprocess.resource.spi.model.JwtUserPrincipal;

public class UrlResourceTest {

    private static final String IMAGE_URL = "http://farm4.staticflickr.com/3140/3094868910_41c19ce2a3_b_d.jpg";
    private String imageDestFile = "target" + File.separator + "test-classes"
            + File.separator + "tux.jpg";

    private static final String JWT_PROTECTED_URL = "http://localhost:8082/tenants/omny.json";
    private String usr;
    private String pwd;
    private String jwtLoginUrl;
    private String destFile = "target" + File.separator + "test-classes"
            + File.separator + "tenant.json";

    @Test
    public void testUrlEncode() {
        String sUrl = "http://api.knowprocess.com/client1/contacts/Builder/Bob/Builder Inc";
        try {
            URL url = UrlResource.getUrl(sUrl);
            assertEquals(
                    "http://api.knowprocess.com/client1/contacts/Builder/Bob/Builder%20Inc",
                    url.toExternalForm());
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void testRelativeUrlEncode() {
        String sUrl = "/client1/contacts/Builder/Bob/Builder Inc";
        try {
            UrlResource.getUrl(sUrl);
            fail("Cannot fetch relative URLs");
        } catch (MalformedURLException e) {
            ; // expected
        } catch (URISyntaxException e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void testGetUnauthenticated() {
        UrlResource resource = new UrlResource();
        try (InputStream is = resource.getResource(IMAGE_URL)) {
            writeResource(is, imageDestFile);
        } catch (UnknownHostException e) {
            Assume.assumeNoException(
                    String.format(
                            "Test failed to fetch resource from '%1$s'. Assume because we are running test whilst offline",
                            IMAGE_URL), e);
        } catch (NullPointerException e) {
            if (e.getCause() != null
                    && e.getCause() instanceof ConnectException) {
                e.getCause().printStackTrace();
                Assume.assumeNoException(
                        String.format(
                                "Test failed to connect to the URL '%1$s'. Assume a temporary network problem",
                                IMAGE_URL), e);
            } else {
                e.printStackTrace();
                fail();
            }
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    private void writeResource(InputStream is, String destFile)
            throws FileNotFoundException, IOException {
        assertNotNull(is);
        byte[] b = new byte[2048];
        int length;
        OutputStream os = new FileOutputStream(destFile);
        while ((length = is.read(b)) != -1) {
            os.write(b, 0, length);
        }
        try {
            os.close();
        } catch (Exception e) {
            ;
        }
    }

    @Test
    public void testGetJwtAuthenticated() {
        usr = System.getProperty("usr");
        pwd = System.getProperty("pwd");
        jwtLoginUrl = System.getProperty("jwtLoginUrl");

        UrlResource resource = new UrlResource(new JwtUserPrincipal(usr,pwd,jwtLoginUrl));
        try (InputStream is = resource.getResource(JWT_PROTECTED_URL)) {
            writeResource(is, destFile);
        } catch (UnknownHostException e) {
            Assume.assumeNoException(
                    String.format(
                            "Test failed to fetch resource from '%1$s'. Assume because we are running test whilst offline",
                            JWT_PROTECTED_URL), e);
        } catch (Exception e) {
            if (usr == null || pwd == null || jwtLoginUrl == null) {
                Assume.assumeTrue("You must supply -Dusr=xxx -Dpwd=yyy -DjwtLoginUrl=zzz to call this test", true);
            } else {
                e.printStackTrace();
                fail();
            }
        }
    }
}
