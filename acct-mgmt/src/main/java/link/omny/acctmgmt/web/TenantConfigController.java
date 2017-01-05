package link.omny.acctmgmt.web;

import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

import link.omny.acctmgmt.model.BotConfig;
import link.omny.acctmgmt.model.SystemConfig;
import link.omny.acctmgmt.model.Tenant;
import link.omny.acctmgmt.model.TenantAction;
import link.omny.acctmgmt.model.TenantConfig;
import link.omny.acctmgmt.model.TenantExtension;
import link.omny.acctmgmt.model.TenantPartial;
import link.omny.acctmgmt.model.TenantTemplate;
import link.omny.acctmgmt.model.TenantToolbarEntry;
import link.omny.acctmgmt.model.TenantTypeaheadControl;
import link.omny.acctmgmt.repositories.TenantRepository;
import link.omny.custmgmt.model.Memo;
import link.omny.custmgmt.repositories.ContactRepository;
import link.omny.custmgmt.repositories.MemoRepository;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.identity.User;
import org.activiti.engine.repository.ProcessDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.knowprocess.bpm.web.Md5HashUtils;
import com.knowprocess.resource.spi.RestGet;

@Controller
@RequestMapping(value = "/tenants")
public class TenantConfigController {

    protected static final Logger LOGGER = LoggerFactory
            .getLogger(TenantConfigController.class);

    private static final String STATIC_BASE = "/static";

    @Autowired
    private SystemConfig systemConfig;

    @Autowired
    protected ContactRepository contactRepo;

    @Autowired
    protected TenantRepository tenantRepo;

    @Autowired
    protected MemoRepository memoRepo;

    @Autowired
    private ProcessEngine processEngine;

    @Autowired
    private ObjectMapper objectMapper;


    /**
     * @param id
     *            The id of an existing tenant.
     * @return The complete configuration for that tenant.
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.GET, headers = "Accept=application/json")
    public @ResponseBody TenantConfig showTenant(@PathVariable("id") String id) {
        LOGGER.info(String.format("showTenant"));

        Tenant tenant = tenantRepo.findOne(id);

        if (tenant == null) {
            throw new IllegalArgumentException(String.format(
                    "Unknown tenant '%1$s'", id));
        } else if (tenant.getRemoteUrl() == null) {
            // embedded configs (legacy)
            String resource = STATIC_BASE + "/tenants/" + id + ".json";
            try {
                tenant.setConfig(objectMapper.readValue(
                        TenantConfig.readResource(resource),
                        new TypeReference<TenantConfig>() {
                        }));
                tenant.getConfig().setId(id);
            } catch (Exception e) {
                throw new IllegalArgumentException(String.format(
                        "Unable to read legacy tenant config from '%1$s'",
                        resource));
            }
        } else {
            RestGet get = new RestGet();
            try {
                LOGGER.info(String.format(
                        "About to read config for %1$s from %2$s", id,
                        tenant.getRemoteUrl()));
                String sConfig = get.fetchToString(tenant.getRemoteUrl());
                tenant.setConfig(objectMapper.readValue(sConfig,
                        new TypeReference<TenantConfig>() {
                        }));
                tenant.getConfig().setId(id);
            } catch (IOException e) {
                String msg = String.format(
                        "Unable to read tenant config for '%1$s' from '%2$s'",
                        id, tenant.getRemoteUrl());
                LOGGER.warn(msg, e);
                throw new IllegalArgumentException(msg);
            }
        }

        validateTenantConfig(id, tenant.getConfig());

        return tenant.getConfig();
    }

    protected void validateTenantConfig(String id, TenantConfig tenantConfig) {
        User botUser = processEngine.getIdentityService().createUserQuery()
                .userFirstName(id).userLastName("Bot").singleResult();
        if (botUser==null) {
            LOGGER.warn(String.format("No bot user for tenant '%1$s'", id));
            tenantConfig.setBot(new BotConfig(id));
        } else {
            tenantConfig.setBot(new BotConfig(botUser.getId(), botUser
                .getPassword(), botUser.getEmail()));
            tenantConfig.getBot().setCustMgmtUrl(
                    processEngine.getIdentityService().getUserInfo(botUser.getId(),
                            BotConfig.KEY_CUST_MGMT_URL));
            tenantConfig.getBot().setCustMgmtSecret(
                    processEngine.getIdentityService().getUserInfo(botUser.getId(),
                            BotConfig.KEY_CUST_MGMT_SECRET));
            tenantConfig.getBot().setCcAccount(
                    processEngine.getIdentityService().getUserInfo(botUser.getId(),
                            BotConfig.KEY_CC_ACCOUNT));
            tenantConfig.getBot().setValid(
                    tenantConfig.getBot().getCustMgmtUrl() != null
                    && tenantConfig.getBot().getCustMgmtSecret() != null
                    && tenantConfig.getBot().getCcAccount() != null);
        }
        
        for (TenantExtension process : tenantConfig.getProcesses()) {
            ProcessDefinition pd = processEngine.getRepositoryService()
                    .createProcessDefinitionQuery()
                    .processDefinitionKey(process.getRef())
                    .processDefinitionTenantId(id).latestVersion()
                    .singleResult();
            if (pd != null) {
                InputStream is = null;
                try {
                    is = processEngine.getRepositoryService().getProcessModel(
                            pd.getId());
                    @SuppressWarnings("resource")
                    String latestDeployedBpmn = new Scanner(is).useDelimiter(
                            "\\A").next();

                    if (process.getUrl() != null
                            && Md5HashUtils.isIdentical(
                            com.knowprocess.bpm.model.ProcessDefinition
                                    .readFromClasspath("/" + process.getUrl()),
                            latestDeployedBpmn)) {
                        process.setValid(true);
                    } else {
                        process.setStatus("warning");
                        process.setValid(false);
                    }
                    process.setDescription(pd.getDescription());
                } catch (Throwable t) {
                    LOGGER.error(String
                            .format("Problem loading process from %1$s, check configuration",
                                    process.getUrl()));
                } finally {
                     try {
                         is.close();
                     } catch (Exception e) {
                         ;
                     }
                }
            } else {
                // Missing will be flagged as an error
            }
        }

        for (TenantAction entry : tenantConfig.getContactActions()) {
            try {
                switch (entry.getUrl()) {
                case "/true":
                    entry.setValid(true);
                    break;
                default:
                    entry.setValid(TenantConfig.resourceExists(STATIC_BASE
                            + entry.getUrl()));
                }
            } catch (NullPointerException e) {
                // ok for now, may refactor to ensure actions always specify url
                entry.setValid(true);
            }
        }

        for (TenantToolbarEntry entry : tenantConfig.getToolbar()) {
            entry.setValid(TenantConfig.resourceExists(STATIC_BASE
                    + entry.getUrl()));
        }

        for (TenantPartial partial : tenantConfig.getPartials()) {
            partial.setValid(TenantConfig.resourceExists(STATIC_BASE
                    + partial.getUrl()));
        }

        for (TenantTemplate template : tenantConfig.getTemplates()) {
            try {
                Memo memo = memoRepo.findByName(template.getRef(), id);
                if (memo != null) {
                    template.setValid(true);
                    template.setDescription(String.format("Subject: %1$s",
                            memo.getTitle()));
                }
            } catch (Exception e) {
                LOGGER.error(String.format("Missing template named %1$s",
                        template.getName()));
            }
        }

        for (TenantTypeaheadControl control : tenantConfig
                .getTypeaheadControls()) {
            if (control.getUrl() == null) {
                LOGGER.debug(String.format("Have embedded typeahead for %1$s",
                        control.getName()));
            } else if (control.getUrl() != null
                    && control.getUrl().indexOf(id) == -1) {
                control.setStatus("warning");
                control.setValid(false);
                if (control.getDescription() == null) {
                    control.setDescription("NOTE: relying on Omny Link defaults");
                }
            } else {
                control.setValid(TenantConfig.resourceExists(STATIC_BASE
                        + control.getUrl()));
            }

        }
    }
}
