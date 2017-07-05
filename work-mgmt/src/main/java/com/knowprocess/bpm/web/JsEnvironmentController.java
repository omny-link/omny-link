package com.knowprocess.bpm.web;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class JsEnvironmentController {

    protected static final Logger LOGGER = LoggerFactory
            .getLogger(JsEnvironmentController.class);

    @Value("${spring.application.name:Know Process}")
    protected String appName;

    @Value("${kp.app.tagLine:}")
    protected String tagLine;

    @Value("${spring.data.rest.baseUri}")
    protected String restBaseUri;

    @RequestMapping(path = "/js/env.js", method = RequestMethod.GET)
    public final String getEnvironment(HttpServletResponse resp, Model model)
            throws Exception {
        resp.setContentType("text/javascript");
        model.addAttribute("appName", appName);
        model.addAttribute("tagLine", tagLine);
        model.addAttribute("restBaseUri", restBaseUri);
        return "/js-env";
    }

}
