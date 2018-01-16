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
package link.omny.catalog.model;

import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.springframework.data.rest.core.annotation.RestResource;

import link.omny.custmgmt.model.CustomField;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "OL_ORDER_CUSTOM")
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@Getter
@Setter
@NoArgsConstructor
public class CustomOrderField extends CustomField {

    private static final long serialVersionUID = -4795761632689941890L;

    @ManyToOne(optional = false, targetEntity = Order.class)
    @RestResource(rel = "customOrder")
    private Order order;

    public CustomOrderField(String key, String object) {
        super(key, object);
    }

    @Override
    public String toString() {
        return String.format(
                "CustomOrderField [id=%s, name=%s, value=%s, orderId=%s]",
                getId(), getName(), getValue(),
                order == null ? null : order.getId());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        CustomOrderField other = (CustomOrderField) obj;
        if (order == null) {
            if (other.order != null)
                return false;
        } else if (!order.getId().equals(other.order.getId()))
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result
                + ((order == null) ? 0 : order.getId() == null ? 0 : order
                        .getId().hashCode());
        return result;
    }

}
