package org.activiti.spring.rest.web;
import org.activiti.spring.rest.model.Form;
import org.springframework.roo.addon.web.mvc.controller.json.RooWebJson;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.roo.addon.web.mvc.controller.scaffold.RooWebScaffold;

@RooWebJson(jsonObject = Form.class)
@Controller
@RequestMapping("/forms")
@RooWebScaffold(path = "forms", formBackingObject = Form.class)
public class FormController {
}
