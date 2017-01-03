package link.omny.analytics.factories;

import java.io.InputStream;

import link.omny.analytics.api.AnalyticsNotFoundException;
import link.omny.analytics.api.ReportFactory;

public class ClasspathReportFactory implements ReportFactory {
    public InputStream getReportStream(String reportName) {
        InputStream is = getClass().getResourceAsStream(reportName);
        if (is == null) {
            is = getClass().getResourceAsStream(
                    "/reports/" + reportName + ".jrxml");
        }
        if (is == null) {
            throw new AnalyticsNotFoundException(String.format(
                    "Unable to get report definition for '%1$s'", reportName));
        }

        return is;
    }
}
