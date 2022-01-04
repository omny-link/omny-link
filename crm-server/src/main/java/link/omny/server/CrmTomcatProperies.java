package link.omny.server;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties("crm.tomcat")
@Component
public class CrmTomcatProperies {

    private boolean ajpEnabled;

    private int ajpPort = 8080;

    private boolean ajpSecure;

    private String ajpScheme = "http2";

    public boolean isAjpEnabled() {
        return ajpEnabled;
    }

    public void setAjpEnabled(boolean tomcatAjpEnabled) {
        this.ajpEnabled = tomcatAjpEnabled;
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

}
