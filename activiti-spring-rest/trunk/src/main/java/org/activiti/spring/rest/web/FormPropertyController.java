package org.activiti.spring.rest.web;
import org.activiti.spring.rest.model.FormProperty;
import org.springframework.roo.addon.web.mvc.controller.json.RooWebJson;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.roo.addon.web.mvc.controller.scaffold.RooWebScaffold;

@RooWebJson(jsonObject = FormProperty.class)
@Controller
@RequestMapping("/formpropertys")
@RooWebScaffold(path = "formpropertys", formBackingObject = FormProperty.class)
public class FormPropertyController {
}
