package com.knowprocess.bpm.web;

import java.util.HashMap;

import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.IdentityService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.identity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.knowprocess.bpm.model.UserInfoKeys;
import com.knowprocess.usermgmt.PasswordGenerator;
import com.knowprocess.usermgmt.SimplePasswordGenerator;

@Controller
@RequestMapping("/reset-password")
public class ResetPasswordController {

    protected static final String RESET_PWD_DEFINITION_KEY = "ResetPassword";

    protected static final String PATH = "/users";

    protected static final Logger LOGGER = LoggerFactory
            .getLogger(ResetPasswordController.class);

    @Autowired(required = true)
    ProcessEngine processEngine;

    private PasswordGenerator passwordGenerator = new SimplePasswordGenerator();

    /**
     * Initiate a password reset.
     * 
     * @param userId
     * @param existingPassword
     * @param model
     * @return viewName
     */
    @RequestMapping(value = "/{userId}/reset-password", method = RequestMethod.GET)
    public String resetPassword(
            @PathVariable("userId") String userId, Model model) {
        LOGGER.info(String.format("resetPassword for %1$s", userId));

        HashMap<String, Object> vars = new HashMap<String, Object>();
        vars.put("userId", userId);
        String tenantId = processEngine.getIdentityService().getUserInfo(
                userId, UserInfoKeys.TENANT.name().toLowerCase());
        if (tenantId == null) {
            throw new ActivitiObjectNotFoundException(User.class);
        }
        processEngine.getRuntimeService().startProcessInstanceByKeyAndTenantId(
                RESET_PWD_DEFINITION_KEY, vars, tenantId);
        return "login?msg=resetEmail";
    }

    /**
     * Confirm the current password (or reset token) matches and return a view
     * to enable the password to be reset.
     * 
     * @param id
     * @param existingPassword
     * @param model
     * @return viewName
     */
    @RequestMapping(value = "/{id}/reset-password/{existingPassword}", method = RequestMethod.GET)
    public String resetPassword(
            @PathVariable("id") String id,
            @PathVariable(value = "existingPassword") String existingPassword,
            Model model) {
        LOGGER.info(String.format("resetPassword for %1$s", id));

        if (processEngine.getIdentityService().checkPassword(id,
                existingPassword)) {
            model.addAttribute("user", processEngine.getIdentityService()
                    .createUserQuery().userId(id).singleResult());
            return "resetPasswordNow";
        } else {
            return "forward:/login?msg=passwordReset";
        }
    }

    @RequestMapping(value = "/{id}/reset-password/{existingPassword}", method = RequestMethod.POST)
    public String updatePassword(
            @PathVariable("id") String id,
            @PathVariable(value = "existingPassword") String existingPassword,
            @RequestParam(value = "newPassword", defaultValue = "") String newPassword,
            @RequestParam(value = "confirmPassword", defaultValue = "") String confirmPassword) {
        LOGGER.info(String.format("updatePassword for %1$s", id));

        if (!newPassword.equals(confirmPassword)) {
            throw new IllegalArgumentException(
                    "New passwords do not match, password unchanged.");
        }

        IdentityService idSvc = processEngine.getIdentityService();
        User user = idSvc.createUserQuery().userId(id).singleResult();
        if (existingPassword.length() == 0 && newPassword.length() == 0
                && confirmPassword.length() == 0) {
            LOGGER.warn(String.format("Resetting pwd for %1$s", id));
            user.setPassword(passwordGenerator.generate());
            idSvc.saveUser(user);

            return "forward:/login?msg=passwordReset";
        } else if (user.getPassword().equals(existingPassword)) {
            LOGGER.warn(String.format("Updating pwd for %1$s", id));
            user.setPassword(newPassword);
            idSvc.saveUser(user);
            
            return "forward:/login?msg=passwordReset";
        } else {
            throw new IllegalArgumentException(
                    "Existing password does not match our records, password unchanged.");
        }
    }
}
