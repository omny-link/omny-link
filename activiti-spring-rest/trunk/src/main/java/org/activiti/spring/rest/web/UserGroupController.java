package org.activiti.spring.rest.web;
import org.activiti.spring.rest.model.UserGroup;
import org.springframework.roo.addon.web.mvc.controller.scaffold.RooWebScaffold;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/groups")
@Controller
@RooWebScaffold(path = "groups", formBackingObject = UserGroup.class)
public class UserGroupController {
}
