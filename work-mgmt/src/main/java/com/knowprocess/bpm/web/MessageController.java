package com.knowprocess.bpm.web;

import java.io.StringReader;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonString;
import javax.json.JsonStructure;
import javax.json.JsonValue;
import javax.script.ScriptException;
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
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.util.UriComponentsBuilder;

import com.knowprocess.bpm.BadJsonMessageException;
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

    @RequestMapping(method = RequestMethod.GET, value = "/{tenant}/{msgId}", headers = "Accept=application/json")
    @ResponseBody
    public final ResponseEntity<String> doInOutMep(
            UriComponentsBuilder uriBuilder,
            @PathVariable("tenant") String tenantId,
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
            String msg = "";
            try {
                Method toJson = o.getClass().getMethod("toJson", new Class[0]);

                if (toJson != null) {
                    msg = (String) toJson.invoke(o, null);
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
            return new ResponseEntity(msg, headers, HttpStatus.OK);
        } catch (ActivitiException e) {
            throw e;
        } catch (Exception e) {
            ReportableException e2 = new ReportableException(e.getClass()
                    .getName() + ":" + e.getMessage(), e);
            return new ResponseEntity(e2.toJson(), headers,
                    HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping(method = RequestMethod.POST, value = "/{tenant}/{msgId}", headers = "Accept=application/json")
    @ResponseBody
    public ResponseEntity<String> doInOnlyMep(UriComponentsBuilder uriBuilder,
            @PathVariable("tenant") String tenantId,
            @PathVariable("msgId") String msgId,
            @RequestParam(required = false, value = "businessDescription") String bizDesc,
            @RequestParam String json) {
        long start = System.currentTimeMillis();
        LOGGER.info("handling In-Only MEP: " + msgId + ", json:" + json);

        Map<String, Object> vars = new HashMap<String, Object>();
        ResponseEntity<String> response = handleMep(uriBuilder, tenantId, msgId,
                bizDesc, json, vars, 0);

        LOGGER.debug(String.format("doInOnlyMep took: %1$s ms",
                (System.currentTimeMillis() - start)));
        return response;
    }

    protected ResponseEntity<String> handleMep(
            final UriComponentsBuilder uriBuilder, String tenantId,
            String msgId, String bizDesc, String jsonBody,
            final Map<String, Object> vars, int retry) {

        if (isEmptyJson(jsonBody))
            throw new BadJsonMessageException(
                    String.format(
                            "The JSON requested is empty or otherwise badly formed: %1$s",
                            jsonBody));
        // if (SecurityContextHolder.getContext().getAuthentication() == null
        // || "anonymousUser".equals(SecurityContextHolder.getContext()
        // .getAuthentication().getName())) {
            LOGGER.debug("allow Anonymous is set to " + allowAnonymous);
            if (Boolean.valueOf(allowAnonymous)) {
                LOGGER.warn("No user associated with this message, this may result in errors if the process author expected a username.");
            } else {
                ReportableException e = new ReportableException(
                        "Please ensure you are logged in before sending messages");
                return new ResponseEntity(e.toJson(), HttpStatus.UNAUTHORIZED);
            }
        // }
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");

        String bizKey = bizDesc == null ? msgId + " - "
                + isoFormatter.format(new Date()) : bizDesc;
        try {
            vars.put("tenantId", tenantId);
            String modifiedMsgId = getMessageVarName(msgId);
            vars.put("messageName", modifiedMsgId);
            if (messageRegistry.canDeserialise(modifiedMsgId, jsonBody)) {
                vars.put(modifiedMsgId,
                        messageRegistry.deserialiseMessage(msgId, jsonBody));
            } else {
                vars.put(modifiedMsgId, jsonManager.toObject(jsonBody));
            }
            // TODO deprecate query param?
            vars.put("query", jsonBody);

            try {
                String username = "anonymousUser";
                // String username = SecurityContextHolder.getContext()
                // .getAuthentication().getName();
                LOGGER.debug(" ... from " + username);
                Authentication.setAuthenticatedUserId(username);
                vars.put("initiator", username);
            } catch (Exception e) {
                LOGGER.warn("No initiator username available, attempting to continue");
            }
            LOGGER.debug(String.format("vars: %1$s", vars));
            ProcessInstance instance = processEngine.getRuntimeService()
                    .startProcessInstanceByMessageAndTenantId(msgId, bizKey,
                            vars, tenantId);
            vars.put("piid", instance.getId());
            com.knowprocess.bpm.model.ProcessInstance pi = com.knowprocess.bpm.model.ProcessInstance
                    .findProcessInstance(instance.getId());
            if (pi.getProcessVariables().containsKey("Location")) {
                String path = pi.getProcessVariables().get("Location")
                        .toString();
                if (!path.startsWith("http")) {
                    path = uriBuilder.path(path).build().toUriString();
                }
                headers.add("Location", path);
            } else {
                addLocationHeader(uriBuilder, headers, instance);
            }
            return new ResponseEntity<String>(headers, HttpStatus.CREATED);
        } catch (CannotCreateTransactionException e) {
            LOGGER.error(e.getClass().getName() + ":" + e.getMessage(), e);
            if (e.getCause() instanceof com.mysql.jdbc.exceptions.jdbc4.CommunicationsException
                    && retry < 3) {
                LOGGER.error("Confirmed timeout exception, retrying...");
                return handleMep(uriBuilder, tenantId, msgId, bizDesc,
                        jsonBody, vars, ++retry);
            } else {
                ReportableException e2 = new ReportableException(
                        "Cause is not timeout or retries exceeds limit", e);
                LOGGER.error("  retries: " + retry);
                LOGGER.error("  e.getCause() instanceof com.mysql.jdbc.exceptions.jdbc4.CommunicationsException"
                        + (e.getCause() instanceof com.mysql.jdbc.exceptions.jdbc4.CommunicationsException));
                LOGGER.error(e.getMessage(), e);
                return new ResponseEntity<String>(e2.toJson(), headers,
                        HttpStatus.SERVICE_UNAVAILABLE);
            }
        } catch (ActivitiObjectNotFoundException e) {
            if (e.getMessage().contains("no subscription to message with name")) {
                return startCatchAllProcess(uriBuilder, tenantId, vars,
                        headers, bizKey, e);
            } else if (e.getMessage()
                    .contains("no processes deployed with key")) {
                return startCatchAllProcess(uriBuilder, tenantId, vars,
                        headers, bizKey, e);
            } else {
                LOGGER.error("The ObjectNotFound below is NOT a missing process exception");
                LOGGER.error(e.getMessage(), e);
                throw e;
            }
        } catch (ActivitiException e) {
            LOGGER.error(e.getMessage(), e);
            ReportableException e2 = null;

            ConstraintViolationException cve = (ConstraintViolationException) isCausedBy(
                    e, ConstraintViolationException.class.getName());
            ScriptException se = (ScriptException) isCausedBy(e,
                    ScriptException.class.getName());
            if (cve != null) {
                e2 = new ReportableException(cve);
                return new ResponseEntity<String>(e2.toJson(), headers,
                        HttpStatus.BAD_REQUEST);
            } else if (se != null && se.getMessage().contains("OptimisticLock")) {
                LOGGER.info("Script Exception message: " + se.getMessage());
                e2 = new ReportableException(
                        "Optimistic locking exception, please reload the record and try again.");
                return new ResponseEntity<String>(e2.toJson(), headers,
                        HttpStatus.BAD_REQUEST);
            } else {
                startExceptionHandlerProcess(uriBuilder, tenantId, vars,
                        headers, bizKey, e);
                throw e;
                // e2 = new ReportableException(e.getClass().getName() + ":"
                // + e.getMessage(), e);
                // return new ResponseEntity<String>(e2.toJson(), headers,
                // HttpStatus.SERVICE_UNAVAILABLE);
            }
        } finally {
            Authentication.setAuthenticatedUserId(null);
        }
    }

    private ResponseEntity<String> startCatchAllProcess(
            final UriComponentsBuilder uriBuilder, String tenantId,
            final Map<String, Object> vars, HttpHeaders headers, String bizKey,
            ActivitiObjectNotFoundException e) {
        LOGGER.debug("Detected a missing process exception: " + e.getMessage());
        ProcessInstance instance = processEngine.getRuntimeService()
                .startProcessInstanceByKeyAndTenantId("CatchAllProcess",
                        bizKey,
                        vars, tenantId);
        addLocationHeader(uriBuilder, headers, instance);
        LOGGER.debug(String.format(
                "Created an instance of %1$s to handle it, id: %2$s",
                "CatchAllProcess", instance.getId()));
        return new ResponseEntity<String>(headers, HttpStatus.CREATED);
    }

    private ResponseEntity<String> startExceptionHandlerProcess(
            final UriComponentsBuilder uriBuilder, String tenantId,
            final Map<String, Object> vars, HttpHeaders headers, String bizKey,
            ActivitiException e) {
        LOGGER.debug("An unexpected exception occurred: " + e.getMessage());
        ProcessInstance instance = processEngine.getRuntimeService()
                .startProcessInstanceByKeyAndTenantId("CatchAllProcess",
                        bizKey,
                        vars, tenantId);
        addLocationHeader(uriBuilder, headers, instance);
        LOGGER.debug(String.format(
                "Created an instance of %1$s to handle it, id: %2$s",
                "CatchAllProcess", instance.getId()));
        return new ResponseEntity<String>(headers, HttpStatus.CREATED);
    }

    private void addLocationHeader(final UriComponentsBuilder uriBuilder,
            HttpHeaders headers, ProcessInstance instance) {
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
            LOGGER.error(e.getMessage(), e);
            return null;
        }
    }

    protected boolean isEmptyJson(String json) {
        if (json == null) {
            return true;
        }
        boolean isEmpty = false;

        JsonReader jsonReader = null;
        try {
            jsonReader = Json.createReader(new StringReader(json));

            JsonStructure jsonObject = jsonReader.read();

            if (jsonObject instanceof JsonObject) {
                JsonObject jo = (JsonObject) jsonObject;
                for (Entry<String, JsonValue> jv : jo.entrySet()) {
                    if (jv.getValue() != null) {
                        if (jv.getValue() instanceof JsonString) {
                            String s = jv.getValue().toString().trim();
                            // remove leading and trailing quotes
                            s = s.substring(1, s.length() - 1).trim();
                            return s.length() == 0;
                        } else {
                            return  true;
                        }
                    }
                }
            } else if (jsonObject instanceof JsonArray) {
                JsonArray ja = (JsonArray) jsonObject;
                return ja.toArray().length == 0;
            }

        } finally {
            jsonReader.close();
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
