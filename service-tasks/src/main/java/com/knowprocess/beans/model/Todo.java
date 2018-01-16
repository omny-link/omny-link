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

public class Todo implements Serializable{

    /**
     * 
     */
    private static final long serialVersionUID = -8855312776383484731L;
    private static final String KEY_ASSIGNEE = "\"assignee\"";
    private static final String KEY_DELAY = "\"delay\"";
    private static final String KEY_BUSINESS_KEY = "\"businessKey\"";
    private String assignee;
    private String delay;
    private String businessKey;

    public String getAssignee() {
        return assignee;
    }

    public String getDelay() {
        return delay;
    }

    public String getBusinessKey() {
        return businessKey;
    }

    public static Todo fromJsonToTodo(String json) {
        Todo todo = new Todo();
        todo.assignee = parseValue(KEY_ASSIGNEE, json);
        todo.delay = parseValue(KEY_DELAY, json);
        todo.businessKey = parseValue(KEY_BUSINESS_KEY, json);
        return todo;
    }


    private static String parseValue(String key, String json) {
        int start = json.indexOf(key) + key.length();
        int valStart = json.indexOf('"', start) + 1;
        return json.substring(valStart, json.indexOf('"', valStart));
    }

    @Override
    public String toString() {
        return "Todo [assignee=" + assignee + ", delay=" + delay
                + ", businessKey=" + businessKey + "]";
    }

}
