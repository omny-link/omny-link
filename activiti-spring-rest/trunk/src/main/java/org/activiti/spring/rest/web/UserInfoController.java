package org.activiti.spring.rest.web;
import org.activiti.spring.rest.model.UserInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.roo.addon.web.mvc.controller.scaffold.RooWebScaffold;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/userinfoes")
@Controller
@RooWebScaffold(path = "userinfoes", formBackingObject = UserInfo.class)
public class UserInfoController {
	protected static final Logger LOGGER = LoggerFactory
			.getLogger(UserRecordController.class);
}
