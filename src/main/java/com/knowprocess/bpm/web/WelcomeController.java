package com.knowprocess.bpm.web;

import java.util.Date;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class WelcomeController {

  @Value("${app.name:Studio}")
  private String name = "Studio";

  @RequestMapping("/")
  public String welcome(Map<String, Object> model) {
    model.put("time", new Date());
    model.put("app_name", name);
    return "index";
  }

}
