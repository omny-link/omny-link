package com.knowprocess.bpm.web;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.knowprocess.bpm.model.Task;

@RequestMapping("/tasks")
@Controller
public class TaskController {

    protected static final Logger LOGGER = LoggerFactory
            .getLogger(TaskController.class);

    @RequestMapping(value = "/{username}/")
    public @ResponseBody List<Task> listForUser(
            @PathVariable("username") String username,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "size", required = false) Integer size,
            @RequestParam(value = "sortFieldName", required = false) String sortFieldName,
            @RequestParam(value = "sortOrder", required = false) String sortOrder,
            Model uiModel) {
        LOGGER.info("listForUser");

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
            return Task.findAllTasks(username, sortFieldName, sortOrder);
        }
        // addDateTimeFormatPatterns(uiModel);
        // return "tasks/list";
    }
}
