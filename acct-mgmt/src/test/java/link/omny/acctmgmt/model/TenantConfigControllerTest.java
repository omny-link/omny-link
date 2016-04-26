package link.omny.acctmgmt.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;

import link.omny.acctmgmt.Application;
import link.omny.acctmgmt.web.TenantConfigController;

import org.activiti.engine.ProcessEngine;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
public class TenantConfigControllerTest {

    private static final String TOOLBAR_ENTRY_WINDOW = "/window.html";

    private static final String PARTIAL_EXTENSION = "/partials/dummy-extension.html";

    private static final String PROCESS_DUMMY_1 = "processes/link/omny/acctmgmt/Dummy1.bpmn";

    private static final String CONTROL_EXTENSION = "/data/test/owners.json";

    private static final String TENANT_ID = "test";

    @Autowired
    public TenantConfigController svc;

    @Autowired
    private ProcessEngine processEngine;

    public void tearDown() {
        try {
            svc.delete(TENANT_ID);
            TenantConfig TenantConfig6 = svc.showTenant(TENANT_ID);
            assertNull(TenantConfig6);
        } catch (Exception e) {
            ; // ok if exception prevented test completing successfully
        }
    }

    @Test
    public void testLifecycle() {
        // CREATE
        TenantConfig tenantConfig = new TenantConfig(TENANT_ID);
        tenantConfig.setFeature("account", true);
        tenantConfig.addPartial(new TenantPartial("omnyContactExtension",
                PARTIAL_EXTENSION));
        tenantConfig.addProcess(new TenantProcess("Dummy Process",
                PROCESS_DUMMY_1, "Dummy1"));
        tenantConfig.addToolbarEntry(new TenantToolbarEntry("90 Moving Window",
                TOOLBAR_ENTRY_WINDOW, "omny-icon-dashboard",
                "View your 90 day moving window report here"));
        tenantConfig.addTypeaheadControl(new TenantTypeaheadControl("owners",
                CONTROL_EXTENSION));
        svc.create(TENANT_ID, tenantConfig);

        // RETRIEVE
        TenantConfig tenantConfig2 = svc.showTenant(TENANT_ID);
        assertNotNull(tenantConfig2);
        assertTrue(tenantConfig2.getFeatures().isAccount());

        List<TenantProcess> processes = tenantConfig2.getProcesses();
        assertEquals(1, processes.size());
        assertEquals(PROCESS_DUMMY_1, processes.get(0).getUrl());
        assertTrue(processes.get(0).isValid());

        List<TenantToolbarEntry> toolbarEntries = tenantConfig2.getToolbar();
        assertEquals(1, toolbarEntries.size());
        assertEquals(TOOLBAR_ENTRY_WINDOW, toolbarEntries.get(0).getUrl());
        assertTrue(toolbarEntries.get(0).isValid());

        List<TenantPartial> partials = tenantConfig2.getPartials();
        assertEquals(1, partials.size());
        assertEquals(PARTIAL_EXTENSION, partials.get(0).getUrl());
        assertTrue(partials.get(0).isValid());

        List<TenantTypeaheadControl> controls = tenantConfig2
                .getTypeaheadControls();
        assertEquals(1, controls.size());
        assertEquals(CONTROL_EXTENSION, controls.get(0).getUrl());
        assertTrue(controls.get(0).isValid());

        // UPDATE
        svc.update(tenantConfig2.getId(), tenantConfig2);

        // DELETE
        svc.delete(TENANT_ID);
        try {
            TenantConfig tenantConfig3 = svc.showTenant(TENANT_ID);
            System.out.println("  " + tenantConfig3);
            fail("Should not have found tenant config here");
        } catch (IllegalArgumentException e) {
            ; // good
        }
    }

}
