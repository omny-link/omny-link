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
package link.omny.custmgmt.model;

import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "OL_CONTACT_CUSTOM")
@Data
@EqualsAndHashCode(callSuper = true)
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@NoArgsConstructor
public class CustomContactField extends CustomField {

    private static final long serialVersionUID = -7683896817261973079L;

    public CustomContactField(String key, Object value) {
        super(key, value);
    }

    @ManyToOne(optional = false, targetEntity = Contact.class)
    private Contact contact;

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        CustomContactField other = (CustomContactField) obj;
        if (contact == null) {
            if (other.contact != null)
                return false;
        } else if (contact.getId() == null) {
            if (other.contact.getId() != null)
                return false;
        } else if (!contact.getId().equals(other.contact.getId()))
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result
                + ((contact == null || contact.getId() == null) ? 0 : contact
                        .getId().hashCode());
        return result;
    }

    @Override
    public String toString() {
        return String.format(
                "CustomContactField [contact=%s, id=%s, name=%s, value=%s]",
                (contact == null || contact.getId() == null) ? null : contact
                        .getId(), getId(), getName(), getValue());
    }

}
