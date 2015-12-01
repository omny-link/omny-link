package link.omny.acctmgmt.web;

import link.omny.acctmgmt.model.TenantConfig;
import link.omny.acctmgmt.repositories.TenantConfigRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/configuration")
public class TenantConfigController {

    protected static final Logger LOGGER = LoggerFactory
            .getLogger(TenantConfigController.class);

    @Autowired
    protected TenantConfigRepository tenantRepo;

    @RequestMapping(value = "/", method = RequestMethod.GET, headers = "Accept=application/json")
    public @ResponseBody Iterable<TenantConfig> showAllJson() {
        LOGGER.info(String.format("showAllJson"));

        return tenantRepo.findAll();
    }
}
