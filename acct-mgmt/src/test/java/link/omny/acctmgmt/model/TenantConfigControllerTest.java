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
package link.omny.acctmgmt.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.List;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.identity.User;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.webapp.WebAppContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import com.fasterxml.jackson.databind.ObjectMapper;

import link.omny.acctmgmt.Application;
import link.omny.acctmgmt.web.TenantConfigController;
import link.omny.acctmgmt.web.TenantController;
import link.omny.acctmgmt.web.TenantController.TenantSummary;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = Application.class)
@WebAppConfiguration
public class TenantConfigControllerTest {

    private static final String TEST_TENANT_ID = "test";

    private static final String REMOTE_TEST_TENANT_ID = "remote";

    private static final String TOOLBAR_ENTRY_WORK = "/work.html";

    private static final String PARTIAL_EXTENSION = "/partials/contact-extension.html";

    private static final String PROCESS_DUMMY_1 = "processes/link/omny/acctmgmt/Dummy1.bpmn";

    private static final String CONTROL_EXTENSION = "/data/" + TEST_TENANT_ID
            + "/owners.json";

    // Use Boot to serve test data as if remote
    private static final String REMOTE_TEST_TENANT_CONFIG_URL = "http://localhost:8080/RemoteTestTenantConfig.json";

    private static Server server;

    @Autowired
    public ObjectMapper objectMapper;

    @Autowired
    public TenantController tenantController;

    @Autowired
    public TenantConfigController svc;

    @Autowired
    private ProcessEngine processEngine;

    @BeforeClass
    public static void startServer() {
        server = new Server(8080);
        server.setStopAtShutdown(true);
        WebAppContext webAppContext = new WebAppContext();
        webAppContext.setContextPath("/");
        webAppContext.setResourceBase("src/test/resources/static");
        webAppContext.setClassLoader(TenantConfigControllerTest.class
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

    @Before
    public void setUp() {
        User botUser = processEngine.getIdentityService().newUser(
                TEST_TENANT_ID);
        botUser.setFirstName(TEST_TENANT_ID);
        botUser.setLastName("Bot");
        processEngine.getIdentityService().saveUser(botUser);

        // processEngine.getIdentityService().setUserInfo(userId, key, value);
    }

    @After
    public void tearDown() {
        processEngine.getIdentityService().deleteUser(TEST_TENANT_ID);
        try {
            tenantController.delete(TEST_TENANT_ID);
            List<TenantSummary> tenant = tenantController.showAllTenants();
            for (TenantSummary tenantSummary : tenant) {
                if (tenantSummary.getId().toString().contains(TEST_TENANT_ID)) {
                    fail(String.format("Unable to delete tenant %1$s",
                            TEST_TENANT_ID));
                }
            }
        } catch (Exception e) {
            ; // ok if exception prevented test completing successfully
        }
    }

    @AfterClass
    public static void stopServer() throws Exception {
        server.stop();
    }

    @Test
    public void testLocalJsonLifecycle() throws IOException {
        // CREATE
        Tenant tenant = new Tenant(TEST_TENANT_ID, null);
        tenantController.create(tenant);

        // RETRIEVE
        TenantConfig tenantConfig2 = svc.showTenant(TEST_TENANT_ID);
        assertNotNull(tenantConfig2);
        assertTrue(!tenantConfig2.getFeatures().isAccount());

        List<TenantProcess> processes = tenantConfig2.getProcesses();
        assertEquals(1, processes.size());
        assertEquals(PROCESS_DUMMY_1, processes.get(0).getUrl());
        assertTrue(processes.get(0).isValid());

        List<TenantToolbarEntry> toolbarEntries = tenantConfig2.getToolbar();
        assertEquals(11, toolbarEntries.size());
        assertEquals(TOOLBAR_ENTRY_WORK, toolbarEntries.get(0).getUrl());
        assertTrue(toolbarEntries.get(0).isValid());

        List<TenantPartial> partials = tenantConfig2.getPartials();
        assertEquals(6, partials.size());
        assertEquals(PARTIAL_EXTENSION, partials.get(0).getUrl());
        assertTrue(partials.get(0).isValid());

        List<TenantTypeaheadControl> controls = tenantConfig2
                .getTypeaheadControls();
        assertEquals(2, controls.size());
        // #421 creates owner list from contacts, so url is null
        assertNull(controls.get(0).getUrl());
        assertTrue(controls.get(0).isValid());
    }

    @Test
    public void testRemoteJsonLifecycle() throws IOException {
        // CREATE
        Tenant tenant = new Tenant(REMOTE_TEST_TENANT_ID,
                REMOTE_TEST_TENANT_CONFIG_URL);
        tenantController.create(tenant);

        // RETRIEVE
        TenantConfig tenantConfig2 = svc.showTenant(REMOTE_TEST_TENANT_ID);
        assertNotNull(tenantConfig2);
        assertTrue(!tenantConfig2.getFeatures().isAccount());

        List<TenantProcess> processes = tenantConfig2.getProcesses();
        assertEquals(1, processes.size());
        assertEquals(PROCESS_DUMMY_1, processes.get(0).getUrl());
        assertTrue(processes.get(0).isValid());

        List<TenantToolbarEntry> toolbarEntries = tenantConfig2.getToolbar();
        assertEquals(11, toolbarEntries.size());
        assertEquals(TOOLBAR_ENTRY_WORK, toolbarEntries.get(0).getUrl());
        assertTrue(toolbarEntries.get(0).isValid());

        List<TenantPartial> partials = tenantConfig2.getPartials();
        assertEquals(6, partials.size());
        assertEquals(PARTIAL_EXTENSION, partials.get(0).getUrl());
        assertTrue(partials.get(0).isValid());

        List<TenantTypeaheadControl> controls = tenantConfig2
                .getTypeaheadControls();
        assertEquals(1, controls.size());
        // #421 creates owner list from contacts, so url is null
        assertNull(controls.get(0).getUrl());
        assertTrue(!controls.get(0).isValid());
    }
}
