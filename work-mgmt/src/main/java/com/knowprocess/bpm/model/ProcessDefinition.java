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
package com.knowprocess.bpm.model;

import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import javax.persistence.Id;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.form.StartFormData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
@Component
public class ProcessDefinition implements Serializable {
    private static final long serialVersionUID = -2657367116355427744L;

    protected static final Logger LOGGER = LoggerFactory
            .getLogger(ProcessDefinition.class);

    @SuppressWarnings("resource")
    public static String readFromClasspath(String resourceName) {
        InputStream is = null;
        try {
            is = ProcessDefinition.class.getResourceAsStream(resourceName);
            return new Scanner(is).useDelimiter("\\A").next();
        } catch (Exception e) {
            throw e;
        } finally {
            try {
                is.close();
            } catch (Exception e) {
                ;
            }
        }
    }

    private static ProcessEngine processEngine;

    @Id
    private String id;

    private String name;

    private String category;

    private String description;

    private Integer version;

    private String resourceName;

    private Integer deploymentId;

    private String diagramResourceName;

    private String key;

    private String formKey;

    private boolean suspended;

    private Deployment deployment;

    private String md5Hash;

    private List<String> diagramIds;
    
    private List<String> messageNames;

    private String tenantId;

    private List<FormProperty> formProperties;

    @JsonProperty
    private transient String processText;

    @JsonProperty
    private transient String bpmn;

    @JsonProperty
    private transient String svgImage;

    @JsonProperty
    private transient Long instanceCount;

    public ProcessDefinition() {
        super();
        formProperties = new ArrayList<FormProperty>();
    }

    public ProcessDefinition(org.activiti.engine.repository.ProcessDefinition pd) {
        this();
        setId(pd.getId());
        setKey(pd.getKey());
        setName(pd.getName());
        setCategory(pd.getCategory());
        if (pd.getDeploymentId() != null) {
            setDeploymentId(Integer.valueOf(pd.getDeploymentId()));
        }
        setDescription(pd.getDescription());
        setVersion(Integer.valueOf(pd.getVersion()));
        setResourceName(pd.getResourceName());
        setDiagramResourceName(pd.getDiagramResourceName());
        setSuspended(pd.isSuspended());
        setTenantId(pd.getTenantId());
    }
    
    public ProcessDefinition(ProcessModel pd) {
        this((org.activiti.engine.repository.ProcessDefinition) pd);
        setBpmn(pd.getBpmnString());
    }

    // Autowiring static fields is obviously dangerous, but should be ok in this
    // case as PE is thread safe.
    @Autowired(required = true)
    public void setProcessEngine(ProcessEngine pe) {
        ProcessDefinition.processEngine = pe;
    }

    public static long countProcessDefinitions() {
        return processEngine.getRepositoryService()
                .createProcessDefinitionQuery().count();
    }

    public static List<ProcessDefinition> findAllProcessDefinitions() {
        return wrap(processEngine.getRepositoryService()
                .createProcessDefinitionQuery().list());
    }

    public static List<ProcessDefinition> findAllProcessDefinitions(
            String tenantId) {
        return wrap(processEngine.getRepositoryService()
                .createProcessDefinitionQuery()
                .processDefinitionTenantId(tenantId).list());
    }

    public static ProcessDefinition findProcessDefinition(String id) {
        ProcessDefinition pd = new ProcessDefinition(processEngine
                .getRepositoryService().createProcessDefinitionQuery()
                .processDefinitionId(id).singleResult());
        try {
            StartFormData formData = processEngine.getFormService()
                    .getStartFormData(id);
            pd.setFormKey(formData.getFormKey());
            for (org.activiti.engine.form.FormProperty prop : formData
                    .getFormProperties()) {
                pd.getFormProperties().add(new FormProperty(prop));
            }
        } catch (ActivitiException e) {
            LOGGER.warn("No Activiti form extensions: {}", e.getMessage());
        }
        return pd;
    }

    @SuppressWarnings("resource")
    public static String findProcessDefinitionAsBpmn(String id) {
        InputStream is = null;
        try {
            is = processEngine.getRepositoryService().getProcessModel(id);
            return new Scanner(is).useDelimiter("\\A").next();
        } finally {
            try {
                is.close();
            } catch (Exception e) {
                ;
            }
        }
    }

    public static List<ProcessDefinition> findProcessDefinitionEntries(
            int firstResult, int maxResults) {
        return wrap(processEngine.getRepositoryService()
                .createProcessDefinitionQuery()
                .listPage(firstResult, maxResults));
    }

    public static List<ProcessDefinition> findProcessDefinitionEntries(
            int firstResult, int maxResults, String sortFieldName,
            String sortOrder) {
        // TODO honour sort order
        return wrap(processEngine.getRepositoryService()
                .createProcessDefinitionQuery().orderByProcessDefinitionName()
                .asc().listPage(firstResult, maxResults));
    }

    public static List<ProcessDefinition> findAllProcessDefinitions(
            String sortFieldName, String sortOrder) {
        System.out.println("pe: " + processEngine);
        // TODO honour sort order
        return wrap(processEngine.getRepositoryService()
                .createProcessDefinitionQuery().orderByProcessDefinitionName()
                .asc().list());
    }

    private static List<ProcessDefinition> wrap(
            final List<org.activiti.engine.repository.ProcessDefinition> list) {
        ArrayList<ProcessDefinition> list2 = new ArrayList<ProcessDefinition>();
        for (org.activiti.engine.repository.ProcessDefinition processDefinition : list) {
            list2.add(new ProcessDefinition(processDefinition));
        }
        return list2;
    }

    public List<String> getMessageNames() {
        if (messageNames == null) {
            messageNames = new ArrayList<String>();
        }
        return messageNames;
    }

    public void addMessageName(String name) {
        getMessageNames().add(name);
    }

    // public String toJson() {
    // return toJson(JSON_FIELDS);
    // }
    //
    // public String toJson(String[] fields) {
    // return new JSONSerializer().include(fields).exclude("*.class")
    // .exclude("*.processEngine").serialize(this);
    // }
    //
    // public static String toJsonArray(Collection<ProcessDefinition>
    // collection) {
    // return toJsonArray(collection, JSON_FIELDS);
    // }
    //
    // public static String toJsonArray(Collection<ProcessDefinition>
    // collection,
    // String[] fields) {
    // System.out.println("toJsonArray....");
    // return new JSONSerializer().exclude("*.class")
    // .exclude("*.processEngine").include(fields)
    // .serialize(collection);
    // }
}
