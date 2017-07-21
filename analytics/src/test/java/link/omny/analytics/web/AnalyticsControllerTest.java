package link.omny.analytics.web;

import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import link.omny.acctmgmt.model.Tenant;
import link.omny.acctmgmt.model.TenantConfig;
import link.omny.acctmgmt.model.TenantReport;
import link.omny.acctmgmt.repositories.TenantRepository;
import link.omny.analytics.TestApplication;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = TestApplication.class)
public class AnalyticsControllerTest {

    private static final String TENANT_ID = "test";

    @Autowired
    private TenantRepository tenantRepo;

    @Autowired
    private AnalyticsController svc;

    @Before
    public void setUpClass() {
        Tenant tenant = new Tenant();
        tenant.setId(TENANT_ID);
        TenantConfig config = new TenantConfig();
        TenantReport tenantReport = new TenantReport();
        tenantReport.setName("First report");
        tenantReport.setRef("/reports/report1.jrxml");
        config.getReports().add(tenantReport);
        tenant.setConfig(config);

        tenantRepo.save(tenant);
    }
    
    
    @Test
    public void testSpringRepo() {
        ResponseEntity<InputStreamResource> responseEntity = svc.showReport(
                TENANT_ID, "/reports/report1.jrxml");
        assertNotNull(responseEntity);
    }

}
