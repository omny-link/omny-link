/*******************************************************************************
 * Copyright 2015-2025 Tim Stephenson and contributors
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
package link.omny.supportservices.model;

import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.xml.bind.annotation.XmlElement;

import org.springframework.hateoas.Link;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;

import link.omny.supportservices.views.NumberSequenceViews;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Table(name = "OL_SEQ")
@NoArgsConstructor
public class NumberSequence {

    @JsonProperty
    @Id
    @GeneratedValue
    private Long id;

    @JsonView(NumberSequenceViews.Summary.class)
    @JsonProperty
    @NotNull
    @Size(max = 60)
    @Column(name = "name")
    private String name;

    @JsonView(NumberSequenceViews.Summary.class)
    @JsonProperty
    @NotNull
    @Size(max = 60)
    @Column(name = "tenant_id")
    private String tenantId;

    @JsonView(NumberSequenceViews.Summary.class)
    @JsonProperty
    @NotNull
    @Column(name = "last")
    private Long lastUsed;

    @Transient
    @XmlElement(name = "link", namespace = Link.ATOM_NAMESPACE)
    @JsonProperty("links")
    @JsonView({ NumberSequenceViews.Summary.class })
    private List<Link> links;

    public NumberSequence(String name, String tenantId) {
        this.name = name;
        this.tenantId = tenantId;
        this.lastUsed = 0l;
    }

    public Long getNext() {
        return getLastUsed() + 1l;
    }

    public void increment() {
        this.lastUsed = this.getLastUsed() + 1l;        
    }
    
}
