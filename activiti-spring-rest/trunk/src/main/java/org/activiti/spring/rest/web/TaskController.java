package org.activiti.spring.rest.web;
import org.activiti.spring.rest.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.roo.addon.web.mvc.controller.json.RooWebJson;
import org.springframework.roo.addon.web.mvc.controller.scaffold.RooWebScaffold;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RequestMapping("/tasks")
@Controller
@RooWebScaffold(path = "tasks", formBackingObject = Task.class)
@RooWebJson(jsonObject = Task.class)
public class TaskController {

    protected static final Logger LOGGER = LoggerFactory
    		.getLogger(TaskController.class);

    @RequestMapping(produces = "text/html", value = "/{username}")
    public String listForUser(
            @PathVariable("username") String msgId,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "size", required = false) Integer size,
            @RequestParam(value = "sortFieldName", required = false) String sortFieldName,
            @RequestParam(value = "sortOrder", required = false) String sortOrder,
            Model uiModel) {
        String involvesUser = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        if (page != null || size != null) {
            int sizeNo = size == null ? 10 : size.intValue();
            final int firstResult = page == null ? 0 : (page.intValue() - 1)
                    * sizeNo;
            uiModel.addAttribute("tasks", Task.findTaskEntries(involvesUser,
                    firstResult, sizeNo, sortFieldName, sortOrder));
            float nrOfPages = (float) Task.countTasks() / sizeNo;
            uiModel.addAttribute(
                    "maxPages",
                    (int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ? nrOfPages + 1
                            : nrOfPages));
        } else {
            uiModel.addAttribute("tasks",
                    Task.findAllTasks(involvesUser, sortFieldName, sortOrder));
        }
        addDateTimeFormatPatterns(uiModel);
        return "tasks/list";
    }
}
