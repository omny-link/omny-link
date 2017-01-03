package link.omny.analytics.engine;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URI;
import java.net.URL;

import link.omny.analytics.TestDataSource;
import link.omny.analytics.api.AnalyticsEngine;

import org.junit.After;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.webapp.WebAppContext;

public class AnalyticsEngineTest {

    private static final int TEST_PORT = 8080;

    private static Server server;

    private static AnalyticsEngine engine;

    private File report;

    @BeforeClass
    public static void setUp() {
        server = new Server(8080);
        server.setStopAtShutdown(true);
        WebAppContext webAppContext = new WebAppContext();
        webAppContext.setContextPath("/");
        webAppContext.setResourceBase("src/test/web-resources/");
        webAppContext
                .setClassLoader(AnalyticsEngineTest.class.getClassLoader());
        server.addHandler(webAppContext);
        try {
            server.start();
        } catch (Exception e) {
            Assume.assumeTrue(
                    "Unable to start test resource server, assume due to port clash",
                    false);
        }

        engine = new AnalyticsEngineImpl();
        engine.setDataSource(new TestDataSource());
    }

    @After
    public void tearDown() {
        if (report != null) {
            report.delete();
        }
    }

    @Test
    public void testEngineHtmlFromClasspath() {
        URI uri = engine.runAsHtml("omny", "/reports/report1.jrxml");
        System.out.println("Report generated to: " + uri);
        assertNotNull(uri);

        report = new File(uri.getPath());
        assertTrue(report.exists());

        uri = engine.runAsHtml("omny", "/reports/report1.jrxml");
        report.delete();
    }

    @Test
    public void testEngineHtmlFromUrl() throws Exception {
        URI uri = engine.runAsHtml("omny",
            new URL(String.format(
                        "http://localhost:%1$d/reports/report2.jrxml",
                    TEST_PORT)));
        System.out.println("Report generated to: " + uri);
        assertNotNull(uri);

        report = new File(uri.getPath());
        assertTrue(report.exists());
        report.delete();

        uri = engine.runAsHtml(
                "omny",
                new URL(String.format(
                        "http://localhost:%1$d/reports/report2.jrxml",
                        TEST_PORT)));

        // having fetched and compiled, should be able to re-run locally...
        // uri = engine.runAsHtml("omny", "/reports/report2.jrxml");
        // System.out.println("Report generated to: " + uri);
        // assertNotNull(uri);
        //
        // report = new File(uri.getPath());
        // assertTrue(report.exists());
    }
}
