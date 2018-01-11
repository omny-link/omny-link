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
package com.knowprocess.deployment;

import com.knowprocess.bpmn.model.TaskSubType;

public class ActivityModel {

    public final TaskSubType subType;
    
    public final String name;
    
    public final String doc;
    
    public final String actor;
    
    public ActivityModel(TaskSubType subType, String name, String actor, String doc) {
        this.subType = subType;
        this.name = name;
        this.doc = doc;
        this.actor = actor;
    }

    public ActivityModel(TaskSubType subType, String name, String actor) {
        this.subType = subType;
        this.name = name;
        this.doc = null;
        this.actor = actor;
    }

    public ActivityModel(String name, String actor) {
        this.subType = TaskSubType.USER;
        this.name = name;
        this.doc = null;
        this.actor = actor;
    }

    public ActivityModel(String name, String actor, String doc) {
        this.subType = TaskSubType.USER;
        this.name = name;
        this.doc = doc;
        this.actor = actor;
    }
}
