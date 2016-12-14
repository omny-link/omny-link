package link.omny.acctmgmt.web;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import link.omny.acctmgmt.model.BotConfig;
import link.omny.acctmgmt.model.SystemConfig;
import link.omny.acctmgmt.model.Tenant;
import link.omny.acctmgmt.model.TenantConfig;
import link.omny.acctmgmt.model.TenantExtension;
import link.omny.acctmgmt.model.TenantProcess;
import link.omny.acctmgmt.repositories.TenantRepository;
import link.omny.custmgmt.repositories.ContactRepository;
import lombok.Data;
import lombok.EqualsAndHashCode;

import org.activiti.engine.IdentityService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.identity.User;
import org.activiti.engine.repository.Deployment;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import org.springframework.web.util.UriComponentsBuilder;

import com.knowprocess.bpm.api.ReportableException;

@Controller
@RequestMapping("/admin/tenants")
public class TenantController {

    protected static final Logger LOGGER = LoggerFactory
            .getLogger(TenantController.class);

    @Autowired
    private SystemConfig systemConfig;

    @Autowired
    protected ContactRepository contactRepo;

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
    @RequestMapping(value = "/tenants/{id}", method = RequestMethod.POST)
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public @ResponseBody ResponseEntity<?> create(
            @PathVariable("id") String tenantId,
            @RequestBody Tenant tenant) {
        tenant.setId(tenantId);

        tenantRepo.save(tenant);

        tenant.setConfig(tenantConfigController.showTenant(tenantId));

        for (TenantExtension process : tenant.getConfig().getProcesses()) {
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

        String url = request.getRequestURL().toString();
        idSvc.setUserInfo(botUser.getId(), BotConfig.KEY_CUST_MGMT_URL,
                url.substring(0, url.indexOf("/tenants")));
        idSvc.setUserInfo(botUser.getId(), BotConfig.KEY_CUST_MGMT_SECRET,
                botUser.getPassword());
        idSvc.setUserInfo(botUser.getId(), BotConfig.KEY_CC_ACCOUNT, "");
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
    public @ResponseBody List<TenantSummary> showAllTenants() {
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
     * Delete an existing contact.
     */
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE, consumes = { "application/json" })
    public @ResponseBody void delete(@PathVariable("id") String id) {
        Tenant tenant = tenantRepo.findOne(id);

        tenantRepo.delete(tenant);
    }

    private List<TenantSummary> wrap(Iterable<TenantConfig> list) {
        List<TenantSummary> resources = new ArrayList<TenantSummary>();
        for (TenantConfig contact : list) {
            resources.add(wrap(contact));
        }
        return resources;
    }

    private TenantSummary wrap(TenantConfig tenant) {
        TenantSummary resource = new TenantSummary();
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
    public static class TenantSummary extends ResourceSupport {
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
