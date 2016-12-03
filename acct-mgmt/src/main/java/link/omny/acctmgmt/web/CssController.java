package link.omny.acctmgmt.web;

import java.io.IOException;

import link.omny.acctmgmt.model.SystemConfig;
import link.omny.acctmgmt.model.TenantConfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.knowprocess.resource.spi.RestGet;

@Controller
public class CssController {

    protected static final Logger LOGGER = LoggerFactory
            .getLogger(CssController.class);

    private static final String STATIC_BASE = "/static";

    @Autowired
    private SystemConfig systemConfig;

    @Autowired
    protected TenantConfigController tenantConfigController;

    private String defaultCss;

    /**
     * @param tenantId
     *            The id of an existing tenant.
     * @return The CSS for that tenant.
     */
    @RequestMapping(value = "/css/{tenantId}-{semVar}.css", method = RequestMethod.GET, headers = "Accept=text/css")
    public @ResponseBody String showTenant(
            @PathVariable("tenantId") String tenantId,
            @RequestParam(required = false, defaultValue = "1.0.0") String semVer) {
        LOGGER.info(String.format("showTenant"));

        String resourceUrl = String.format("%1$s/css/%2$s-%3$s.css",
                STATIC_BASE, tenantId, semVer);
        TenantConfig tenantConfig = tenantConfigController.showTenant(tenantId);
        if (tenantConfig.getTheme() != null
                && tenantConfig.getTheme().getCssUrl() != null
                && tenantConfig.getTheme().getCssUrl().startsWith("http")) {
            String theme = null;
            RestGet get = new RestGet();
            try {
                theme = get.fetchToString(tenantConfig.getTheme()
                        .getCssUrl());
            } catch (IOException e) {
                LOGGER.error(String
                        .format("Unable to read tenant CSS for '%1$s' from '%2$s', fallback on defaults",
                                tenantId, tenantConfig.getTheme().getCssUrl()));
                theme = "";
            }
            return String.format(getDefaultCss(tenantConfig)) + theme;
        } else if (tenantConfig.getTheme() != null
                && tenantConfig.getTheme().getCssUrl() != null) {
            return TenantConfig.readResource(String.format("%1$s%2$s",
                    STATIC_BASE, tenantConfig.getTheme().getCssUrl()));
        } else if (TenantConfig.resourceExists(resourceUrl)) {
            return TenantConfig.readResource(resourceUrl);
        } else {
            // Defaults exist for everything but log warning
            String msg = "Please specify either CSS url or theme color properties in tenant file";
            LOGGER.warn(msg);
            return getDefaultCss(tenantConfig);
        }
    }

    private String getDefaultCss(TenantConfig tenantConfig) {
        return String.format(getDefaultCssTemplate(),
                tenantConfig.getTheme().getHeadingColor(),
                tenantConfig.getTheme().getSubHeadingColor(),
                tenantConfig.getTheme().getBodyColor(),
                tenantConfig.getTheme().getAccentColor(),
                tenantConfig.getTheme().getIconColor());
    }

    private String getDefaultCssTemplate() {
        // if (defaultCss == null) {
            defaultCss = TenantConfig.readResource(String.format(
                    "%1$s/css/%2$s.css", STATIC_BASE, "default"));
        // }
        return defaultCss;
    }

}
