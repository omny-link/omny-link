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
package link.omny.acctmgmt.model;

import java.io.Serializable;

import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import lombok.Data;
import lombok.NoArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonProperty;

@Data
@NoArgsConstructor
public class TenantExtension implements Serializable {

    private static final long serialVersionUID = 4993644181746182288L;

    protected static final Logger LOGGER = LoggerFactory
            .getLogger(TenantExtension.class);

    @NotNull
    @JsonProperty
    private String name;

    @JsonProperty
    private String icon;

    @JsonProperty
    private String role;

    @JsonProperty
    private String feature;

    @JsonProperty
    private String description;

    @JsonProperty
    private String url;

    @JsonProperty
    private String ref;

    @Transient
    @JsonProperty
    private boolean valid;

    @Transient
    @JsonProperty
    private String status;

    public TenantExtension(String name, String url) {
        super();
        this.name = name;
        this.url = url;
    }

    public TenantExtension(String name, String url, String description) {
        this(name, url);
        setIcon(icon);
        setDescription(description);
    }

    public String getStatus() {
        if (status == null) {
            return Boolean.toString(valid);
        } else {
            return status;
        }
    }

    @Override
    public String toString() {
        return String
                .format("TenantExtension [name=%s, icon=%s, role=%s, description=%s, url=%s, ref=%s]",
                        name, icon, role, description, url, ref);
    }

}
