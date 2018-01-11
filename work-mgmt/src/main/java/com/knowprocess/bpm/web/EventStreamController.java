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

import java.util.ArrayList;
import java.util.List;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.history.HistoricActivityInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.knowprocess.bpm.model.HistoricDetail;

@Controller
@RequestMapping("/{tenantId}/events")
public class EventStreamController {

    protected static final Logger LOGGER = LoggerFactory
            .getLogger(EventStreamController.class);

    @Autowired(required = true)
    ProcessEngine processEngine;

    @RequestMapping(value = "", method = RequestMethod.GET)
    @ResponseBody
    public ArrayList<HistoricDetail> list(
            @PathVariable("tenantId") String tenantId,
            @RequestParam(required = false, value = "type") String type,
            @RequestParam(required = false, value = "startIdx", defaultValue = "0") Integer startIdx,
            @RequestParam(required = false, value = "limit") Integer limit) {
        LOGGER.info(String.format("Listing events %1$s", tenantId));

        if (limit == null) {
            return wrap(processEngine.getHistoryService()
                    .createHistoricActivityInstanceQuery()
                    .activityTenantId(tenantId)
                    .orderByHistoricActivityInstanceEndTime().desc().list());
        } else {
            return wrap(processEngine.getHistoryService()
                    .createHistoricActivityInstanceQuery()
                    .activityTenantId(tenantId)
                    .orderByHistoricActivityInstanceEndTime().desc()
                    .listPage(startIdx, limit));
        }
    }

    protected ArrayList<HistoricDetail> wrap(List<HistoricActivityInstance> list) {
        ArrayList<HistoricDetail> list2 = new ArrayList<HistoricDetail>();
        for (HistoricActivityInstance activity : list) {
            list2.add(new HistoricDetail(activity));
        }
        return list2;
    }
}
