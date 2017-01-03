package link.omny.analytics.api;

import java.io.InputStream;

public interface ReportFactory {
    InputStream getReportStream(String reportUrl);
}