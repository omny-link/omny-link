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

import io.onedecision.engine.domain.model.EntityField;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class ExtendedEntityField extends EntityField {

    private String placeholder;

    /**
     * Convert from legacy property name.
     *
     * @deprecated Use setName instead.
     */
    public void setKey(String key) {
        setName(key);
    }

    /**
     * Convert from legacy property name.
     *
     * @deprecated Use getName instead.
     */
    public String getKey() {
        return getName();
    }

}
