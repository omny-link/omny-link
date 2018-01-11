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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.Size;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a single tenant.
 *
 * @author Tim Stephenson
 */
@Data
@Entity
@Table(name = "OL_TENANT")
@NoArgsConstructor
@AllArgsConstructor
public class Tenant implements Serializable {

    private static final long serialVersionUID = -4729049290436298887L;

    protected static final Logger LOGGER = LoggerFactory
            .getLogger(Tenant.class);

    @Id
    protected String id;

    @JsonProperty
    protected String name;

    @JsonProperty
    @Column(name = "remote_url")
    protected String remoteUrl;

    @JsonProperty
    @Size(max = 20)
    private String status;

    @Transient
    private TenantConfig config;

    public Tenant(String id, String configUrl) {
        this();
        setId(id);
        setRemoteUrl(configUrl);
    }

    public String getName() {
        if (name == null) {
            return id;
        } else {
            return name;
        }
    }

}
