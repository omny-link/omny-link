package com.knowprocess.bpm.decisions.web;

import java.io.IOException;
import java.util.Map;

import link.omny.decisions.api.DecisionException;
import link.omny.decisions.impl.DecisionModelFactory;
import link.omny.decisions.impl.DecisionService;
import link.omny.decisions.model.Decision;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Handle REST requests to decision service.
 * 
 * @author Tim Stephenson
 * 
 */
@Controller
@RequestMapping("/decisions")
@RestController
public class DecisionController {

    protected static final Logger LOGGER = LoggerFactory
            .getLogger(DecisionController.class);

    @Autowired
    private DecisionModelFactory decisionModelFactory;

    @Autowired
    private DecisionService decisionService;

    @RequestMapping("/")
    public String index() {
        return "Omny.link installed and running!\nTODO there should be a form here.";
    }

    @RequestMapping(method = RequestMethod.GET, value = "/{definitionsId}/{decisionId}", headers = "Accept=application/json")
    @ResponseBody
    public final String executeDecision(
            UriComponentsBuilder uriBuilder,
            @PathVariable("definitionsId") String definitionsId,
            @PathVariable("decisionId") String decisionId,
            @RequestParam Map<String, String> params) throws IOException,
            DecisionException {
        LOGGER.info(String.format(
                "handling request to decision: %1$s, with params: %2$s",
                decisionId, params));

        Decision d = decisionModelFactory.find(definitionsId, decisionId);
        String jsonOut = decisionService.execute(d, params).get("conclusion");
        LOGGER.info(String.format("decision conclusion: %1$s", jsonOut));

        return jsonOut;
    }
}
