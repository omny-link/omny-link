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

import java.io.Serializable;

import javax.persistence.Id;

import lombok.Data;

import org.springframework.stereotype.Component;

@Data
@Component
public class UserInfo implements Serializable {

    private static final long serialVersionUID = -4782038617402728868L;

    /**
     */
    @Id
    private String id;

    /**
     */
    private String key;

    /**
     */
    private String value;

    public UserInfo() {
        super();
    }

    public UserInfo(String key, String value) {
        this();
        setKey(key);
        setValue(value);
    }

}
