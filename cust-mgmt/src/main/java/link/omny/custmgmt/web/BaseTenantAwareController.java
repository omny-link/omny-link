package link.omny.custmgmt.web;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import org.springframework.web.util.UriComponentsBuilder;

public abstract class BaseTenantAwareController {
    static final Logger LOGGER = LoggerFactory
            .getLogger(BaseTenantAwareController.class);
    
    protected URI getGlobalUri(Object id) {
        try {
            UriComponentsBuilder builder = MvcUriComponentsBuilder
                    .fromController(getClass());
            String uri = builder.build().toUriString()
                    .replace("{tenantId}/", "");
            return new URI(uri
                    + "/" + id);
        } catch (URISyntaxException e) {
            LOGGER.error(e.getMessage(), e);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    protected URI getTenantBasedUri(String tenantId, Object id) {
        UriComponentsBuilder builder = MvcUriComponentsBuilder
                .fromController(getClass());
        HashMap<String, String> vars = new HashMap<String, String>();
        vars.put("tenantId", tenantId);
        vars.put("id", id.toString());
        return builder.path("/{id}").buildAndExpand(vars).toUri();
    }

}
