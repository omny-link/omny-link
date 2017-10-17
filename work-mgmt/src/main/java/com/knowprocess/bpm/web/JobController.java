package com.knowprocess.bpm.web;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.activiti.engine.ManagementService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.impl.cmd.AbstractCustomSqlExecution;
import org.activiti.engine.impl.cmd.CustomSqlExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.knowprocess.bpm.impl.JobRetryMapper;
import com.knowprocess.bpm.model.Job;

@RequestMapping("/{tenantId}/jobs")
@Controller
public class JobController {

    protected static final Logger LOGGER = LoggerFactory
            .getLogger(JobController.class);

    @Autowired
    protected ProcessEngine processEngine;

    private DateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

    @RequestMapping(value = "", method = RequestMethod.GET, headers = "Accept=application/json")
    public @ResponseBody List<Job> showAllJson(
            @PathVariable("tenantId") String tenantId) {
        LOGGER.info(String.format("showAllJson for %1$s", tenantId));

        List<org.activiti.engine.runtime.Job> list = processEngine
                .getManagementService().createJobQuery().jobTenantId(tenantId)
                .orderByJobDuedate().asc()
                .list();
        LOGGER.info("Total jobs: " + list.size());
        return wrap(list);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.POST, headers = "Accept=application/json")
    public @ResponseBody void retry(
            @PathVariable("tenantId") String tenantId,
            @PathVariable("id") String jobId) {
        LOGGER.info(String.format("retry %1$s for %2$s", jobId, tenantId));

        ManagementService managementService = processEngine.getManagementService();
        String dueDate = iso8601Format.format(new Date());
        CustomSqlExecution<JobRetryMapper, List<Map<String, Object>>> customSqlExecution
                = new AbstractCustomSqlExecution<JobRetryMapper, List<Map<String, Object>>>(
                JobRetryMapper.class) {
            public List<Map<String, Object>> execute(
                    JobRetryMapper customMapper) {
                customMapper.retryJob(jobId, dueDate);
                return Collections.emptyList();
            }
        };

        managementService.executeCustomSql(customSqlExecution);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE, headers = "Accept=application/json")
    public @ResponseBody void delete(
            @PathVariable("tenantId") String tenantId,
            @PathVariable("id") String jobId) {
        LOGGER.info(String.format("delete %1$s for %2$s", jobId, tenantId));

        processEngine.getManagementService().deleteJob(jobId);
    }

    public static List<Job> wrap(
            final List<org.activiti.engine.runtime.Job> list) {
        ArrayList<Job> list2 = new ArrayList<Job>();
        for (org.activiti.engine.runtime.Job job : list) {
            list2.add(new Job(job));
        }
        return list2;
    }

}
