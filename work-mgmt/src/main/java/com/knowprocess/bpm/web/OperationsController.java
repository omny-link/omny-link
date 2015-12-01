package com.knowprocess.bpm.web;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import link.omny.acctmgmt.model.TenantConfig;
import link.omny.acctmgmt.repositories.TenantConfigRepository;

import org.activiti.engine.HistoryService;
import org.activiti.engine.IdentityService;
import org.activiti.engine.ManagementService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricProcessInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.knowprocess.bpm.model.OperationsSummary;
import com.knowprocess.bpm.model.ProcessInstance;

@Controller
@RequestMapping("/operations")
public class OperationsController {

    protected static final Logger LOGGER = LoggerFactory
            .getLogger(OperationsController.class);

    @Autowired
    protected HistoryService histSvc;

    @Autowired
    protected ManagementService mgmtSvc;

    @Autowired
    protected RuntimeService runtimeSvc;

    @Autowired
    protected RepositoryService repoSvc;

    @Autowired
    protected TaskService taskSvc;

    @Autowired
    protected IdentityService idSvc;

    @Autowired
    protected TenantConfigRepository tenantRepo;

    private DateFormat isoFormatter = new SimpleDateFormat("yyyy-MM-dd");

    @RequestMapping(value = "/", method = RequestMethod.GET, headers = "Accept=application/json")
    public @ResponseBody List<OperationsSummary> showSummaryJson() {
        ArrayList<OperationsSummary> results = new ArrayList<OperationsSummary>();

        for (TenantConfig tenant : tenantRepo.findAll()) {
            OperationsSummary summary = new OperationsSummary();
            summary.setTenantId(tenant.getTenantId());
            summary.setJobs(mgmtSvc.createJobQuery()
                    .jobTenantId(tenant.getTenantId())
                    .count());
            summary.setActiveInstances(runtimeSvc.createProcessInstanceQuery()
                    .processInstanceTenantId(tenant.getTenantId()).count());
            summary.setCompletedInstances(histSvc
                    .createHistoricProcessInstanceQuery()
                    .processInstanceTenantId(tenant.getTenantId()).finished()
                    .count());
            summary.setTotalDefinitions(repoSvc.createProcessDefinitionQuery()
                    .processDefinitionTenantId(tenant.getTenantId()).count());
            summary.setActiveDefinitions(repoSvc.createProcessDefinitionQuery()
                    .latestVersion()
                    .processDefinitionTenantId(tenant.getTenantId())
                    .count());
            summary.setTasks(taskSvc.createTaskQuery()
                    .taskTenantId(tenant.getTenantId())
                    .active().count());
            // TODO find users for tenant
            // summary.setUsers(idSvc.createUserQuery().count());
            results.add(summary);
        }
        return results;
    }

    @RequestMapping(value = "/{tenantId}/archive", method = RequestMethod.GET, headers = "Accept=application/json")
    public @ResponseBody List<ProcessInstance> archiveInstances(
            @PathVariable("tenantId") String tenantId, 
            @RequestParam (value="before", required= false) String before) {
        Date beforeDate = null;
        if (before == null) {
            GregorianCalendar oneMonthAgo = new GregorianCalendar();
            oneMonthAgo.add(Calendar.MONTH, -1);
            LOGGER.debug(String.format(
                    "Archiving messages or %1$s older than %2$s", tenantId,
                    oneMonthAgo.getTime().toString()));
            beforeDate = oneMonthAgo.getTime();
        } else {
            try {
                beforeDate = isoFormatter.parse(before);
            } catch (ParseException e) {
                throw new IllegalArgumentException(
                        String.format(
                                "Parameter 'before' must be an ISO 8601 date, not '%1$s'",
                                before));
            }
        }

        List<HistoricProcessInstance> archivedInstances = histSvc
                .createHistoricProcessInstanceQuery()
                .processInstanceTenantId(tenantId)
.finishedBefore(beforeDate)
                .list();
        for (HistoricProcessInstance hpi : archivedInstances) {
            histSvc.deleteHistoricProcessInstance(hpi.getId());
        }

        return ProcessInstance.wrap(archivedInstances);
    }
}
