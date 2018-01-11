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
package link.omny.custmgmt.web;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.annotation.JsonProperty;

import link.omny.custmgmt.repositories.AccountRepository;
import link.omny.custmgmt.repositories.ContactRepository;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * REST web service for fetching contacts aggregated in a way convenient for
 * reporting.
 * 
 * @author Tim Stephenson
 */
@Controller
@RequestMapping(value = "/{tenantId}/funnel")
public class FunnelController {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(FunnelController.class);

    @Autowired
    private AccountRepository accountRepo;

    @Autowired
    private ContactRepository contactRepo;

    /**
     * @return Funnel report based on account stage for the specified tenant.
     */
    @RequestMapping(value = "/accounts", method = RequestMethod.GET)
    public @ResponseBody FunnelReport reportTenantByAccount(
            @PathVariable("tenantId") String tenantId) {
        LOGGER.info(String.format("List account funnel for tenant %1$s", tenantId));

        FunnelReport rpt = new FunnelReport();
        List<Object[]> list = accountRepo
                .findAllForTenantGroupByStage(tenantId);
        LOGGER.debug(String.format("Found %1$s stages", list.size()));

        for (Object[] objects : list) {
            rpt.addStage((String) objects[0], (Number) objects[1]);
        }

        return rpt;
    }
    
    /**
     * @return Funnel report based on contact stage for the specified tenant.
     */
    @RequestMapping(value = "/contacts", method = RequestMethod.GET)
    public @ResponseBody FunnelReport reportTenantByContact(
            @PathVariable("tenantId") String tenantId) {
        LOGGER.info(String.format("List contact funnel for tenant %1$s", tenantId));

        FunnelReport rpt = new FunnelReport();
        List<Object[]> list = contactRepo
                .findAllForTenantGroupByStage(tenantId);
        LOGGER.debug(String.format("Found %1$s stages", list.size()));

        for (Object[] objects : list) {
            rpt.addStage((String) objects[0], (Number) objects[1]);
        }

        return rpt;
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class FunnelReport extends ResourceSupport {
        @JsonProperty
        private Map<String, Number> stages;

        public Map<String, Number> getStages() {
            if (stages == null) {
                stages = new HashMap<String, Number>();
            }
            return stages;
        }

        public void addStage(String stage, Number count) {
            getStages().put(
                    (stage == null || stage.length() == 0)
                    ? "N/A" : stage, count == null ? 0 : count);
        }
      }
}
