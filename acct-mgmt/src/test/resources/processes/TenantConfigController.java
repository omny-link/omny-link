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
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import link.omny.acctmgmt.model.BotConfig;
import link.omny.acctmgmt.model.SystemConfig;
import link.omny.acctmgmt.model.Tenant;
import link.omny.acctmgmt.model.TenantAction;
import link.omny.acctmgmt.model.TenantConfig;
import link.omny.acctmgmt.model.TenantExtension;
import link.omny.acctmgmt.model.TenantPartial;
import link.omny.acctmgmt.model.TenantProcess;
import link.omny.acctmgmt.model.TenantTemplate;
import link.omny.acctmgmt.model.TenantToolbarEntry;
import link.omny.acctmgmt.model.TenantTypeaheadControl;
import link.omny.acctmgmt.repositories.TenantRepository;
import link.omny.custmgmt.model.Memo;
import link.omny.custmgmt.repositories.ContactRepository;
import link.omny.custmgmt.repositories.MemoRepository;
import lombok.Data;
import lombok.EqualsAndHashCode;

import org.activiti.engine.IdentityService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.identity.User;
import org.activiti.engine.repository.ProcessDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.knowprocess.bpm.api.ReportableException;
import com.knowprocess.bpm.web.Md5HashUtils;
import com.knowprocess.resource.spi.RestGet;

@Controller
// @RequestMapping("/admin/tenants")
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
     * Create a new tenant bot user.
     */
    @ResponseStatus(value = HttpStatus.CREATED)
    @RequestMapping(value = "/tenants/{id}/bot", method = RequestMethod.POST)
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public @ResponseBody ResponseEntity<?> createBot(
           @PathVariable("id") String tenantId, @RequestParam(value="force", required=false) boolean force, HttpServletRequest request) {

        IdentityService idSvc = processEngine.getIdentityService();

        List<User> users = idSvc.createUserQuery().userFirstName(tenantId)
                .userLastName("Bot").list();
        if (users.size() >= 1 && !force) {
            throw new ReportableException(
                    String.format(
                            "A bot user already exists for tenant %1$s. If you're sure you want to recreate it supply the parameter 'force'",
                            tenantId));
        } else if (force) {
            for (User user : users) {
                idSvc.deleteUser(user.getId());
            }
        }

        User botUser = idSvc.newUser(UUID.randomUUID().toString());
        botUser.setFirstName(tenantId);
        botUser.setLastName("Bot");
        botUser.setPassword(UUID.randomUUID().toString());
        idSvc.saveUser(botUser);

        String url = request.getRequestURL().toString();
        idSvc.setUserInfo(botUser.getId(), "cust-mgmt-url",
                url.substring(0, url.indexOf("/tenants")));
        idSvc.setUserInfo(botUser.getId(), "cust-mgmt-secret",
                botUser.getPassword());
        idSvc.setUserInfo(botUser.getId(), "tenant",tenantId);

        idSvc.createMembership(botUser.getId(), "bot");

        UriComponentsBuilder builder = MvcUriComponentsBuilder
                .fromController(getClass());
        HashMap<String, String> vars = new HashMap<String, String>();
        vars.put("tenantId", tenantId);

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(builder.path("/{tenantId}/bot")
                .buildAndExpand(vars).toUri());

        return new ResponseEntity(headers, HttpStatus.CREATED);
    }

    /**
     * @param id
     *            The id of an existing tenant.
     * @return The complete configuration for that tenant.
     */
    @RequestMapping(value = "/admin/tenants", method = RequestMethod.GET, headers = "Accept=application/json")
    public @ResponseBody List<TenantConfigSummary> showAllTenants() {
        LOGGER.info(String.format("showAllTenants"));

        // TODO not currently persisting tenants
        // Iterable<TenantConfig> list = tenantRepo.findAll();
        // for (TenantConfig tenant : list) {
        List<TenantConfig> list = new ArrayList<TenantConfig>();
        for (String tenantId : systemConfig.getTenants().split(",")) {
            TenantConfig tenant = new TenantConfig(tenantId);

            tenant.setContacts(contactRepo.countForTenant(tenantId));
            tenant.setContactAlerts(contactRepo.countAlertsForTenant(tenantId));

            tenant.setDefinitions(processEngine.getRepositoryService()
                    .createProcessDefinitionQuery()
                    .processDefinitionTenantId(tenant.getId()).count());
            tenant.setActiveInstances(processEngine.getRuntimeService()
                    .createProcessInstanceQuery()
                    .processInstanceTenantId(tenant.getId()).count());
            tenant.setHistoricInstances(processEngine.getHistoryService()
                    .createHistoricProcessInstanceQuery()
                    .processInstanceTenantId(tenant.getId()).count());
            tenant.setJobs(processEngine.getManagementService()
                    .createJobQuery().jobId(tenant.getId()).count());
            tenant.setTasks(processEngine.getTaskService().createTaskQuery()
                    .taskTenantId(tenant.getId()).count());
            tenant.setUsers(processEngine.getIdentityService()
                    .createUserQuery().memberOfGroup(tenant.getId()).count());

            list.add(tenant);
        }
        return wrap(list);
    }

    /**
     * @param id
     *            The id of an existing tenant.
     * @return The complete configuration for that tenant.
     */
    @RequestMapping(value = "/tenants/{id}", method = RequestMethod.GET, headers = "Accept=application/json")
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
            } catch (IOException e) {
                throw new IllegalArgumentException(String.format(
                        "Unable to read legacy tenant config from '%1$s'",
                        resource));
            }
        } else {
            RestGet get = new RestGet();
            try {
                String sConfig = get.fetchToString(tenant.getRemoteUrl());
                tenant.setConfig(objectMapper.readValue(sConfig,
                        new TypeReference<TenantConfig>() {
                        }));
                tenant.getConfig().setId(id);
            } catch (IOException e) {
                String msg = String.format(
                        "Unable to read tenant config for '%1$s' from '%2$s'",
                        id, tenant.getRemoteUrl());
                LOGGER.warn(msg);
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
                        process.setDescription(pd.getDescription());
                    }
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
                Memo memo = memoRepo.findByName(template.getName(), id);
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

    private List<TenantConfigSummary> wrap(Iterable<TenantConfig> list) {
        List<TenantConfigSummary> resources = new ArrayList<TenantConfigSummary>();
        for (TenantConfig contact : list) {
            resources.add(wrap(contact));
        }
        return resources;
    }

    private TenantConfigSummary wrap(TenantConfig tenant) {
        TenantConfigSummary resource = new TenantConfigSummary();
        BeanUtils.copyProperties(tenant, resource);
        resource.setShortId(tenant.getId());
        Link detail = linkTo(TenantRepository.class, tenant.getId())
                .withSelfRel();
        resource.add(detail);
        resource.setSelfRef(detail.getHref());
        return resource;
    }

    private Link linkTo(
            @SuppressWarnings("rawtypes") Class<? extends CrudRepository> clazz,
            String id) {
        return new Link(clazz.getAnnotation(RepositoryRestResource.class)
                .path() + "/" + id);
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class TenantConfigSummary extends ResourceSupport {
        private String shortId;
        private String selfRef;
        private String name;
        private Long contacts;
        private Long contactActions;
        private Long contactAlerts;
        private Long definitions;
        private Long activeInstances;
        private Long historicInstances;
        private Long jobs;
        private Long tasks;
        private Long users;
        private Date lastLogin;
        private Date lastActivity;
        private List<TenantProcess> processes;
    }
}
