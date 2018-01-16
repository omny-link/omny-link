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

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class TenantAction extends TenantExtension {

    private static final long serialVersionUID = -1134185133273685783L;

    public TenantAction(String name, String url) {
        super(name, url);
    }

    /**
     * Convert from legacy property name.
     * 
     * @deprecated Use setName instead.
     */
    public void setLabel(String label) {
        setName(label);
    }

    /**
     * Convert from legacy property name.
     * 
     * @deprecated Use getName instead.
     */
    public String getLabel() {
        return getName();
    }

    /**
     * Convert from legacy property name.
     * 
     * @deprecated Use setRef instead.
     */
    public void setKey(String key) {
        setName(key);
    }

    /**
     * Convert from legacy property name.
     * 
     * @deprecated Use getRef instead.
     */
    public String getKey() {
        return getRef();
    }

    /**
     * Convert from legacy property name.
     * 
     * @deprecated Use setUrl instead.
     */
    public void setForm(String form) {
        setUrl(form);
    }

    /**
     * Convert from legacy property name.
     * 
     * @deprecated Use getUrl instead.
     */
    public String getForm() {
        return getUrl();
    }
}
