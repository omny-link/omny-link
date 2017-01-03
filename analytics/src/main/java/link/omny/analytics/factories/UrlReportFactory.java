package link.omny.analytics.factories;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import link.omny.analytics.api.AnalyticsNotFoundException;
import link.omny.analytics.api.ReportFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UrlReportFactory implements ReportFactory {
    protected static final Logger LOGGER = LoggerFactory
            .getLogger(UrlReportFactory.class);

    public InputStream getReportStream(String reportUrl) {
        try {
            return new URL(reportUrl).openStream();
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
            throw new AnalyticsNotFoundException(e.getMessage());
        }
    }
}
