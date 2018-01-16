/*******************************************************************************
 *Copyright 2015-2018 Tim Stephenson and contributors
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
package link.omny.acctmgmt.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import lombok.Data;

@Data
public class ServiceLevelConfig {

    protected static final Logger LOGGER = LoggerFactory
            .getLogger(ServiceLevelConfig.class);

    @Value("${omny.contact.inactiveStages:cold,complete,on hold,unqualified,waiting list}" )
    private String[] inactiveStages;

    /**
     * Flag / alert when active records have this much inactivity.
     */
    private Integer inactivityReminderThreshold;

    /**
     * Set records to 'On holder' after this many days.
     */
    private Integer inactivityThreshold;
    private Integer initialResponseThreshold;

    public void set(String name, Object obj) {
        switch (name) {
        case "inactiveStages":
            setInactiveStages(((String) obj).split(","));
            break;
        case "inactivityReminderThreshold":
            setInactivityReminderThreshold((Integer) obj);
            break;
        case "inactivityThreshold":
            setInactivityThreshold((Integer) obj);
            break;
        case "initialResponseThreshold":
            setInitialResponseThreshold((Integer) obj);
            break;
        default:
            LOGGER.error("Unsupported service level: " + name);
        }
    }

}
