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
package com.knowprocess.beans.model;

import java.io.Serializable;
import java.util.Date;

public class LeadActivity implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -4058597813090948518L;

    private String id;
    private String description;
    private Date dateOfActivity;

    private ActionType actionType;

    public LeadActivity() {
        super();
        this.dateOfActivity = new Date();
    }

    public LeadActivity(String description) {
        this();
        this.description = description;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getDateOfActivity() {
        return dateOfActivity;
    }

    public void setDateOfActivity(Date dateOfActivity) {
        this.dateOfActivity = dateOfActivity;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String desc) {
        this.description = desc;
    }

    public void setActionType(ActionType type) {
        this.actionType = type;
    }

    public ActionType getActionType() {
        return this.actionType;
    }

}
