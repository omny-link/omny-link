/*******************************************************************************
 *Copyright 2015-2018 Tim Stephenson and contributors
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
package link.omny.analytics.web;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.List;

import link.omny.acctmgmt.model.TenantConfig;
import link.omny.acctmgmt.model.TenantReport;
import link.omny.acctmgmt.web.TenantConfigController;
import link.omny.analytics.api.AnalyticsEngine;
import link.omny.analytics.api.AnalyticsException;
import link.omny.analytics.datasources.ContactDataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/{tenantId}/analytics")
public class AnalyticsController {

    protected static final Logger LOGGER = LoggerFactory
            .getLogger(AnalyticsController.class);

    @Autowired
    private TenantConfigController tenantConfigController;

    @Autowired
    @Qualifier("contactDataSource")
    protected ContactDataSource contactDataSource;

    @Autowired
    protected AnalyticsEngine engine;

    /**
     * @param name
     *            The report name.
     * @param tenantId
     *            The id of an existing tenant.
     * @return Results data set.
     */
    @RequestMapping(value = "/contacts/{reportName}", method = RequestMethod.GET)
    public @ResponseBody ResponseEntity<InputStreamResource> showReport(
            @PathVariable("tenantId") String tenantId,
            @PathVariable("reportName") String reportName) {
        LOGGER.info(String.format("showReport"));

        engine.setDataSource(contactDataSource);
        
        TenantReport tenantReport = getTenantReport(tenantId, reportName);
        URI uri;
        if (tenantReport.getUrl() == null) {
            uri = engine.runAsHtml(tenantId, reportName);
        } else {
            try {
                uri = engine
                        .runAsHtml(tenantId, new URL(tenantReport.getUrl()));
            } catch (MalformedURLException e) {
                IllegalStateException e2 = new IllegalStateException(
                        e.getMessage(), e);
                LOGGER.error(e2.getMessage());
                throw e2;
            }
        }
        File report = new File(uri.getPath());
        LOGGER.info(String.format("Generated report to: %1$s", report));

        InputStreamResource inputStreamResource;
        try {
            inputStreamResource = new InputStreamResource(new FileInputStream(
                    report));
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.add("Location", uri.toString());
            // httpHeaders.setContentLength(sb.length());
            return new ResponseEntity<InputStreamResource>(inputStreamResource,
                    httpHeaders, HttpStatus.OK);
        } catch (FileNotFoundException e) {
            LOGGER.error(e.getMessage(), e);
            throw new AnalyticsException(e.getMessage());
        }

    }

    private TenantReport getTenantReport(String tenantId, String reportName) {
        TenantConfig tenantConfig = tenantConfigController.showTenant(tenantId);
        List<TenantReport> reports = tenantConfig.getReports();
        for (TenantReport report : reports) {
            if (report.getRef().equals(reportName)) {
                return report;
            }
        }
        throw new AnalyticsException(String.format(
                "Report %1$s not configured for tenant %2$s", reportName,
                tenantId));
    }

}
