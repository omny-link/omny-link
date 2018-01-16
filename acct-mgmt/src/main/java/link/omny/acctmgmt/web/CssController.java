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
package link.omny.acctmgmt.web;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.knowprocess.resource.spi.Fetcher;

import link.omny.acctmgmt.model.TenantConfig;

@Controller
public class CssController {

    protected static final Logger LOGGER = LoggerFactory
            .getLogger(CssController.class);

    private static final String STATIC_BASE = "/static";

    @Autowired
    protected TenantConfigController tenantConfigController;

    private String defaultCss;

    /**
     * @param tenantId
     *            The id of an existing tenant.
     * @return The CSS for that tenant.
     */
    @RequestMapping(value = "/css/{tenantId}-{semVer}.css", method = RequestMethod.GET, headers = "Accept=text/css")
    public @ResponseBody String showTenant(
            @PathVariable("tenantId") String tenantId,
            @RequestParam(required = false, defaultValue = "1.0.0") String semVer) {
        LOGGER.info(String.format("showTenant"));

        String resourceUrl = String.format("%1$s/css/%2$s-%3$s.css",
                STATIC_BASE, tenantId, semVer);
        TenantConfig tenantConfig = tenantConfigController.showTenant(tenantId);
        if (tenantConfig.getTheme() != null
                && tenantConfig.getTheme().getCssUrl() != null
                && tenantConfig.getTheme().getCssUrl().startsWith("http")) { // remote css
            String theme = null;
            Fetcher get = new Fetcher();
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
                && tenantConfig.getTheme().getCssUrl() != null) { // embedded css
            return TenantConfig.readResource(String.format("%1$s%2$s",
                    STATIC_BASE, tenantConfig.getTheme().getCssUrl()));
        } else if (TenantConfig.resourceExists(resourceUrl)) {
            return TenantConfig.readResource(resourceUrl);
        } else if (tenantConfig.getTheme() != null
                && tenantConfig.getTheme().getAccentColor() != null
                && tenantConfig.getTheme().getBodyColor() != null
                && tenantConfig.getTheme().getHeadingColor() != null
                && tenantConfig.getTheme().getIconColor() != null
                && tenantConfig.getTheme().getSubHeadingColor() != null) { // simple palette
            return getDefaultCss(tenantConfig);
        } else {
            // Defaults exist for everything but log warning
            LOGGER.warn("Please specify either CSS url or theme color properties in {} tenant configuration", tenantId);
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
