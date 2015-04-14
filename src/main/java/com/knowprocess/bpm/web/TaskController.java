package com.knowprocess.bpm.web;

import java.util.List;

import org.activiti.engine.ProcessEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.knowprocess.bpm.model.Task;

@RequestMapping()
@Controller
public class TaskController {

    protected static final Logger LOGGER = LoggerFactory
            .getLogger(TaskController.class);

    @Autowired
    protected ProcessEngine processEngine;

    @RequestMapping(value = "/task/{taskId}/", method = RequestMethod.GET)
    public @ResponseBody Task getTask(
            @PathVariable("taskId") String taskId) {
        LOGGER.info(String.format("getTask %1$s", taskId));
        Task t = Task.findTask(taskId);
        return t;
    }

    @RequestMapping(value = "/task/{taskId}", method = RequestMethod.PUT)
    public @ResponseBody Task completeTask(
            @PathVariable("taskId") String taskId, @RequestBody Task t,
            @RequestParam(required = false, value = "complete") String complete) {
        LOGGER.info(String.format("completeTask %1$s", taskId));

        if (complete == null) {
            org.activiti.engine.task.Task dest = processEngine.getTaskService()
                    .createTaskQuery().taskId(taskId).singleResult();
            // BeanUtils.copyProperties(t, dest, new String[] {
            // "delegationState",
            // "revision" });

            processEngine.getTaskService().saveTask(dest);
        } else {
            // processEngine.getTaskService().complete(taskId,
            // t.getProcessVariables());
            processEngine.getTaskService().complete(taskId);
        }
        return t;
    }

    @RequestMapping(value = "/{tenantId}/tasks/{username}/")
    public @ResponseBody List<Task> listForUser(
            @PathVariable("tenantId") String tenantId,
            @PathVariable("username") String username,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "size", required = false) Integer size,
            @RequestParam(value = "sortFieldName", required = false) String sortFieldName,
            @RequestParam(value = "sortOrder", required = false) String sortOrder,
            Model uiModel) {
        LOGGER.info(String.format("listForUser %1$s", username));

        String requestor = "tim@omny.link";
        // = SecurityContextHolder.getContext()
        // .getAuthentication().getName();
        if (page != null || size != null) {
            // int sizeNo = size == null ? 10 : size.intValue();
            // final int firstResult = page == null ? 0 : (page.intValue() - 1)
            // * sizeNo;
            // Task.findTaskEntries(involvesUser, firstResult, sizeNo,
            // sortFieldName, sortOrder);
            // float nrOfPages = (float) Task.countTasks() / sizeNo;
            // uiModel.addAttribute(
            // "maxPages",
            // (int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ?
            // nrOfPages + 1
            // : nrOfPages));
            throw new IllegalStateException("No paging implemented yet");
        } else {
            return Task.findAllTasks(tenantId, username, sortFieldName,
                    sortOrder);
        }
        // addDateTimeFormatPatterns(uiModel);
        // return "tasks/list";
    }
}
