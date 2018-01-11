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
package com.knowprocess.bpmn;

public class BpmnRestHelper {

    public static final String ID = "bpmnRest";

    public String tenantUri(String tenant, String uri) {
        int startPath = uri.indexOf("/", uri.indexOf("//")+2);
        return String.format("%1$s/%2$s%3$s", uri.substring(0, startPath), tenant, uri.substring(startPath));
    }

    public Long uriToLocalId(String uri) {
        if (uri == null) {
            throw new IllegalArgumentException(String.format("URI %1$s is null", uri));
        }

        return Long.parseLong(uri.substring(uri.lastIndexOf('/')+1));
    }

}
