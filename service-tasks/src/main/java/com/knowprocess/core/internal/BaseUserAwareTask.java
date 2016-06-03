package com.knowprocess.core.internal;

import javax.annotation.Nonnull;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.delegate.JavaDelegate;
import org.activiti.engine.identity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.knowprocess.exceptions.UserInfoNotFoundException;

/**
 * Base class capable of looking up user settings such as for external accounts.
 */
public abstract class BaseUserAwareTask implements JavaDelegate {

    public static final Logger LOGGER = LoggerFactory
            .getLogger(BaseUserAwareTask.class);

    protected Expression resourceUsername;
    protected Expression resourcePassword;

    protected String lookup(DelegateExecution execution, @Nonnull String usr, @Nonnull Expression expr) {
        try {
            String s = (String) expr.getValue(execution);
            if (s.startsWith("userInfo('")) {
                String key = s.substring("userInfo(".length(), s.indexOf(')'));
                if (key.startsWith("'") || key.startsWith("\"")) {
                    key = key.substring(1, key.length() - 1);
                }
                String val = execution.getEngineServices().getIdentityService()
                        .getUserInfo(usr, key);
                if (val == null) {
                    throw new ActivitiObjectNotFoundException(
                            String.format(
                                    "No user setting '%1$s' found for '%2$s'",
                                    key, usr));
                }
                s = val + s.substring(s.indexOf(')') + 1);
            }
            return s;
        } catch (ActivitiException e) {
            String msg = String.format(
                    "Problem whilst looking up '%1$s' for '%2$s', check with your administrator.",
                    expr, usr);
            LOGGER.error(msg + " " + e.getClass().getName() + ":"
                    + e.getMessage());
            throw new UserInfoNotFoundException(msg);
        }
    }

    protected String getPassword(DelegateExecution execution, String usr) {
        return resourcePassword == null ? null : lookup(execution, usr,
                resourcePassword);
    }

    protected String getUsername(DelegateExecution execution) {
        if (resourceUsername == null) {
            return null;
        } else if (resourceUsername.getExpressionText().equals(
                "userInfo('tenant-bot')")) {
            return lookupBotName(execution);
        } else {
            return (String) resourceUsername.getValue(execution);
        }
    }

    private String lookupBotName(DelegateExecution execution) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(String.format("  Looking for bot of tenant %1$s",
                    execution.getTenantId()));
        }
        User botUser = execution.getEngineServices().getIdentityService()
                .createUserQuery().userFirstName(execution.getTenantId())
                .userLastName("Bot").singleResult();
        if (botUser == null) {
            String msg = String.format("No bot user for tenant '%1$s'",
                    execution.getTenantId());
            LOGGER.error(msg);
            throw new ActivitiObjectNotFoundException(msg, User.class);
        }
        return botUser.getId();
    }

}
