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
package com.knowprocess.bpm.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import lombok.Data;

import org.activiti.engine.ProcessEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Data
@Component
public class Deployment implements Serializable {

    private static final long serialVersionUID = 7941240932869015488L;

    protected static final Logger LOGGER = LoggerFactory
            .getLogger(Deployment.class);

    private static ProcessEngine processEngine;

    /**
     */
    @Id
    @Column(unique = true)
    private String id;

    /**
     */
    private String name;

    /**
     */
    @Temporal(TemporalType.TIMESTAMP)
    @DateTimeFormat(style = "M-")
    private Date deploymentTime;

    /**
     */
    private String category;

    /**
     */
    private String url;

    public Deployment() {
        super();
    }

    public Deployment(org.activiti.engine.repository.Deployment d) {
        this();
        setId(d.getId());
        setName(d.getName());
        setDeploymentTime(d.getDeploymentTime());
        setCategory(d.getCategory());
        // setUrl(d.getUrl());
    }

    // Autowiring static fields is obviously dangerous, but should be ok in this
    // case as PE is thread safe.
    @Autowired(required = true)
    public void setProcessEngine(ProcessEngine pe) {
        Deployment.processEngine = pe;
    }

    @Transactional
    public void remove() {
        LOGGER.warn(String.format("Deleting deployment %1$s", getId()));
        processEngine.getRepositoryService().deleteDeployment(getId(), true);

        long count = processEngine.getRepositoryService()
                .createDeploymentQuery().deploymentId(getId()).count();
        if (count > 0) {
            throw new RuntimeException(String.format(
                    "Failed to delete deployment with id %1$s", getId()));
        }
    }

    public static long countDeployments() {
        return processEngine.getRepositoryService().createDeploymentQuery()
                .count();
    }

    public static List<Deployment> findAllDeployments() {
        return wrap(processEngine.getRepositoryService()
                .createDeploymentQuery().list());
    }

    public static List<Deployment> findAllDeployments(String tenantId) {
        return wrap(processEngine.getRepositoryService()
                .createDeploymentQuery().deploymentTenantId(tenantId).list());
    }

    public static Deployment findDeployment(String id) {
        return wrap(
                processEngine.getRepositoryService().createDeploymentQuery()
                        .deploymentId(id).list()).get(0);
    }

    public static List<Deployment> findDeploymentEntries(int firstResult,
            int maxResults) {
        return wrap(processEngine.getRepositoryService()
                .createDeploymentQuery().listPage(firstResult, maxResults));
    }

    public static List<Deployment> findDeploymentEntries(int firstResult,
            int maxResults, String sortFieldName, String sortOrder) {
        // TODO honour sort order
        return wrap(processEngine.getRepositoryService()
                .createDeploymentQuery().listPage(firstResult, maxResults));
    }

    public static List<Deployment> findAllDeployments(String sortFieldName,
            String sortOrder) {
        System.out.println("pe: " + processEngine);
        // TODO honour sort order
        return wrap(processEngine.getRepositoryService()
                .createDeploymentQuery().list());
    }

    private static List<Deployment> wrap(
            final List<org.activiti.engine.repository.Deployment> list) {
        ArrayList<Deployment> list2 = new ArrayList<Deployment>();
        for (org.activiti.engine.repository.Deployment instance : list) {
            list2.add(new Deployment(instance));
        }
        return list2;
    }

    // public static String toJsonArray(Collection<Deployment> collection) {
    // String[] fields = { "assignee", "createTime", "id", "name", "owner",
    // "parentDeploymentId", "priority", "processDefinitionId",
    // "suspended", "repositoryDefinitionKey" };
    // return toJsonArray(collection, fields);
    // }
    //
    // public static String toJsonArray(Collection<Deployment> collection,
    // String[] fields) {
    // System.out.println("toJsonArray....");
    // return new JSONSerializer().exclude("*.class")
    // .exclude("*.processEngine").include(fields)
    // .serialize(collection);
    // }
}
