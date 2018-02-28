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
package link.omny.supportservices.exceptions;

/**
 * Business exception suitable for catching as BPMN error.
 *
 * @author Tim Stephenson
 */
public class BusinessEntityNotFoundException extends RuntimeException {

    private String entity;
    private String id;

    public BusinessEntityNotFoundException(String entity, String id) {
        super();
        this.entity = entity;
        this.id = id;
    }

    private static final long serialVersionUID = 7932887356223861356L;

    public String getEntity() {
        return entity;
    }

    public String getId() {
        return id;
    }

}
