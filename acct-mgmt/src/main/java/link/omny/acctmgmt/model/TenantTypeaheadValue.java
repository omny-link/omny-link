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
package link.omny.acctmgmt.model;

import java.io.Serializable;

import javax.persistence.Embeddable;
import javax.validation.constraints.NotNull;

import lombok.Data;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
@Embeddable
public class TenantTypeaheadValue implements Serializable {

    private static final long serialVersionUID = -163516717625120278L;

    @NotNull
    @JsonProperty
    private String id;

    @JsonProperty
    // Include override is necessary due to this decision:
    // https://github.com/FasterXML/jackson-databind/issues/849
    @JsonInclude(value = Include.ALWAYS)
    private Integer idx = -1;

    @NotNull
    @JsonProperty
    private String name;

    public TenantTypeaheadValue() {

    }

    public TenantTypeaheadValue(String id) {
        this();
        setId(id);
        setName(id);
    }

    public TenantTypeaheadValue(String id, String name) {
        this();
        setId(id);
        setName(name);
    }
}
