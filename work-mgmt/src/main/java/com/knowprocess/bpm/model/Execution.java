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

import javax.persistence.Id;

import lombok.Data;

import org.springframework.stereotype.Component;

@Data
@Component
public class Execution implements Serializable {

    private static final long serialVersionUID = 7684119384630391017L;

    /**
     */
    private String activityId;

    /**
     */
    @Id
    private String id;

    /**
     */
    private String parentId;

    /**
     */
    private String processInstanceId;

	private String businessKey;

    /**
     */
    private Boolean ended;

    private String tenantId;

    public Execution() {
        ;
    }

    public Execution(org.activiti.engine.runtime.Execution exe) {
        this();
        setActivityId(exe.getActivityId());
        setId(exe.getId());
        setParentId(exe.getParentId());
        setProcessInstanceId(exe.getProcessInstanceId());
        setEnded(exe.isEnded());
    }

    // public static String toJsonArray(Collection<? extends Execution>
    // collection) {
    // String[] fields = { "businessKey", "processDefinitionId", "suspended" };
    // return toJsonArray(collection, fields);
    // }
    //
    // public static String toJsonArray(
    // Collection<? extends Execution> collection,
    // String[] fields) {
    // System.out.println("toJsonArray....");
    // return new JSONSerializer().exclude("*.class")
    // .exclude("*.processEngine").include(fields)
    // .serialize(collection);
    // }
}
