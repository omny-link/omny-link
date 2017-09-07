package com.knowprocess.bpm.web;

import java.io.StringReader;
import java.lang.reflect.Method;
import java.security.Principal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonString;
import javax.json.JsonStructure;
import javax.json.JsonValue;
import javax.json.stream.JsonParsingException;
import javax.script.ScriptException;
import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolationException;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.impl.identity.Authentication;
import org.activiti.engine.runtime.ProcessInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.util.UriComponentsBuilder;

import com.knowprocess.bpm.MultiTenantActivitiProperties;
import com.knowprocess.bpm.api.BadJsonMessageException;
import com.knowprocess.bpm.api.ReportableException;
import com.knowprocess.bpm.impl.JsonManager;
import com.knowprocess.bpm.impl.MessageRegistry;

/**
 * Handle REST requests sending BPMN message events to start or modify process
 * instances.
 *
 * @author Tim Stephenson
 *
 */
@Controller
@RequestMapping("/msg")
public class MessageController {

    protected static final Logger LOGGER = LoggerFactory
            .getLogger(MessageController.class);

    @Autowired
    private MultiTenantActivitiProperties activitiMultiTenantProperties;

    protected static ProcessEngine processEngine;

    @Autowired
    protected MessageRegistry messageRegistry;

    @Autowired
    protected JsonManager jsonManager;

    /**
     * Whether messages may be sent anonymously. Default: false.
     */
    // TODO cannot figure how to get spring do property substitution
    // @Value("${message.allowAnonymous}")
    private String allowAnonymous = "true";

    private static final DateFormat isoFormatter = new SimpleDateFormat(
            "yyyy-MM-dd HH:mm");

    @Autowired
    public void setProcessEngine(ProcessEngine pe) {
        LOGGER.debug("Injected process engine into " + getClass().getName());
        MessageController.processEngine = pe;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/{tenantId}/{msgId}",
            headers = "Accept=application/json")
    @ResponseBody
    public final ResponseEntity<String> doInOutMep(
            UriComponentsBuilder uriBuilder,
            @PathVariable("tenantId") String tenantId,
            @PathVariable("msgId") String msgId,
            @RequestParam(required = false, value = "businessDescription") String bizDesc,
            @RequestParam(required = false, value = "query") String json) {
        // TODO need to look into why but the PathVariable is truncated at the
        // last . so 'kp.foo' arrives as 'kp'. To work around this clients
        // should send kp.foo.json or similar/
        LOGGER.info(String.format("handling In-Out MEP to: %1$s, json: %2$s",
                msgId, json));

        Map<String, Object> vars = new HashMap<String, Object>();
        HttpHeaders headers = null;
        try {
            ResponseEntity<String> response = handleMep(uriBuilder, tenantId,
                    msgId, bizDesc, json, vars, 0);
            if (response.getStatusCode() != HttpStatus.CREATED) {
                return response;
            }
            headers = response.getHeaders();
            Object o = com.knowprocess.bpm.model.ProcessInstance
                    .findProcessInstance((String) vars.get("piid"))
                    .getProcessVariables()
                    .get(getMessageVarName(msgId));
            LOGGER.debug(String.format("Object to return: %1$s", o));
            if (o == null) {
                String msg = String
                        .format("No message returned from request to %1$s. If this is expected, please POST instead of GET",
                                msgId);
                LOGGER.warn(msg);
                return new ResponseEntity<String>(msg, headers, HttpStatus.OK);
            }
            String msg = null;
            try {
                Method toJson = o.getClass().getMethod("toJson", new Class[0]);

                if (toJson != null) {
                    msg = (String) toJson.invoke(o, (Object[]) null);
                } else if (o != null) {
                    msg = o.toString();
                }
            } catch (java.lang.NoSuchMethodException e) {
                // Tant pis!
                if (LOGGER.isWarnEnabled()) {
                    LOGGER.warn("No toJson method on " + o);
                }
                msg = o.toString();
            }
            LOGGER.debug("msg: " + msg);
            return new ResponseEntity<String>(msg, headers, HttpStatus.CREATED);
        } catch (ActivitiException e) {
            throw e;
        } catch (Exception e) {
            ReportableException e2 = new ReportableException(e.getClass()
                    .getName() + ":" + e.getMessage(), e);
            return new ResponseEntity<String>(e2.toJson(), headers,
                    HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping(method = RequestMethod.POST, value = "/{tenantId}/{msgId}",
            headers = { "Accept=application/json" },
            consumes = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_JSON_UTF8_VALUE })
    @ResponseBody
    public ResponseEntity<String> doInOnlyMepAsBody(UriComponentsBuilder uriBuilder,
            @PathVariable("tenantId") String tenantId,
            @PathVariable("msgId") String msgId,
            @RequestParam(required = false, value = "businessDescription") String bizDesc,
            @RequestBody String json) {
        long start = System.currentTimeMillis();
        LOGGER.info("doInOnlyMepAsBody: handling In-Only MEP to {}, biz key: {}, payload: {}", msgId, bizDesc, json);

        Map<String, Object> vars = new HashMap<String, Object>();
        ResponseEntity<String> response = handleMep(uriBuilder, tenantId,
                msgId,
                bizDesc, json, vars, 0);

        LOGGER.debug(String.format("doInOnlyMep took: %1$s ms",
                (System.currentTimeMillis() - start)));
        return response;
    }

    @RequestMapping(method = RequestMethod.POST, value = "/{tenantId}/{msgId}",
            headers = { "Accept=application/json" },
            consumes = { MediaType.APPLICATION_FORM_URLENCODED_VALUE, "application/x-www-form-urlencoded; charset=UTF-8" })
    @ResponseBody
    public ResponseEntity<String> doInOnlyMep(UriComponentsBuilder uriBuilder,
            @PathVariable("tenantId") String tenantId,
            @PathVariable("msgId") String msgId,
            @RequestParam(required = false, value = "businessDescription") String bizDesc,
            @RequestParam(required = false) String json) {
        long start = System.currentTimeMillis();
        LOGGER.info("doInOnlyMep: handling In-Only MEP to {}, biz key: {}, payload: {}", msgId, bizDesc, json);

        Map<String, Object> vars = new HashMap<String, Object>();
        ResponseEntity<String> response = handleMep(uriBuilder, tenantId,
                msgId,
                bizDesc, json, vars, 0);

        LOGGER.debug(String.format("doInOnlyMep took: %1$s ms",
                (System.currentTimeMillis() - start)));
        return response;
    }

    // On the face of it this not needed but apparently the forms plugin
    // matches this set of headers / consumes, so leave for now
    @RequestMapping(method = RequestMethod.POST, value = "/{tenantId}/{msgId}",
            headers = { "Accept=application/json" })
    @ResponseBody
    public ResponseEntity<String> doInOnlyMep2(HttpServletRequest request,
            UriComponentsBuilder uriBuilder,
            @PathVariable("tenantId") String tenantId,
            @PathVariable("msgId") String msgId,
            @RequestParam(required = false, value = "businessDescription") String bizDesc,
            @RequestParam(required = false) String json) {
        LOGGER.info("doInOnlyMep2: handling In-Only MEP to {}, biz key: {}, payload: {}, Content-Type, Accept",
                msgId, bizDesc, json, request.getHeader("Content-Type"), request.getHeader("Accept"));

        return doInOnlyMep(uriBuilder, tenantId, msgId, bizDesc, json);
    }

    protected ResponseEntity<String> handleMep(
            final UriComponentsBuilder uriBuilder, String tenantId,
            String msgId, String bizDesc, String jsonBody,
            final Map<String, Object> vars, int retry) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");

        com.knowprocess.bpm.model.ProcessInstance instance = handleMep(
                tenantId, msgId, bizDesc, jsonBody, vars, retry);

        if (instance.getProcessVariables().containsKey("Location")) {
            String path = instance.getProcessVariables().get("Location")
                    .toString();
            if (!path.startsWith("http")) {
                path = uriBuilder.path(path).build().toUriString();
            }
            headers.add("Location", path);
        } else {
            addLocationHeader(uriBuilder, headers, instance);
        }
        return new ResponseEntity<String>(headers, HttpStatus.CREATED);
    }

    /**
     *
     * @param tenantId
     * @param msgId
     * @param bizDesc
     * @param jsonBody
     *            May be null but if not, must be valid JSON msg payload.
     * @param vars
     * @param retry
     *            Specify zero on first attempt, will be incremented internally
     *            on each retry.
     * @return the instance started
     */
    protected com.knowprocess.bpm.model.ProcessInstance handleMep(
            String tenantId,
            String msgId, String bizDesc, String jsonBody,
            final Map<String, Object> vars, int retry) {

        addJsonToVars(msgId, vars, jsonBody);


        String bizKey = bizDesc == null ? msgId + " - "
                + isoFormatter.format(new Date()) : bizDesc;
        try {
            vars.put("tenantId", tenantId);
            addInitiatorToVars(tenantId, vars);

            LOGGER.debug(String.format("vars: %1$s", vars));
            ProcessInstance instance = processEngine.getRuntimeService()
                    .startProcessInstanceByMessageAndTenantId(msgId, bizKey,
                            vars, tenantId);
            vars.put("piid", instance.getId());
            com.knowprocess.bpm.model.ProcessInstance pi = com.knowprocess.bpm.model.ProcessInstance
                    .findProcessInstance(instance.getId());
            return pi;
        } catch (CannotCreateTransactionException e) {
            LOGGER.error(e.getClass().getName() + ":" + e.getMessage(), e);
            if (e.getCause() instanceof com.mysql.jdbc.exceptions.jdbc4.CommunicationsException
                    && retry < 3) {
                LOGGER.error("Confirmed timeout exception, retrying...");
                return handleMep(tenantId, msgId, bizDesc,
                        jsonBody, vars, ++retry);
            } else {
                ReportableException e2 = new ReportableException(
                        "Cause is not timeout or retries exceeds limit", e);
                LOGGER.error("  retries: " + retry);
                LOGGER.error("  e.getCause() instanceof com.mysql.jdbc.exceptions.jdbc4.CommunicationsException"
                        + (e.getCause() instanceof com.mysql.jdbc.exceptions.jdbc4.CommunicationsException));
                LOGGER.error(e.getMessage(), e);
                throw e2;
            }
        } catch (ActivitiObjectNotFoundException e) {
            if (e.getMessage().contains("no subscription to message with name")) {
                LOGGER.error(e.getMessage());
                return startCatchAllProcess(tenantId, vars, bizKey, e);
            } else if (e.getMessage()
                    .contains("no processes deployed with key")) {
                LOGGER.error(e.getMessage());
                return startCatchAllProcess(tenantId, vars, bizKey, e);
            } else {
                LOGGER.error("The ObjectNotFound below is NOT a missing process exception");
                LOGGER.error(e.getMessage(), e);
                throw e;
            }
        } catch (ActivitiException e) {
            LOGGER.error(String.format("%1$s: %2$s", e.getClass().getName(),
                    e.getMessage()));
            ReportableException e2 = null;

            ConstraintViolationException cve = (ConstraintViolationException) isCausedBy(
                    e, ConstraintViolationException.class.getName());
            ScriptException se = (ScriptException) isCausedBy(e,
                    ScriptException.class.getName());
            if (cve != null) {
                e2 = new ReportableException(cve);
                throw e2;
            } else if (se != null && se.getMessage().contains("OptimisticLock")) {
                LOGGER.info("Script Exception message: " + se.getMessage());
                e2 = new ReportableException(
                        "Optimistic locking exception, please reload the record and try again.");
                throw e2;
            } else {
                startExceptionHandlerProcess(tenantId, vars, bizKey, e);
                throw e;
                // e2 = new ReportableException(e.getClass().getName() + ":"
                // + e.getMessage(), e);
                // return new ResponseEntity<String>(e2.toJson(), headers,
                // HttpStatus.SERVICE_UNAVAILABLE);
            }
        } catch (Throwable e) {
            LOGGER.error("Process exception: {}", e.getMessage());
            throw e;
        } finally {
            Authentication.setAuthenticatedUserId(null);
        }
    }

    private void addInitiatorToVars(final String tenantId,
            final Map<String, Object> vars) {
        try {
            String username = "";
            org.springframework.security.core.Authentication authentication = SecurityContextHolder
                    .getContext().getAuthentication();
            if (authentication.isAuthenticated()) {
                if (authentication.getPrincipal() instanceof Principal) {
                    String tmp = ((Principal) authentication.getPrincipal())
                            .getName();
                    if (tmp.contains("@")) { // trust that this is an email addr
                        username = tmp;
                    } else {
                        LOGGER.warn("Username '{}' is not an email address, ignoring, this may result in errors if the process author expected a username.", tmp);
                    }
                } else if (authentication.getPrincipal() instanceof String) {
                    username = (String) authentication.getPrincipal();
                } else {
                    LOGGER.warn(
                            "Authenticated but principal of unknown type {}",
                            authentication.getPrincipal().getClass().getName());
                }
            } else if (activitiMultiTenantProperties.getServers()
                    .containsKey(tenantId)) {
                username = activitiMultiTenantProperties.getServers()
                        .get(tenantId).getMailServerDefaultFrom();
            }
            LOGGER.debug(" ... from " + username);
            Authentication.setAuthenticatedUserId(username);
            vars.put("initiator", username);
        } catch (Exception e) {
            LOGGER.debug("allow Anonymous is set to " + allowAnonymous);
            if (Boolean.valueOf(allowAnonymous)) {
                LOGGER.warn(
                        "No user associated with this message, this may result in errors if the process author expected a username.");
            } else {
                throw new ReportableException(
                        "Please ensure you are logged in before sending messages");
            }
        }
    }

    private void addJsonToVars(final String msgId,
            final Map<String, Object> vars, final String jsonBody) {
        if (jsonBody != null && isEmptyJson(jsonBody))
            throw new BadJsonMessageException(String.format(
                    "The JSON requested is empty or otherwise badly formed: %1$s",
                    jsonBody));
        String modifiedMsgId = getMessageVarName(msgId);
        vars.put("messageName", modifiedMsgId);

        if (jsonBody == null) {
            LOGGER.info("No JSON in body of message: {}", msgId);
        } else {
            if (messageRegistry.canDeserialise(modifiedMsgId, jsonBody)) {
                vars.put(modifiedMsgId,
                        messageRegistry.deserialiseMessage(msgId, jsonBody));
            } else {
                vars.put(modifiedMsgId, jsonManager.toObject(jsonBody));
            }
            // TODO deprecate query param?
            vars.put("query", jsonBody);
        }
    }

    private com.knowprocess.bpm.model.ProcessInstance startCatchAllProcess(
            String tenantId,
            final Map<String, Object> vars, String bizKey,
            ActivitiObjectNotFoundException e) {
        LOGGER.debug("Detected a missing process exception: " + e.getMessage());
        ProcessInstance instance = processEngine.getRuntimeService()
                .startProcessInstanceByKeyAndTenantId("CatchAllProcess",
                        bizKey,
                        vars, tenantId);

        LOGGER.debug(String.format(
                "Created an instance of %1$s to handle it, id: %2$s",
                "CatchAllProcess", instance.getId()));
        return new com.knowprocess.bpm.model.ProcessInstance(instance);
    }

    private com.knowprocess.bpm.model.ProcessInstance startExceptionHandlerProcess(
            String tenantId,
            final Map<String, Object> vars, String bizKey, ActivitiException e) {
        LOGGER.debug("An unexpected exception occurred: " + e.getMessage());
        ProcessInstance instance = processEngine.getRuntimeService()
                .startProcessInstanceByKeyAndTenantId("CatchAllProcess",
                        bizKey, vars, tenantId);
        LOGGER.debug(String.format(
                "Created an instance of %1$s to handle it, id: %2$s",
                "CatchAllProcess", instance.getId()));
        return new com.knowprocess.bpm.model.ProcessInstance(instance);
    }

    private void addLocationHeader(final UriComponentsBuilder uriBuilder,
            HttpHeaders headers,
            com.knowprocess.bpm.model.ProcessInstance instance) {
        RequestMapping a = ProcessInstanceController.class
                .getAnnotation(RequestMapping.class);
        String basePath = a.value()[0];
        if (basePath.contains("{tenantId}")) {
            basePath = basePath.replace("{tenantId}", instance.getTenantId());
        }
        headers.add("Location",
                uriBuilder.path(basePath + "/" + instance.getId()).build()
                        .toUriString());
    }

	protected String getMessageVarName(String msgId) {
        // Fix message name to avoid dot notation scripting errors
        msgId = msgId.replace('.', '_');
        LOGGER.debug(String.format("Message name: %1$s", msgId));
        return msgId;
    }

    private Throwable isCausedBy(Throwable e, String className) {
        LOGGER.info("Considering cause: " + e.getCause());
        if (e.getCause() != null
                && e.getCause().getClass().getName().equals(className)) {
            return e.getCause();
        } else if (e.getCause() != null) {
            return isCausedBy(e.getCause(), className);
        } else {
            LOGGER.error(e.getMessage());
            return null;
        }
    }

    protected boolean isEmptyJson(String json) {
        if (json == null) {
            return true;
        }
        boolean isEmpty = true;

        JsonReader jsonReader = null;
        try {
            jsonReader = Json.createReader(new StringReader(json));

            JsonStructure jsonObject = jsonReader.read();

            if (jsonObject instanceof JsonObject) {
                JsonObject jo = (JsonObject) jsonObject;
                if (!isEmpty(jo)) {
                    return false;
                }
            } else if (jsonObject instanceof JsonArray) {
                JsonArray ja = (JsonArray) jsonObject;
                return ja.toArray().length == 0;
            }
        } catch (JsonParsingException e) {
            LOGGER.error(e.getMessage());
            LOGGER.error("json received: " + json);
            return true;
        } finally {
            if (jsonReader != null) {
                jsonReader.close();
            }
        }
        return isEmpty;
    }

    private boolean isEmpty(JsonObject jo) {
        boolean isEmpty = true;
        for (Entry<String, JsonValue> jv : jo.entrySet()) {
            if (jv.getValue() != null) {
                if (jv.getValue() instanceof JsonString) {
                    String s = jv.getValue().toString().trim();
                    // remove leading and trailing quotes
                    s = s.substring(1, s.length() - 1).trim();
                    isEmpty = s.length() == 0;
                } else if (jv.getValue() instanceof JsonNumber) {
                    isEmpty = false;
                } else if (jv.getValue() instanceof JsonObject) {
                    return isEmpty((JsonObject) jv.getValue());
                } else if (jv.getValue() instanceof JsonArray) {
                    return ((JsonArray) jv.getValue()).size() == 0;
                }
            }
        }
        return isEmpty;
    }

    protected String parseInstanceIdFromLocation(ResponseEntity<String> response) {
        if (response.getStatusCode().compareTo(HttpStatus.BAD_REQUEST) < 0) {
            String locationHeader = response.getHeaders().get("Location")
                    .get(0);
            LOGGER.debug("location: " + locationHeader);
            String id = locationHeader.substring(locationHeader
                    .lastIndexOf('/') + 1);
            LOGGER.debug("id: " + id);
            return id;
        } else {
            throw new ActivitiException("Cannot find location");
        }
    }

}
