/*******************************************************************************
 * Copyright 2015-2022 Tim Stephenson and contributors
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

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

import org.springframework.data.rest.core.annotation.RestResource;

import com.fasterxml.jackson.annotation.JsonProperty;

import link.omny.supportservices.model.CustomField;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "OL_STOCK_ITEM_CUSTOM")
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@Getter
@Setter
@NoArgsConstructor
public class CustomStockItemField extends CustomField {

    private static final long serialVersionUID = -4795761632689941890L;

    @Id
    @Column(name = "id")
    @SequenceGenerator(name = "stockItemCustomIdSeq", sequenceName = "ol_stock_item_custom_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "stockItemCustomIdSeq")
    @JsonProperty
    private Long id;

    @ManyToOne(optional = false, targetEntity = StockItem.class)
    @RestResource(rel = "customStockItem")
    @JoinColumn(name = "stock_item_id")
    private StockItem stockItem;

    public CustomStockItemField(String key, String object) {
        super(key, object);
    }

    @Override
    public String toString() {
        return String
                .format("CustomStockItemField [id=%s, name=%s, value=%s, stockItemId=%s]",
                        getId(), getName(), getValue(),
                        stockItem == null ? null : stockItem.getId());
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime
                * result
                + ((stockItem == null) ? 0 : stockItem.getId() == null ? 0
                        : stockItem.getId().hashCode());
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
        CustomStockItemField other = (CustomStockItemField) obj;
        if (stockItem == null) {
            if (other.stockItem != null)
                return false;
        } else if (!stockItem.getId().equals(other.stockItem.getId()))
            return false;
        return true;
    }

}
