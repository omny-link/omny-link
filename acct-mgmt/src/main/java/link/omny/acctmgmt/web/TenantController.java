/*******************************************************************************
 *Copyright 2018 Tim Stephenson and contributors
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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.activiti.engine.IdentityService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.identity.User;
import org.activiti.engine.repository.Deployment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import org.springframework.web.util.UriComponentsBuilder;

import com.knowprocess.bpm.api.ReportableException;

import link.omny.acctmgmt.model.BotConfig;
import link.omny.acctmgmt.model.Tenant;
import link.omny.acctmgmt.model.TenantConfig;
import link.omny.acctmgmt.model.TenantExtension;
import link.omny.acctmgmt.model.TenantProcess;
import link.omny.acctmgmt.model.ThemeConfig;
import link.omny.acctmgmt.repositories.TenantRepository;
import link.omny.catalog.repositories.OrderRepository;
import link.omny.catalog.repositories.StockCategoryRepository;
import link.omny.catalog.repositories.StockItemRepository;
import link.omny.custmgmt.repositories.AccountRepository;
import link.omny.custmgmt.repositories.ContactRepository;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Controller
@RequestMapping(value = "/admin/tenants")
public class TenantController {

    protected static final Logger LOGGER = LoggerFactory
            .getLogger(TenantController.class);

    @Value("${kp.application.root-user:tim@knowprocess.com}")
    protected String rootUser;

    @Autowired
    protected AccountRepository accountRepo;

    @Autowired
    protected ContactRepository contactRepo;

    @Autowired
    protected OrderRepository orderRepo;

    @Autowired
    protected StockItemRepository stockItemRepo;

    @Autowired
    protected StockCategoryRepository stockCategoryRepo;

    @Autowired
    protected TenantRepository tenantRepo;

    @Autowired
    private ProcessEngine processEngine;

    @Autowired
    private TenantConfigController tenantConfigController;

    /**
     * Create a new tenant.
     */
    @ResponseStatus(value = HttpStatus.CREATED)
    @RequestMapping(value = "/", method = RequestMethod.POST)
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public @ResponseBody ResponseEntity<?> create(
            @RequestBody Tenant tenant) {
        tenantRepo.save(tenant);

        tenant.setConfig(tenantConfigController.showTenant(tenant.getId()));

        for (TenantExtension process : tenant.getConfig().getProcesses()) {
            Deployment deployment = processEngine.getRepositoryService()
                    .createDeployment().addClasspathResource(process.getUrl())
                    .tenantId(tenant.getId()).deploy();
            LOGGER.info(String.format(
                    "Deployed process from %1$s, deployment id: %2$s",
                    process.getUrl(), deployment.getId()));
        }

        UriComponentsBuilder builder = MvcUriComponentsBuilder
                .fromController(getClass());
        HashMap<String, String> vars = new HashMap<String, String>();
        vars.put("tenantId", tenant.getId());
        vars.put("id", tenant.getId().toString());

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(builder.path("/{id}").buildAndExpand(vars).toUri());

        return new ResponseEntity(headers, HttpStatus.CREATED);
    }


    /**
     * Create a new tenant bot user.
     */
    @ResponseStatus(value = HttpStatus.CREATED)
    @RequestMapping(value = "/{id}/bot", method = RequestMethod.POST)
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

        String url = String.format("%1$s://%2$s:%3$s",
                request.getScheme(), request.getServerName(),
                request.getServerPort());
        idSvc.setUserInfo(botUser.getId(), BotConfig.KEY_CUST_MGMT_URL, url);
        idSvc.setUserInfo(botUser.getId(), BotConfig.KEY_CUST_MGMT_SECRET,
                botUser.getPassword());
        idSvc.setUserInfo(botUser.getId(), BotConfig.KEY_CC_ACCOUNT, rootUser);
        idSvc.setUserInfo(botUser.getId(), BotConfig.KEY_JWT_AUTH_URL, url + "/auth/login");
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
     * @return Summary statistics for all tenants.
     */
    @RequestMapping(value = "/", method = RequestMethod.GET, headers = "Accept=application/json")
    public @ResponseBody List<TenantSummary> showAllTenants() {
        LOGGER.info(String.format("showAllTenants"));

        List<TenantSummary> result = new ArrayList<TenantSummary>();
        Iterable<Tenant> list = tenantRepo.findAll();
        for (Tenant tenant : list) {
            TenantSummary summary = showOne(tenant.getId());
            summary.setConfigUrl(tenant.getRemoteUrl());
            result.add(summary);
        }
        return result;
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET, headers = "Accept=application/json")
    public @ResponseBody TenantSummary showOne(@PathVariable("id") String tenantId) {
        TenantSummary summary = new TenantSummary();
        summary.setTenantId(tenantId);

        try {
            TenantConfig tenantConfig =
                    tenantConfigController.showTenant(tenantId);
            summary.setName(tenantConfig.getName());
            summary.setTheme(tenantConfig.getTheme());
        } catch (Exception e) {
            LOGGER.error(String.format(
                    "Unable to read tenant config for '%1$s'",
                    tenantId));
        }

        UriComponentsBuilder builder = MvcUriComponentsBuilder
                .fromController(TenantConfigController.class);

        Link detail = new Link(builder.path("/{tenantId}")
                .buildAndExpand(tenantId).toUriString());
        summary.add(detail);

        summary.setAccounts(accountRepo.countForTenant(tenantId));
        summary.setContacts(contactRepo.countForTenant(tenantId));
        summary.setContactAlerts(contactRepo.countAlertsForTenant(tenantId));
        summary.setDefinitions(processEngine.getRepositoryService()
                .createProcessDefinitionQuery()
                .processDefinitionTenantId(tenantId).count());
        summary.setActiveInstances(processEngine.getRuntimeService()
                .createProcessInstanceQuery()
                .processInstanceTenantId(tenantId).count());
        summary.setHistoricInstances(processEngine.getHistoryService()
                .createHistoricProcessInstanceQuery()
                .processInstanceTenantId(tenantId).count());
        summary.setJobs(processEngine.getManagementService()
                .createJobQuery().jobTenantId(tenantId).count());
        summary.setOrders(orderRepo.countForTenant(tenantId));
        summary.setStockItems(stockItemRepo.countForTenant(tenantId));
        summary.setStockCategories(stockCategoryRepo.countForTenant(tenantId));
        summary.setTasks(processEngine.getTaskService().createTaskQuery()
                .taskTenantId(tenantId).count());
        summary.setUsers(processEngine.getIdentityService()
                .createUserQuery().memberOfGroup(tenantId).count());
        return summary;
    }

    /**
     * Delete an existing tenant.
     */
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    @RequestMapping(value = "/admin/tenants/{id}", method = RequestMethod.DELETE, consumes = { "application/json" })
    public @ResponseBody void delete(@PathVariable("id") String id) {
        Tenant tenant = tenantRepo.findOne(id);

        tenantRepo.delete(tenant);
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class TenantSummary extends ResourceSupport {
        private String tenantId;
        private String name;
        private Long accounts;
        private Long contacts;
        private Long contactAlerts;
        private Long definitions;
        private Long activeInstances;
        private Long historicInstances;
        private Long jobs;
        private Long orders;
        private Long stockItems;
        private Long stockCategories;
        private Long tasks;
        private Long users;
        private Date lastLogin;
        private Date lastActivity;
        private String configUrl;
        private ThemeConfig theme;
        private List<TenantProcess> processes;
    }
}
