package org.activiti.spring.rest.web;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.validation.ConstraintViolationException;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.impl.identity.Authentication;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.spring.rest.ActivitiRestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Handle REST requests sending BPMN message events to start or modify process
 * instances.
 * 
 * @author Tim Stephenson
 * 
 */
// @RooWebJson(jsonObject = Deployment.class)
@Controller
@RequestMapping("/msg")
// @RooWebScaffold(path = "msg", formBackingObject = Deployment.class)
public class MessageController {

    protected static final Logger LOGGER = LoggerFactory
            .getLogger(MessageController.class);

    protected static ProcessEngine processEngine;

    /**
     * Whether messages may be sent anonymously. Default: false.
     */
    // TODO cannot figure how to get spring do property substitution
    // @Value("${message.allowAnonymous}")
    private String allowAnonymous = "true";

    @Autowired
    public void setProcessEngine(ProcessEngine pe) {
        LOGGER.debug("Injected process engine into " + getClass().getName());
        MessageController.processEngine = pe;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/{msgId}", headers = "Accept=application/json")
    @ResponseBody
    public final ResponseEntity<String> doInOutMep(
            UriComponentsBuilder uriBuilder,
            @PathVariable("msgId") String msgId,
            @RequestParam(required = false, value = "query") String json) {
        // TODO need to look into why but the PathVariable is truncated at the
        // last . so 'kp.foo' arrives as 'kp'. To work around this clients
        // should send kp.foo.json or similar/
        LOGGER.info(String.format("handling In-Out MEP to: %1$s, json: %2$s",
                msgId, json));
        String id = "";
        Map<String, Object> vars = new HashMap<String, Object>();
        HttpHeaders headers = null;
        try {
            ResponseEntity<String> response = handleMep(uriBuilder, msgId,
                    json, vars, 0);
            headers = response.getHeaders();
            id = parseInstanceIdFromLocation(response);
            String msg = (String) org.activiti.spring.rest.model.ProcessInstance
                    .findProcessInstance(id).getProcessVariables().get(msgId);
            LOGGER.debug("msg: " + msg);
            return new ResponseEntity(msg, headers, HttpStatus.OK);
        } catch (ActivitiRestException e) {
            ReportableException e2 = new ReportableException(e.getClass()
                    .getName() + ":" + e.getMessage(), e);
            return new ResponseEntity(e2.toJson(), headers,
                    HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            ReportableException e2 = new ReportableException(e.getClass()
                    .getName() + ":" + e.getMessage(), e);
            return new ResponseEntity(e2.toJson(), headers,
                    HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping(method = RequestMethod.POST, value = "/{msgId}", headers = "Accept=application/json")
    @ResponseBody
    public ResponseEntity<String> doInOnlyMep(UriComponentsBuilder uriBuilder,
            @PathVariable("msgId") String msgId, @RequestParam String json) {
        long start = System.currentTimeMillis();
        LOGGER.info("handling In-Only MEP: " + msgId + ", json:" + json);

        Map<String, Object> vars = new HashMap<String, Object>();
        ResponseEntity<String> response = handleMep(uriBuilder, msgId, json,
                vars, 0);

        LOGGER.debug(String.format("doInOnlyMep took: %1$s ms",
                (System.currentTimeMillis() - start)));
        return response;
    }

    protected ResponseEntity<String> handleMep(UriComponentsBuilder uriBuilder,
            String msgId, String jsonBody, Map<String, Object> vars, int retry) {
        if (SecurityContextHolder.getContext().getAuthentication() == null
                || "anonymousUser".equals(SecurityContextHolder.getContext()
                        .getAuthentication().getName())) {
            LOGGER.debug("allow Anonymous is set to " + allowAnonymous);
            if (Boolean.valueOf(allowAnonymous)) {
                LOGGER.warn("No user associated with this message, this may result in errors if the process author expected a username.");
            } else {
                ReportableException e = new ReportableException(
                        "Please ensure you are logged in before sending messages");
                return new ResponseEntity(e.toJson(), HttpStatus.UNAUTHORIZED);
            }
        }
        String username = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        LOGGER.debug(" ... from " + username);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");

        try {
            String bizKey = msgId + " - " + new Date().getTime();
            Authentication.setAuthenticatedUserId(username);
            vars.put("messageName", msgId);
            vars.put(msgId, jsonBody);
            // TODO deprecate query param?
            vars.put("query", jsonBody);

            vars.put("initiator", username);
            LOGGER.debug(String.format("vars: %1$s", vars));
            ProcessInstance instance = processEngine.getRuntimeService()
                    .startProcessInstanceByMessage(msgId, bizKey, vars);
            org.activiti.spring.rest.model.ProcessInstance pi = org.activiti.spring.rest.model.ProcessInstance
                    .findProcessInstance(instance.getId());
            if (pi.getProcessVariables().containsKey("Location")) {
                String path = pi.getProcessVariables().get("Location")
                        .toString();
                if (!path.startsWith("http")) {
                    path = uriBuilder.path(path).build().toUriString();
                }
                headers.add("Location", path);
            } else {
                RequestMapping a = ProcessInstanceController.class
                        .getAnnotation(RequestMapping.class);
                headers.add("Location",
                        uriBuilder.path(a.value()[0] + "/" + instance.getId())
                                .build().toUriString());
            }
            return new ResponseEntity<String>(headers, HttpStatus.CREATED);
        } catch (CannotCreateTransactionException e) {
            LOGGER.error(e.getClass().getName() + ":" + e.getMessage(), e);
            if (e.getCause() instanceof com.mysql.jdbc.exceptions.jdbc4.CommunicationsException
                    && retry < 3) {
                LOGGER.error("Confirmed timeout exception, retrying...");
                return handleMep(uriBuilder, msgId, jsonBody, vars, ++retry);
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
        } catch (ActivitiException e) {
            LOGGER.error(e.getMessage(), e);
            ReportableException e2 = null;
            ConstraintViolationException cve = isCausedBy(e,
                    ConstraintViolationException.class.getName());
            if (cve != null) {
                e2 = new ReportableException(cve);
                return new ResponseEntity<String>(e2.toJson(), headers,
                        HttpStatus.BAD_REQUEST);
            } else {
                e2 = new ReportableException(e.getClass().getName() + ":"
                        + e.getMessage(), e);
                return new ResponseEntity<String>(e2.toJson(), headers,
                        HttpStatus.SERVICE_UNAVAILABLE);
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            ReportableException e2 = new ReportableException(e.getClass()
                    .getName() + ":" + e.getMessage(), e);
            return new ResponseEntity<String>(e2.toJson(), headers,
                    HttpStatus.BAD_REQUEST);
        } finally {
            Authentication.setAuthenticatedUserId(null);
        }
    }

    private ConstraintViolationException isCausedBy(Throwable e,
            String className) {
        if (e.getCause().getClass().getName().equals(className)) {
            return (ConstraintViolationException) e.getCause();
        } else if (e.getCause() != null) {
            return isCausedBy(e.getCause(), className);
        } else {
            LOGGER.error(e.getMessage(), e);
            return null;
        }
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
            throw new ActivitiRestException("Cannot find location");
        }
    }

}
