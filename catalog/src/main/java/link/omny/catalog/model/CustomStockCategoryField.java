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
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.springframework.data.rest.core.annotation.RestResource;

import link.omny.custmgmt.model.CustomField;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "OL_STOCK_CAT_CUSTOM")
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@Getter
@Setter
@NoArgsConstructor
public class CustomStockCategoryField extends CustomField {

    private static final long serialVersionUID = -4795761632689941890L;

    @ManyToOne(optional = false, targetEntity = StockCategory.class)
    @RestResource(rel = "customStockCategory")
    @JoinColumn(name = "stock_cat_id")
    private StockCategory stockCategory;

    public CustomStockCategoryField(String key, String object) {
        super(key, object);
    }

    @Override
    public String toString() {
        return String
                .format("CustomStockCategoryField [id=%s, name=%s, value=%s, stockCategoryId=%s]",
                        getId(), getName(), getValue(),
                        stockCategory == null ? null : stockCategory.getId());
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime
                * result
                + ((stockCategory == null) ? 0
                        : stockCategory.getId() == null ? 0 : stockCategory
                                .getId()
                        .hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        CustomStockCategoryField other = (CustomStockCategoryField) obj;
        if (stockCategory == null) {
            if (other.stockCategory != null)
                return false;
        } else if (!stockCategory.getId().equals(other.stockCategory.getId()))
            return false;
        return true;
    }
}
