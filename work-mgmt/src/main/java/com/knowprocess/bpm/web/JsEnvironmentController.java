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

    @Value("${kp.application.tag-line:}")
    protected String tagLine;

    @Value("${server.port:8080}")
    protected String serverPort;

    @Value("${spring.data.rest.base-path:}")
    protected String restBaseUri;

    public String getRestBaseUri() {
        if (restBaseUri.length() == 0) {
			restBaseUri = "http://localhost:" + serverPort;
        }
		return restBaseUri;
	}

	@RequestMapping(path = "/js/env.js", method = RequestMethod.GET)
    public final String getEnvironment(HttpServletResponse resp, Model model)
            throws Exception {
        resp.setContentType("text/javascript");
        model.addAttribute("appName", appName);
        model.addAttribute("tagLine", tagLine);
        model.addAttribute("restBaseUri", getRestBaseUri());
        return "/js-env";
    }

}