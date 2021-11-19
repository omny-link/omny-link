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
package link.omny.supportservices.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@MappedSuperclass
@Data
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class CustomField implements Serializable {
    private static final long serialVersionUID = 7496048564725313117L;
    protected static final Logger LOGGER = LoggerFactory
            .getLogger(CustomField.class);

    /**
     */
    @NotNull
    @JsonProperty
    @Column(nullable = false)
    private String name;

    /**
     */
    @JsonProperty
    @Size(max = 1000)
    private String value;

    public CustomField(String key, Object value2) {
        this.name = key;
        this.value = value2 == null ? null : value2.toString();
    }
}
