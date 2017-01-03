package link.omny.analytics.api;

import net.sf.jasperreports.engine.JRDataSource;

public interface ReportDataSource extends JRDataSource {

    void init(String tenantId);
}
