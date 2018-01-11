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

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class TenantToolbarEntry extends TenantExtension {

    private static final long serialVersionUID = -8327456294072464761L;

    public TenantToolbarEntry(String name, String url, String icon,
            String description) {
        super(name, url, description);
        setIcon(icon);
    }

    public String getUrl() {
        if (super.getUrl() != null && !super.getUrl().startsWith("/")) {
            setUrl("/" + super.getUrl());
        }
        return super.getUrl();
    }

    /**
     * Convert from legacy property name.
     * 
     * @deprecated Use setName instead.
     */
    public void setTitle(String selector) {
        setName(selector);
    }

    /**
     * Convert from legacy property name.
     * 
     * @deprecated Use getName instead.
     */
    public String getTitle() {
        return getName();
    }

    /**
     * Convert from legacy property name.
     * 
     * @deprecated Use setIcon instead.
     */
    public void setClasses(String classes) {
        setIcon(classes);
    }

    /**
     * Convert from legacy property name.
     * 
     * @deprecated Use getIcon instead.
     */
    public String getClasses() {
        if (getIcon().indexOf("glyphicon ") == -1) {
            return "glyphicon " + getIcon();
        } else {
        return getIcon();
        }
    }
}
