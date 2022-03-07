package link.omny.server;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties("crm.bpm")
@Component
public class CrmBpmProperies {

    private boolean tomcatAjpEnabled;

    private int ajpPort = 8080;

    private boolean ajpSecure;

    private String ajpScheme = "http2";

    private String corsMethods = "DELETE,GET,HEAD,POST,PUT";

    private String corsOrigins = "http://localhost:8000";

    private String corsHeaders = "*";

    private String corsExposedHeaders = "*";

    private boolean corsAllowCredentials;
    
    private String processGateway;

    public boolean isTomcatAjpEnabled() {
        return tomcatAjpEnabled;
    }

    public void setTomcatAjpEnabled(boolean tomcatAjpEnabled) {
        this.tomcatAjpEnabled = tomcatAjpEnabled;
    }

    public int getAjpPort() {
        return ajpPort;
    }

    public void setAjpPort(int ajpPort) {
        this.ajpPort = ajpPort;
    }

    public boolean isAjpSecure() {
        return ajpSecure;
    }

    public void setAjpSecure(boolean ajpSecure) {
        this.ajpSecure = ajpSecure;
    }

    public String getAjpScheme() {
        return ajpScheme;
    }

    public void setAjpScheme(String ajpScheme) {
        this.ajpScheme = ajpScheme;
    }

    public String getCorsMethods() {
        return corsMethods;
    }

    public void setCorsMethods(String corsMethods) {
        this.corsMethods = corsMethods;
    }

    public String getCorsOrigins() {
        return corsOrigins;
    }

    public void setCorsOrigins(String corsOrigins) {
        this.corsOrigins = corsOrigins;
    }

    public String getCorsHeaders() {
        return corsHeaders;
    }

    public void setCorsHeaders(String corsHeaders) {
        this.corsHeaders = corsHeaders;
    }

    public String getCorsExposedHeaders() {
        return corsExposedHeaders;
    }

    public void setCorsExposedHeaders(String corsExposedHeaders) {
        this.corsExposedHeaders = corsExposedHeaders;
    }

    public boolean isCorsAllowCredentials() {
        return corsAllowCredentials;
    }

    public void setCorsAllowCredentials(boolean corsAllowCredentials) {
        this.corsAllowCredentials = corsAllowCredentials;
    }
    
    public String getProcessGateway() {
        return processGateway;
    }

    public void setProcessGateway(String processGateway) {
        this.processGateway = processGateway;
    }
    
    
}
