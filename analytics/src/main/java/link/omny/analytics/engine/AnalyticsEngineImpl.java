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
package link.omny.analytics.engine;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import link.omny.analytics.api.AnalyticsEngine;
import link.omny.analytics.api.AnalyticsException;
import link.omny.analytics.api.ReportDataSource;
import link.omny.analytics.api.ReportFactory;
import link.omny.analytics.factories.ClasspathReportFactory;
import link.omny.analytics.factories.UrlReportFactory;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class AnalyticsEngineImpl implements AnalyticsEngine {

    protected static final Logger LOGGER = LoggerFactory
            .getLogger(AnalyticsEngineImpl.class);

    protected Map<String, String> compiledReports = new HashMap<String, String>();

    protected ReportDataSource dataSource;
    
    protected ClasspathReportFactory classpathReportFactory;

    protected UrlReportFactory urlReportFactory;

    /**
     * @see link.omny.analytics.engine.AnalyticsEngine#runAsHtml(java.lang.String,
     *      java.lang.String)
     */
    @Override
    public URI runAsHtml(String tenantId, String reportName) {
        return runAsHtml(tenantId, reportName, getClasspathReportFactory());
    }

    private ReportFactory getClasspathReportFactory() {
        if (classpathReportFactory == null) {
            classpathReportFactory = new ClasspathReportFactory();
        }
        return classpathReportFactory;
    }

    /**
     * @see link.omny.analytics.engine.AnalyticsEngine#runAsHtml(java.net.URL)
     */
    @Override
    public URI runAsHtml(String tenantId, URL report) {
        return runAsHtml(tenantId, report.toExternalForm(),
                getUrlReportFactory());
    }

    private ReportFactory getUrlReportFactory() {
        if (urlReportFactory == null) {
            urlReportFactory = new UrlReportFactory();
        }
        return urlReportFactory;
    }

    protected URI runAsHtml(String tenantId, String reportName,
            ReportFactory reportFactory) {
        try {
            LOGGER.debug(String.format("Running report %1$s", reportName));
            File tempFile = compileIfNecessary(reportName, reportFactory);

            long start = System.currentTimeMillis();
            JasperPrint jasperPrint = JasperFillManager.fillReport(
                    tempFile.getAbsolutePath(), new HashMap<String, Object>(),
                    getDataSource(tenantId));
            File outFile = new File(reportName.substring(reportName
                    .lastIndexOf('/') + 1) + ".html");
            JasperExportManager.exportReportToHtmlFile(jasperPrint,
                    outFile.toString());
            LOGGER.debug(String.format("... rendering took %1$d ms",
                    System.currentTimeMillis() - start));

            return outFile.toURI();
        } catch (JRException e) {
            LOGGER.error(e.getMessage(), e);
            throw new AnalyticsException(e.getMessage(), e.getCause());
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
            throw new AnalyticsException(e.getMessage(), e.getCause());
        }
    }

    private File compileIfNecessary(String reportName,
            ReportFactory reportFactory)
            throws IOException, FileNotFoundException, JRException {
        File tempFile;
        long start = System.currentTimeMillis();
        if (compiledReports.containsKey(reportName)) {
            tempFile = new File(compiledReports.get(reportName));
            tempFile.deleteOnExit();
        } else {
            InputStream is = null;
            OutputStream os = null;
            try {
                tempFile = File.createTempFile("analytics", ".jasper");
                is = reportFactory.getReportStream(reportName);
                os = new FileOutputStream(tempFile);
                JasperCompileManager.compileReportToStream(is, os);
                LOGGER.debug(String.format(
                        "... compiling took %1$d ms (%2$s)",
                        System.currentTimeMillis() - start,
                        tempFile.getAbsolutePath()));
                compiledReports.put(reportName, tempFile.getAbsolutePath());
            } finally {
                try {
                    is.close();
                } catch (Exception e) {
                    ;
                }
                try {
                    os.close();
                } catch (Exception e) {
                    ;
                }
            }
        }
        return tempFile;
    }
    
    private JRDataSource getDataSource(String tenantId) {
        dataSource.init(tenantId);
        return dataSource;
    }

    @Override
    public void setDataSource(ReportDataSource dataSource) {
        this.dataSource = dataSource;
    }
}
