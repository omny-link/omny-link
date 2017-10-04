package com.knowprocess.bpm.web;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Optional;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.impl.identity.Authentication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.knowprocess.bpm.impl.AuthenticationHelper;
import com.knowprocess.bpm.model.FormProperty;
import com.knowprocess.bpm.model.Task;
import com.rometools.rome.feed.atom.Entry;
import com.rometools.rome.feed.atom.Feed;

@RequestMapping()
@Controller
public class TaskController {

    protected static final Logger LOGGER = LoggerFactory
            .getLogger(TaskController.class);

    @Autowired
    protected ProcessEngine processEngine;

    protected DateFormat isoDateParser = new SimpleDateFormat("yyyy-MM-dd");

    @RequestMapping(value = "/task/{taskId}/", method = RequestMethod.GET)
    public @ResponseBody Task getTask(@PathVariable("taskId") String taskId) {
        LOGGER.info(String.format("getTask %1$s", taskId));
        Task t = Task.findTask(taskId);
        setCustomTaskName(t);
        return t;
    }

    protected void setCustomTaskName(Task t) {
        if (processEngine.getRuntimeService().hasVariable(
                t.getProcessInstanceId(), "what")) {
            t.setName((String) processEngine.getRuntimeService().getVariable(
                    t.getProcessInstanceId(), "what"));
        }
    }

    @RequestMapping(value = "/task/{taskId}", method = RequestMethod.PUT)
    public @ResponseBody Task updateTask(
            @PathVariable("taskId") String taskId,
            @RequestBody(required = false) Task t,
            @RequestParam(required = false, value = "complete") String complete,
            @RequestParam(required = false, value = "defer") String defer) {
        LOGGER.info(String.format("updateTask %1$s", taskId));

        if (complete != null) {
            completeTask(taskId, Optional.ofNullable(t));
        } else {
            org.activiti.engine.task.Task dest = processEngine.getTaskService()
                    .createTaskQuery().taskId(taskId).singleResult();

            // convert date to store in UTC
            GregorianCalendar cal = new GregorianCalendar();
            cal.setTime(t.getDueDate());
            cal.add(Calendar.MILLISECOND,
                    cal.getTimeZone().getOffset(t.getDueDate().getTime()));
            dest.setDueDate(cal.getTime());

            processEngine.getTaskService().saveTask(dest);

            for (FormProperty fp : t.getFormProperties()) {
                processEngine.getTaskService().setVariable(
                        taskId, fp.getName(), fp.getValue());
            }

            if (defer != null) {
                try {
                    processEngine.getTaskService().setVariableLocal(taskId,
                            "deferUntil", isoDateParser.parse(defer));
                } catch (ParseException e) {
                    processEngine.getTaskService().setVariableLocal(taskId,
                            "deferUntil", getRelativeDate(defer));
                }
            }
        }
        return t;
    }

    protected void completeTask(String taskId, Optional<Task> t) {
        try {
            // TODO 4 Oct 17 Apparently this does not result in completer being recorded in history
            Authentication.setAuthenticatedUserId(
                    AuthenticationHelper.getUserId(
                            SecurityContextHolder.getContext().getAuthentication()));
            if (t.isPresent()) {
                LOGGER.debug(String.format(
                        "Completing task %1$s with variables %2$s",
                        taskId, t.get().getProcessVariables()));
                processEngine.getTaskService().complete(taskId,
                        t.get().getProcessVariables());
            } else {
                LOGGER.debug(String.format("Completing task %1$s", taskId));
                processEngine.getTaskService().complete(taskId);
            }
        } finally{
            Authentication.setAuthenticatedUserId(null);
        }
    }

    protected Date getRelativeDate(String defer) {
        defer = defer.toUpperCase();
        Calendar cal = new GregorianCalendar();
        if (defer.startsWith("P")) {
            try {
                Duration duration = DatatypeFactory.newInstance().newDuration(
                        defer);
                cal.setTimeInMillis(cal.getTimeInMillis()
                        + duration.getTimeInMillis(cal));
                return cal.getTime();
            } catch (DatatypeConfigurationException e) {
                String msg = "Unable to parse duration from " + defer;
                LOGGER.error(msg, e);
                throw new IllegalArgumentException(msg);
            }
        } else if (defer.startsWith("MON")) {
            cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
            cal.add(Calendar.WEEK_OF_YEAR, 1);
            return cal.getTime();
        } else {
            throw new IllegalArgumentException(String.format(
                    "Cannot find relative date from %1$s", defer));
        }
    }

    @RequestMapping(value = "/{tenantId}/tasks/findByVar/{varName}/{varValue}", method = RequestMethod.GET, headers = "Accept=application/json")
    public @ResponseBody List<Task> listForVar(
            @PathVariable("tenantId") String tenantId,
            @PathVariable("varName") String varName,
            @PathVariable("varValue") String varValue) {
        LOGGER.info(String.format("listInstancesForVar %1$s %2$s ", varName,
                varValue));

        List<Task> tasks = Task.wrap(processEngine.getTaskService()
                .createTaskQuery()
                .taskTenantId(tenantId)
                .processVariableValueLike(varName, ("%/" + varValue))
                .list());
        for (Task task : tasks) {
            setCustomTaskName(task);
        }

        return tasks;
    }

    @RequestMapping(value = "/{tenantId}/tasks/{username}", produces = {
            MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE })
    public @ResponseBody List<Task> listForUser(
            @PathVariable("tenantId") String tenantId,
            @PathVariable("username") String username,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "size", required = false) Integer size,
            @RequestParam(value = "sortFieldName", required = false) String sortFieldName,
            @RequestParam(value = "sortOrder", required = false) String sortOrder) {
        LOGGER.info(String.format("listForUser %1$s", username));

        if (page != null || size != null) {
            throw new IllegalStateException("No paging implemented yet");
        } else {
            List<Task> tasks = Task.findAllTasks(tenantId, username,
                    sortFieldName,
                    sortOrder);
            for (Task task : tasks) {
                setCustomTaskName(task);
            }
            return tasks;
        }
    }

    @RequestMapping(value = "/{tenantId}/tasks/{username}/", produces = MediaType.APPLICATION_ATOM_XML_VALUE)
    public @ResponseBody Feed listForUserAsAtom(
            @PathVariable("tenantId") String tenantId,
            @PathVariable("username") String username,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "size", required = false) Integer size,
            @RequestParam(value = "sortFieldName", required = false) String sortFieldName,
            @RequestParam(value = "sortOrder", required = false) String sortOrder)
            throws IOException {
        LOGGER.info(String.format("listForUserAsAtom %1$s", username));

        List<Task> tasks = null;
        if (page != null || size != null) {
            throw new IllegalStateException("No paging implemented yet");
        } else {
            tasks = Task.findAllTasks(tenantId, username, sortFieldName,
                    sortOrder);
        }

        Feed feed = new Feed();

        feed.setFeedType("atom_1.0");
        feed.setTitle(String.format("Task Atom Feed for %1$s", username));

        List<Entry> entries = new ArrayList<Entry>();
        for (Task task : tasks) {
            Entry entry = new Entry();
            entry.setId(Long.valueOf(task.getId()).toString());
            entry.setTitle(String.format("%1$s: %2$s", task.getName(),
                    task.getBusinessKey()));
            entry.setCreated(task.getCreateTime());
            entries.add(entry);
        }
        feed.setEntries(entries);
        return feed;
    }
}
