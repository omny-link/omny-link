/*******************************************************************************
 * Copyright 2015-2022 Tim Stephenson and contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package link.omny.server.web;

import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.net.URI;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import jakarta.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.annotations.ApiOperation;
import link.omny.server.CrmBpmProperies;
import link.omny.server.ProcessStartException;

@RestController
public class ProcessGatewayController {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(ProcessGatewayController.class);

    private static final String JSON_REQUEST_TEMPLATE = "{\"message\":\"%1$s\","
            + "\"businessKey\":\"%2$s\","
            + "\"name\":\"%3$s\","
            + "\"variables\":["
                + "{\"name\":\"%4$s\",\"type\":\"json\",\"value\":%5$s},"
                + "{\"name\":\"tenantId\",\"value\":\"%6$s\"}"
            + "]}";

    private static final String FORM_REQUEST_TEMPLATE
            = "{\"processDefinitionKey\":\"%1$s\","
            + "\"businessKey\":\"%2$s\","
            + "\"name\":\"%3$s\","
            + "\"variables\":[%4$s]}";

    private static final String VAR_REQUEST_TEMPLATE
                = "{\"name\":\"%1$s\",\"type\":\"%2$s\",\"value\":\"%3$s\"}";

    private static final DateFormat iso = new SimpleDateFormat("yyyy-MM-dd-HH:mm");

    protected CrmBpmProperies crmProps;

    protected String processEndpoint;

    private final RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    public ProcessGatewayController (RestTemplateBuilder restTemplateBuilder, CrmBpmProperies crmProps) {
        // set connection and read timeouts
        this.restTemplate = restTemplateBuilder
                .setConnectTimeout(Duration.ofSeconds(500))
                .setReadTimeout(Duration.ofSeconds(500))
                .build();
        this.crmProps = crmProps;
        processEndpoint = String.format("%1$s/runtime/process-instances/", crmProps.getProcessGateway());
        LOGGER.info("Configured to call process endpoint at {}", processEndpoint);
    }

    @SuppressWarnings("deprecation")
    @PostMapping(value = "/msg/{tenantId}/{msgName}.json",
            consumes = { APPLICATION_JSON_VALUE, APPLICATION_JSON_UTF8_VALUE })
    @ApiOperation(value = "Webhook endpoint to start a process from a JSON message.")
    public @ResponseBody ResponseEntity<?> hookProcessToJsonMessage(
            @PathVariable("tenantId") String tenantId,
            @PathVariable("msgName") String msgName,
            @RequestParam(value = "name", required = false, defaultValue = "-") String name,
            @RequestParam(value = "businessKey", required = false) String bizKey,
            @RequestBody String json, HttpServletRequest req) {
        LOGGER.info("hookProcessToJsonMessage");
        bizKey = bizKey == null ? msgName + "-" + iso.format(new Date()) : bizKey;
        String body = String.format(JSON_REQUEST_TEMPLATE,
                msgName, bizKey, name, msgName, json, tenantId);
        return startProcess(req.getHeader("Authorization"), bizKey, name, body);
    }

    @PostMapping(value = "/form/{tenantId}/{procKey}.action")
    @ApiOperation(value = "Webhook endpoint to start a process from a form.")
    public @ResponseBody ResponseEntity<?> hookProcessToForm(
            @PathVariable("tenantId") String tenantId,
            @PathVariable("procKey") String procKey,
            @RequestParam(value = "name", required = false, defaultValue = "-") String name,
            @RequestParam(value = "businessKey", required = false) String bizKey,
            HttpServletRequest req) {
        LOGGER.info("hookProcessToForm");
        StringBuilder vars = new StringBuilder();
        for (Iterator<Entry<String, String[]>> it = req.getParameterMap()
                .entrySet().iterator(); it.hasNext();) {
            Map.Entry<String, String[]> entry = it.next();
            vars.append(String.format(VAR_REQUEST_TEMPLATE, entry.getKey(),
                    "string", entry.getValue()[0]));
            if (it.hasNext()) {
                vars.append(",");
            }
        }
        String body = String.format(FORM_REQUEST_TEMPLATE, procKey, bizKey,
                name, vars.toString());
        return startProcess(req.getHeader("Authorization"), bizKey, name, body);
    }

    // On the face of it this not needed but apparently the forms plugin
    // matches this set of headers / consumes, so leave for now
    @PostMapping(value = "/msg/{tenantId}/{msgId}.json",
            headers = { "Accept=application/json" })
    @ResponseBody
    public ResponseEntity<?> legacyHookProcessToJsonMessage(HttpServletRequest request,
            UriComponentsBuilder uriBuilder,
            @PathVariable("tenantId") String tenantId,
            @PathVariable("msgId") String msgId,
            @RequestParam(required = false, value = "businessDescription") String bizDesc,
            @RequestParam(required = false) String json) {
        LOGGER.info("legacyHookProcessToJsonMessage: msg: {}, biz key: {},"
                + "payload: {}, Content-Type: {}, Accept: {}",
                msgId, bizDesc, json, request.getHeader("Content-Type"),
                request.getHeader("Accept"));
        return hookProcessToJsonMessage(tenantId, msgId, "-", bizDesc, json, request);
    }

    private ResponseEntity<?> startProcess(String authorization, String bizKey,
            String name, String body) {
        LOGGER.info("startProcess {}", name);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Accept", APPLICATION_JSON_VALUE);
        headers.add("Authorization", authorization);
        headers.add("Content-Type", APPLICATION_JSON_VALUE);

        LOGGER.debug("Composed payload: {}", body);
        HttpEntity<String> entity = new HttpEntity<String>(body, headers);
        try {
            String response = restTemplate.postForObject(processEndpoint,
                    entity, String.class);
            JsonNode responseTree = objectMapper.readTree(response);
            LOGGER.debug("  received response: {}", response);
            String location = processEndpoint + responseTree.get("id").asText();
            LOGGER.debug("  received location: {}", location);
            return ResponseEntity.created(URI.create(location)).build();
        } catch (HttpClientErrorException e) {
            LOGGER.error("Process server threw exception");
            throw e;
        } catch (JsonProcessingException e) {
            LOGGER.error("Process server threw exception");
            throw new ProcessStartException("Unable to parse response", e);
        }
    }
}