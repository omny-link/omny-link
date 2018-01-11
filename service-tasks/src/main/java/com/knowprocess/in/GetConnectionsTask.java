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
package com.knowprocess.in;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.json.JsonWriter;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;

import com.google.code.linkedinapi.schema.Connections;
import com.google.code.linkedinapi.schema.Person;
import com.knowprocess.beans.ConversionTask;
import com.knowprocess.in.filters.IdListFilter;
import com.knowprocess.in.filters.MatchAllFilter;
import com.knowprocess.in.filters.NameFilter;
import com.knowprocess.sugarcrm.api.SugarLead;

public class GetConnectionsTask extends LinkedInTask implements JavaDelegate {

    public List<String> getIdsOfConnections(String username, PersonFilter filter) {
        return getIdsOfConnections(getConnections(username), filter);
    }

    protected List<String> getIdsOfConnections(Connections connections,
            PersonFilter filter) {
        List<String> list = new ArrayList<String>();

        int filterCount = 0;
        for (Person person : connections.getPersonList()) {
            if (filter.match(person)) {
                list.add(person.getId());
                filterCount++;
            }
        }
        LOGGER.debug(String.format("Filter %1$s matched %2$s connections",
                filter, filterCount));

        return list;
    }

    public String getConnectionsAsJson(String username, PersonFilter filter) {
        return getConnectionsAsJson(getConnections(username), filter);
    }

    protected String getConnectionsAsJson(Connections connections,
            PersonFilter filter) {
        JsonArrayBuilder connectionBuilder = Json.createArrayBuilder();
        JsonObjectBuilder personBuilder = Json.createObjectBuilder();

        for (Person person : connections.getPersonList()) {
            if (filter.match(person)) {
                personBuilder.add("id", person.getId());
                personBuilder.add("firstName", person.getFirstName());
                personBuilder.add("lastName", person.getLastName());
                if (person.getCurrentStatus() != null) {
                    personBuilder.add("currentStatus",
                            person.getCurrentStatus());
                }
                if (person.getHeadline() != null) {
                    personBuilder.add("headline", person.getHeadline());
                }
                if (person.getIndustry() != null) {
                    personBuilder.add("industry", person.getIndustry());
                }
                if (person.getInterests() != null) {
                    personBuilder.add("interests", person.getInterests());
                }
                if (person.getPictureUrl() != null) {
                    personBuilder.add("pictureUrl", person.getPictureUrl());
                }
                if (person.getSpecialties() != null) {
                    personBuilder.add("specialities", person.getSpecialties());
                }
                if (person.getSummary() != null) {
                    personBuilder.add("summary", person.getSummary());
                }
                connectionBuilder.add(personBuilder.build());
            }
        }

        JsonArray empJsonObject = connectionBuilder.build();
        Writer sw = new StringWriter();
        JsonWriter jsonWriter = Json.createWriter(sw);
        jsonWriter.writeArray(empJsonObject);
        jsonWriter.close();
        try {
            sw.flush();
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(String.format("JSON constructed is: %1$s chars long",
                    sw.toString().length()));
            LOGGER.debug(String.format("... %1$s", sw.toString()));
        }
        return sw.toString();
    }

    protected Connections getConnections(String username) {
        Connections connections = getClient(username)
                .getConnectionsForCurrentUser();
        LOGGER.debug(String.format("Found %1$s connections.",
                connections.getTotal()));
        return connections;
    }

    protected PersonFilter getFilter(String sFilter) {
        PersonFilter filter;
        if (sFilter == null || sFilter.trim().length() == 0) {
            filter = new MatchAllFilter();
        } else if (sFilter.startsWith(LinkedInTask.ID_LIST_PREFIX)) {
            filter = new IdListFilter(sFilter.substring(
                    LinkedInTask.ID_LIST_PREFIX.length()).trim());
        } else {
            filter = new NameFilter(sFilter);
        }
        return filter;
    }

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        String username = (String) execution.getVariable("initiator");
        LOGGER.debug(String.format("Fetching LinkedIn connections of %1$s",
                username));

        setIdentityService(execution.getEngineServices().getIdentityService());

        PersonFilter filter = getFilter((String) execution
                .getVariable(LinkedInTask.FILTER_KEY));

        Connections connections = getConnections(username);
        execution.setVariable(LinkedInTask.ID_LIST_KEY,
                getIdsOfConnections(connections, filter));
        execution.setVariable(LinkedInTask.CONNECTION_LIST_KEY,
                getConnectionsAsJson(connections, filter));

        // TODO This is an expedient hack, need to remove!
        List<SugarLead> sugarLeadList = new ArrayList<SugarLead>();
        ConversionTask cs = new ConversionTask();
        for (Person p : connections.getPersonList()) {
            if (filter.match(p)) {
                try {
                    sugarLeadList.add((SugarLead) cs
                            .convert(p, SugarLead.class));
                } catch (Exception e) {
                    LOGGER.error(e.getMessage(), e);
                }
            }
        }
        execution.setVariable("sugarLeadList", sugarLeadList);

    }

}
