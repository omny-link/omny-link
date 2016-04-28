package link.omny.acctmgmt.web;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import link.omny.acctmgmt.model.BotConfig;
import link.omny.acctmgmt.model.TenantAction;
import link.omny.acctmgmt.model.TenantConfig;
import link.omny.acctmgmt.model.TenantExtension;
import link.omny.acctmgmt.model.TenantPartial;
import link.omny.acctmgmt.model.TenantToolbarEntry;
import link.omny.acctmgmt.model.TenantTypeaheadControl;
import link.omny.acctmgmt.repositories.TenantConfigRepository;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.identity.User;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Controller
@RequestMapping("/tenants")
public class TenantConfigController {

    protected static final Logger LOGGER = LoggerFactory
            .getLogger(TenantConfigController.class);

    private static final String STATIC_BASE = "/static";

    // @Autowired
    // private SystemConfig systemConfig;

    @Autowired
    protected TenantConfigRepository tenantRepo;

    @Autowired
    private ProcessEngine processEngine;

    @Autowired
    private ObjectMapper objectMapper;

    // @RequestMapping(method = RequestMethod.GET, )
    // public final @ResponseBody SystemConfig getSystemConfig() {
    // LOGGER.info("getSystemConfig");
    //
    // return systemConfig;
    // }

    /**
     * Create a new tenant.
     */
    @ResponseStatus(value = HttpStatus.CREATED)
    @RequestMapping(value = "/{id}", method = RequestMethod.POST)
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public @ResponseBody ResponseEntity<?> create(
            @PathVariable("id") String tenantId,
            @RequestBody TenantConfig tenantConfig) {
        tenantConfig.setId(tenantId);
        tenantRepo.save(tenantConfig);

        for (TenantExtension process : tenantConfig.getProcesses()) {
            Deployment deployment = processEngine.getRepositoryService()
                    .createDeployment().addClasspathResource(process.getUrl())
                    .tenantId(tenantId).deploy();
            LOGGER.info(String.format(
                    "Deployed process from %1$s, deployment id: %2$s",
                    process.getUrl(), deployment.getId()));
        }

        UriComponentsBuilder builder = MvcUriComponentsBuilder
                .fromController(getClass());
        HashMap<String, String> vars = new HashMap<String, String>();
        vars.put("tenantId", tenantId);
        vars.put("id", tenantConfig.getId().toString());

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(builder.path("/{id}").buildAndExpand(vars).toUri());

        return new ResponseEntity(headers, HttpStatus.CREATED);
    }

    /**
     * @param id
     *            The id of an existing tenant.
     * @return The complete configuration for that tenant.
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.GET, headers = "Accept=application/json")
    public @ResponseBody TenantConfig showTenant(@PathVariable("id") String id) {
        LOGGER.info(String.format("showTenant"));

        TenantConfig tenantConfig = null;
        try {
            // Despite JavaDoc, not found does NOT throw exception
            tenantConfig = tenantRepo.findOne(id);
        } catch (Throwable e) {
            // will use JSON file as fallback
        }
        String resource = STATIC_BASE + "/tenants/" + id + ".json";
        boolean legacyConfig = resourceExists(resource);
        if (tenantConfig == null && !legacyConfig) {
            throw new IllegalArgumentException(String.format(
                    "No tenant found with id '%1$s'", id));
        } else if (tenantConfig == null) {
            try {
                tenantConfig = objectMapper.readValue(
                        TenantConfig.readResource(resource),
                        new TypeReference<TenantConfig>() {
                        });
                tenantConfig.setId(id);
                // TODO enable save once ready to migrate to the database config
                // tenantRepo.save(tenantConfig);
            } catch (IOException e) {
                throw new IllegalArgumentException(String.format(
                        "Unable to read legacy tenant config from '%1$s'",
                        resource));
            }
        }

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
                            "cust-mgmt-url"));
            tenantConfig.getBot().setCustMgmtSecret(
                    processEngine.getIdentityService().getUserInfo(botUser.getId(),
                            "cust-mgmt-secret"));
            tenantConfig.getBot().setValid(
                    tenantConfig.getBot().getCustMgmtUrl() != null
                    && tenantConfig.getBot().getCustMgmtSecret() != null);
        }
        
        for (TenantExtension process : tenantConfig.getProcesses()) {
            ProcessDefinition pd = processEngine.getRepositoryService()
                    .createProcessDefinitionQuery()
                    .processDefinitionKey(process.getRef())
                    .processDefinitionTenantId(id).latestVersion()
                    .singleResult();
            if (pd != null) {
                process.setValid(true);
                process.setDescription(pd.getDescription());
            }
        }

        for (TenantAction entry : tenantConfig.getContactActions()) {
            try {
                switch (entry.getUrl()) {
                case "/true":
                    entry.setValid(true);
                    break;
                default:
                    entry.setValid(resourceExists(STATIC_BASE + entry.getUrl()));
                }
            } catch (NullPointerException e) {
                // ok for now, may refactor to ensure actions always specify url
                entry.setValid(true);
            }
        }

        for (TenantToolbarEntry entry : tenantConfig.getToolbar()) {
            entry.setValid(resourceExists(STATIC_BASE + entry.getUrl()));
        }

        for (TenantPartial partial : tenantConfig.getPartials()) {
            partial.setValid(resourceExists(STATIC_BASE + partial.getUrl()));
        }

        for (TenantTypeaheadControl control : tenantConfig
                .getTypeaheadControls()) {
            control.setValid(resourceExists(STATIC_BASE + control.getUrl()));
        }

        return tenantConfig;
    }

    private boolean resourceExists(String resourceUrl) {
        InputStream is = null;
        try {
            is = getClass().getResourceAsStream(resourceUrl);
            return is != null;
        } catch (Exception e) {
            LOGGER.warn(String.format("Unable to find resource named %1$s",
                    resourceUrl));
        } finally {
            try {
                is.close();
            } catch (Exception e) {
                ;
            }
        }
        return false;
    }

    /**
     * Update an existing tenant.
     */
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    @RequestMapping(value = "/{id}", method = RequestMethod.PUT, consumes = { "application/json" })
    public @ResponseBody void update(@PathVariable("tenantId") String tenantId,
            @RequestBody TenantConfig updatedTenant) {
        TenantConfig tenantConfig = tenantRepo.findOne(tenantId);

        BeanUtils.copyProperties(updatedTenant, tenantConfig, "id");
        tenantConfig.setId(tenantId);
        tenantRepo.save(tenantConfig);
    }

    /**
     * Delete an existing tenant. NOT exposed as REST API.
     */
    public void delete(String tenantId) {
        tenantRepo.delete(tenantId);
    }
}
