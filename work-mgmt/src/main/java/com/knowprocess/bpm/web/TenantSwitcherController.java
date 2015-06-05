package com.knowprocess.bpm.web;

import org.activiti.engine.IdentityService;
import org.activiti.engine.ProcessEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.knowprocess.bpm.model.UserInfoKeys;

@Controller
@RequestMapping("/admin/tenant")
public class TenantSwitcherController {

    protected static final Logger LOGGER = LoggerFactory
            .getLogger(TenantSwitcherController.class);

    @Autowired(required = true)
    ProcessEngine processEngine;

    @RequestMapping(value = "/{username}/{tenantId}", method = RequestMethod.PUT)
    @ResponseBody
    public void switchAccountToTenant(
            @PathVariable("username") String username,
            @PathVariable("tenantId") String tenantId) {
        LOGGER.info(String.format("Changing %1$s to tenant %2$s", username,
                tenantId));

        IdentityService idSvc = processEngine.getIdentityService();
        idSvc.setUserInfo(username, UserInfoKeys.TENANT.toString(), tenantId);
    }
}
