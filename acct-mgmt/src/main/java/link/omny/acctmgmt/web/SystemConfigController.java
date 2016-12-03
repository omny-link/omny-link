package link.omny.acctmgmt.web;

import link.omny.acctmgmt.model.SystemConfig;
import link.omny.acctmgmt.repositories.TenantRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/configuration")
public class SystemConfigController {

    protected static final Logger LOGGER = LoggerFactory
            .getLogger(SystemConfigController.class);

    @Autowired
    private SystemConfig systemConfig;

    @Autowired
    protected TenantRepository tenantRepo;

    @RequestMapping(method = RequestMethod.GET)
    public final @ResponseBody SystemConfig getSystemConfig() {
        LOGGER.info("getSystemConfig");

        return systemConfig;
    }
}
