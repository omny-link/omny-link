/*******************************************************************************
 *Copyright 2011-2018 Tim Stephenson and contributors
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
package com.knowprocess.bpm.web;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.transform.TransformerConfigurationException;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ProcessInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.JsonNode;
import com.knowprocess.bpm.api.ReportableException;
import com.knowprocess.bpm.model.ProcessDefinition;
import com.knowprocess.xslt.TransformTask;

/**
 * Handle REST requests sending BPMN message events to start or modify process
 * instances.
 * 
 * @author Tim Stephenson
 * 
 */
@Controller
@RequestMapping("/{tenantId}/messages")
public class Message2Controller {

    protected static final Logger LOGGER = LoggerFactory
            .getLogger(Message2Controller.class);

    private static final String RENDERER_RESOURCES = "/static/xslt/bpmn2msgapi.xslt";

    @Autowired
    protected ProcessEngine processEngine;

    @Autowired
    protected MessageController messageController;

    @Autowired
    protected TenantlessMessageController tenantlessMessageController;

    private TransformTask renderer;

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @RequestMapping(method = RequestMethod.POST, value = "/{msgId}", headers = {
            "Accept=application/json", "Content-Type=application/json" })
    @ResponseBody
    public ResponseEntity<String> handleMessageStart(
            @PathVariable("tenantId") String tenantId,
            @PathVariable("msgId") String msgId,
            @RequestParam(required = false, value = "businessDescription") String bizDesc,
            @RequestBody String json) {

        UriComponentsBuilder uriBuilder = MvcUriComponentsBuilder
                .fromController(getClass());

        Map<String, Object> vars = new HashMap<String, Object>();
        HttpHeaders headers = null;
        try {
            ResponseEntity<String> response = messageController.handleMep(
                    uriBuilder, tenantId, msgId, bizDesc, json, vars, 0);
            if (response.getStatusCode() != HttpStatus.CREATED) {
                return response;
            }
            headers = response.getHeaders();
            Object o = com.knowprocess.bpm.model.ProcessInstance
                    .findProcessInstance((String) vars.get("piid"))
                    .getProcessVariables()
                    .get(messageController.getMessageVarName(msgId));
            String msg = o.toString();
            LOGGER.debug("msg: " + msg);
            return new ResponseEntity(msg, headers, HttpStatus.CREATED);
        } catch (ActivitiException e) {
            throw e;
        } catch (Exception e) {
            ReportableException e2 = new ReportableException(e.getClass()
                    .getName() + ":" + e.getMessage(), e);
            return new ResponseEntity(e2.toJson(), headers,
                    HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Handle a message destined for an intermediate message catch event of an
     * existing process.
     */
    @RequestMapping(method = RequestMethod.POST, value = "/{msgId}/{instanceId}")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    @ResponseBody
    public void handleMessage(@PathVariable("tenantId") String tenantId,
            @PathVariable("msgId") String msgId,
            @PathVariable("instanceId") String instanceId,
            @RequestBody JsonNode json) {

        tenantlessMessageController.handleMessage(msgId, instanceId,
                json);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/{msgId}", headers = "Accept=application/json", produces = "application/json")
    public @ResponseBody List<com.knowprocess.bpm.model.Execution> getEventListeners(
            @PathVariable("tenantId") String tenantId,
            @PathVariable("msgId") String msgId) {
        LOGGER.info(String.format("getEventListeners(%1$s, %2$s)", tenantId,
                msgId));

        List<Execution> list = processEngine.getRuntimeService()
                .createExecutionQuery().messageEventSubscriptionName(msgId)
                .list();
        if (list.size() == 0) {
            throw new ActivitiObjectNotFoundException(String.format(
                    "No message subscriptions to %1$s for %2$s", msgId,
                    tenantId));
        } else {
            LOGGER.debug(String.format("... found %1$d", list.size()));
        }
        // TODO add createProcessInstanceQuery().executionIds to mirror
        // processInstanceIds()
        List<com.knowprocess.bpm.model.Execution> piList = new ArrayList<com.knowprocess.bpm.model.Execution>();
        for (Execution execution : list) {
            com.knowprocess.bpm.model.Execution exe = new com.knowprocess.bpm.model.Execution(
                    execution);
            ProcessInstance pi = processEngine.getRuntimeService()
                    .createProcessInstanceQuery()
                    .processInstanceId(execution.getProcessInstanceId())
                    .singleResult();
            exe.setBusinessKey(pi.getBusinessKey());
            piList.add(exe);
        }
        LOGGER.info(String.format(" found %1$s, instances", piList.size()));
        return piList;
        // return findNativelyProcessInstancesWhereExecutionIn(list);
    }

    // TODO Unreliable?!
    // private List<com.knowprocess.bpm.model.ProcessInstance>
    // findNativelyProcessInstancesWhereExecutionIn(
    // List<Execution> list) {
    // StringBuffer sb = new StringBuffer();
    // for (Execution execution : list) {
    // sb.append(execution.getId()).append(",");
    // }
    // sb = sb.deleteCharAt(sb.length() - 1);
    // return com.knowprocess.bpm.model.ProcessInstance.wrap(processEngine
    // .getRuntimeService().createNativeExecutionQuery()
    // .sql("SELECT * FROM ACT_RU_EXECUTION WHERE id_ IN (#{ids})")
    // .parameter("ids", sb.toString()).list());
    // }

    @RequestMapping(method = RequestMethod.GET, value = "/{msgId}.html", headers = "Accept=text/html", produces = "text/html")
    public @ResponseBody byte[] showMsgApiDoc(
            @PathVariable("tenantId") String tenantId,
            @PathVariable("msgId") String msgId) throws IOException {
        LOGGER.info(String.format("API doc for msg %1$s", msgId));

        org.activiti.engine.repository.ProcessDefinition pd = processEngine
                .getRepositoryService().createProcessDefinitionQuery()
                .messageEventSubscriptionName(msgId).singleResult();
        if (pd == null) {
            throw new ActivitiObjectNotFoundException(
                    org.activiti.engine.repository.ProcessDefinition.class);
        }
        String bpmn = ProcessDefinition.findProcessDefinitionAsBpmn(pd.getId());

        Map<String, String> params = new HashMap<String, String>();
        params.put("tenantId", tenantId);
        return getProcessRenderer().transform(bpmn, params).getBytes();
    }

    private TransformTask getProcessRenderer() {
        // if (renderer == null) {
        renderer = new TransformTask();
        try {
            renderer.setXsltResources(RENDERER_RESOURCES);
        } catch (TransformerConfigurationException e) {
            LOGGER.error(String.format("Unable to location API renderer: %1$s",
                    RENDERER_RESOURCES), e);
        }
        // }
        return renderer;
    }
}
