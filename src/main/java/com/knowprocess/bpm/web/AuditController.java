package com.knowprocess.bpm.web;

import org.activiti.engine.ProcessEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/audit-trails")
public class AuditController {

    protected static final Logger LOGGER = LoggerFactory
            .getLogger(AuditController.class);

    /**
     * Set true to make verbose debug level logging.
     */
    protected static boolean verbose;

    @Autowired
	ProcessEngine process;

}
