/*******************************************************************************
 *Copyright 2018 Tim Stephenson and contributors
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ProcessEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Handle REST requests sending BPMN message events to start or modify process
 * instances.
 * 
 * @author Tim Stephenson
 * 
 */
@Controller
@RequestMapping("/messages")
public class TenantlessMessageController {

    protected static final Logger LOGGER = LoggerFactory
            .getLogger(TenantlessMessageController.class);

    @Autowired
    protected ProcessEngine processEngine;

    @Autowired
    protected MessageController messageController;

    /**
     * Handle a message destined for an intermediate message catch event of an
     * existing process.
     * 
     * <p>
     * No need to supply tenant id because it is implied from the process
     * instance.
     */
    @RequestMapping(method = RequestMethod.POST, value = "/{msgId}/{instanceId}")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    @ResponseBody
    public void handleMessage(@PathVariable("msgId") String msgId,
            @PathVariable("instanceId") String instanceId,
            @RequestBody JsonNode json) {
        long start = System.currentTimeMillis();
        LOGGER.info("handleMessage: " + msgId + ", json:" + json);

        if (LOGGER.isDebugEnabled()) {
            List<String> activeActivityIds = processEngine.getRuntimeService()
                    .getActiveActivityIds(instanceId);
            LOGGER.debug("Active ids: " + activeActivityIds);
        }
        Map<String, Object> vars = new HashMap<String, Object>();
        String s = json.toString();
        vars.put(messageController.getMessageVarName(msgId), s);
        try {
            processEngine.getRuntimeService().messageEventReceived(msgId,
                    instanceId, vars);
        } catch (ActivitiException e) {
            // TODO Activiti seems to have strange msg support
            processEngine.getRuntimeService().signal(instanceId, vars);
        }

        LOGGER.debug(String.format("handleMessage took: %1$s ms",
                (System.currentTimeMillis() - start)));
    }

}
