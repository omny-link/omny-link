package link.omny.acctmgmt.model;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.junit.BeforeClass;
import org.junit.Test;

public class CohortPerformanceTest {

    private static final String TENANT_ID = "clinic";

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    private DateFormat isoParser = new SimpleDateFormat("yyyy-MM-dd");

    @Test
    public void testClinicCoverage() throws ParseException {
        CohortPerformance perf = new CohortPerformance();
        perf.setOccurred(isoParser.parse("2016-05-30"));
        perf.setSubjectName("Ralph Wiggum");
        perf.addCustomField(new CustomCohortField("doctorsName", "Dr Hibbert"));
        Metric opExplainedProperly = new Metric(TENANT_ID,
                "operationExplainedProperly", true);
        opExplainedProperly.setDescription("Very clear");
        perf.addMetric(opExplainedProperly);
        perf.addMetric(new Metric(TENANT_ID, "bookingPerf", 5l));
        perf.setDescription("Very happy with clinical team and my arm function is much improved after 6 moinths.");
    }

    @Test
    public void testBalancedScorecardCoverage() throws ParseException {
        CohortPerformance perf = new CohortPerformance();
        perf.setOccurred(isoParser.parse("2016-05-30"));
        perf.setSubjectName("ACME Inc.");
        perf.addCustomField(new CustomCohortField("cohortName", "FY 2015-16"));
        Metric iso14000Cert = new Metric(TENANT_ID, "iso14000Certified", true);
        iso14000Cert.setDescription("First obtained 2014");
        perf.addMetric(iso14000Cert);
        perf.addMetric(new Metric(TENANT_ID, "waterPerf", 5l));
        perf.setDescription("Takes leadership position from Board down");
    }

}
