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

import java.util.ArrayList;
import java.util.List;

import javax.persistence.ElementCollection;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class TenantTypeaheadControl extends TenantExtension {

    private static final long serialVersionUID = 2970916107407996670L;

    @ElementCollection
    private List<TenantTypeaheadValue> values;

    public TenantTypeaheadControl(String name, String url) {
        super(name, url);
    }

    /**
     * Convert from legacy property name.
     *
     * @deprecated Use setRef instead.
     */
    public void setSelector(String selector) {
        setRef(selector);
    }

    /**
     * Convert from legacy property name.
     *
     * @deprecated Use getRef instead.
     */
    public String getSelector() {
        return getRef();
    }

    public void addValue(TenantTypeaheadValue value) {
        if (values == null) {
            values = new ArrayList<TenantTypeaheadValue>();
        }
        values.add(value);
    }

}
