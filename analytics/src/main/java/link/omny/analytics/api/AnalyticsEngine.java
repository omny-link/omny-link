package link.omny.analytics.api;

import java.net.URI;
import java.net.URL;

public interface AnalyticsEngine {

    URI runAsHtml(String tenantId, String reportName);

    URI runAsHtml(String tenantId, URL reportName);

    void setDataSource(ReportDataSource dataSource);

}