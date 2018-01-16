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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import org.activiti.bdd.test.activiti.ExtendedRule;
import org.activiti.engine.impl.persistence.entity.DeploymentEntity;
import org.activiti.engine.repository.Deployment;
import org.junit.AfterClass;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.webapp.WebAppContext;

import com.knowprocess.resource.internal.gdrive.GDriveConfigurationException;

public class FetcherTest {

    private static Server server;

    @Rule
    public ExtendedRule activitiRule = new ExtendedRule("test-activiti.cfg.xml");

    private Fetcher svc;
    private String resourceUrl = "http://localhost:8080/images/test.png";
    private String repoUri = "https://docs.google.com/tux-collage.jpg";

    @BeforeClass
    public static void startServer() {
        server = new Server(8080);
        server.setStopAtShutdown(true);
        WebAppContext webAppContext = new WebAppContext();
        webAppContext.setContextPath("/");
        webAppContext.setResourceBase("src/test/resources/static");
        webAppContext.setClassLoader(FetcherTest.class
                .getClassLoader());
        server.addHandler(webAppContext);
        try {
            server.start();
        } catch (Exception e) {
            Assume.assumeTrue(
                    "Unable to start test resource server, assume due to port clash",
                    false);
        }
    }

    @AfterClass
    public static void stopServer() throws Exception {
        server.stop();
    }

    @Before
    public void setUp() throws Exception {
        svc = new Fetcher();
    }

    @Test
    public void testUploadImageToRootFolder() {
        try {
            svc.fetchToRepo(resourceUrl, repoUri);
        } catch (GDriveConfigurationException e) {
            System.err
                    .println("Skipping test as GDrive not configured, did you supply a secret?");
        } catch (IOException e) {
            e.printStackTrace();
            fail();
        }
    }

    /*
     * Does not work (uploads to root folder)
     * 
     * @Test public void testUploadImageToSubFolder() { try {
     * svc.fetchToRepo(resourceUrl2, repoUri2); } catch (IOException e) {
     * e.printStackTrace(); fail(); } }
     */

    @Test
    public void testClasspathResourceToString() {
        try {
            String bpmn = svc
                    .fetchToString("classpath:///process/com/knowprocess/deployment/DeploymentProcess.bpmn");
            System.out.println("BPMN: " + bpmn);
            assertNotNull(bpmn);
        } catch (IOException e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void testDeploymentResourceToString() {
        try {
            Deployment deployment = activitiRule.getRepositoryService()
                    .createDeployment().name("TestMailshotDeployment")
                    .addClasspathResource("process/TestMailshot.bpmn")
                    .addClasspathResource("templates/Mailshot.txt").deploy();
            assertNotNull(deployment);
            System.out.println("deployment id: " + deployment.getId());

            String resource = new String(((DeploymentEntity) deployment)
                    .getResource("templates/Mailshot.txt").getBytes());
            System.out.println("resource: " + resource);
            assertNotNull(resource);
            assertTrue(resource.length() > 0);

            svc.setRepositoryService(activitiRule.getRepositoryService());
            String txt = svc
                    .fetchToString("activiti://TestMailshot/templates/Mailshot.txt");
            System.out.println("TEXT: " + txt);
            assertNotNull(txt);
            assertTrue(txt.contains("Kind regards,"));
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void testFetchResourcePart() {
        String resource = "https://omny.link/user-help/contacts";
        try {
            Map<String, String> headers = new HashMap<String, String>();
            String result = svc.fetchToString(resource, headers,
                    ".main_omny_tools");
            System.out.println("Result found: \n" + result);
            assertNotNull(result);
            assertTrue(result.contains("omny_tools"));
            assertTrue(!result.contains("<head>"));
        } catch (IOException e) {
            e.printStackTrace();
            fail("Unable to fetch resource");
        } catch (NullPointerException e) {
            if (e.getCause() != null
                    && e.getCause() instanceof ConnectException) {
                e.getCause().printStackTrace();
                Assume.assumeNoException(
                        String.format(
                                "Test failed to connect to the URL '%1$s'. Assume a temporary network problem",
                                resource), e);
            } else {
                e.printStackTrace();
                fail();
            }
        }
    }

    @Test
    public void testUrlResourceToBlob() {
        OutputStream out = null;
        try {
            byte[] bytes = svc.fetchToByteArray(resourceUrl);
            assertNotNull(bytes);

            File file = new File(new File("target"),
                    "testUrlResourceToBlob.png");
            out = new FileOutputStream(file);
            out.write(bytes);
            System.out.println("Wrote to: " + file);
            assertTrue(file.exists());
        } catch (UnknownHostException e) {
            Assume.assumeNoException(
                    String.format(
                            "Test failed to fetch resource from '%1$s'. Assume because we are running test whilst offline",
                            e.getMessage()), e);
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        } finally {
            try {
                out.close();
            } catch (Exception e) {
            }
        }
    }
}
