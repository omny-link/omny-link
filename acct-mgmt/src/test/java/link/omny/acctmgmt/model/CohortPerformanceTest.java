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
