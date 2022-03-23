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

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.PreUpdate;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.rest.core.annotation.RestResource;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import link.omny.catalog.json.JsonCustomOrderItemFieldDeserializer;
import link.omny.catalog.views.OrderViews;
import link.omny.supportservices.json.JsonCustomFieldSerializer;
import link.omny.supportservices.model.Auditable;
import link.omny.supportservices.model.CustomField;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(exclude = { "order" })
@Entity
@Table(name = "OL_ORDER_ITEM")
@AllArgsConstructor
@NoArgsConstructor
public class OrderItem extends Auditable<String> implements Serializable {

    private static final long serialVersionUID = 8577876040188427429L;

    protected static final Logger LOGGER = LoggerFactory
            .getLogger(OrderItem.class);

    public static final int DEFAULT_IMAGE_COUNT = 4;

    @Id
    @Column(name = "id")
    @SequenceGenerator(name = "orderItemIdSeq", sequenceName = "ol_order_item_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "orderItemIdSeq")
    @JsonProperty
    @JsonView(OrderViews.Detailed.class)
    private Long id;

    @JsonProperty
    @JsonView(OrderViews.Detailed.class)
    private String type;

    @JsonProperty
    @Size(max = 20)
    @JsonView(OrderViews.Detailed.class)
    private String status = "Draft";

    @JsonProperty
    @JsonView(OrderViews.Detailed.class)
    private BigDecimal price;

    @JsonProperty
    @JsonView(OrderViews.Detailed.class)
    @Column(name = "tenant_id")
    private String tenantId;

    @ManyToOne
    @RestResource(rel = "order")
    @JsonBackReference
    private Order order;

    @JsonProperty
    @JsonView(OrderViews.Summary.class)
    @Column(name = "stock_item_id")
    private Long stockItemId;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "orderItem", orphanRemoval = true)
    @JsonDeserialize(using = JsonCustomOrderItemFieldDeserializer.class)
    @JsonSerialize(using = JsonCustomFieldSerializer.class)
    @JsonView(OrderViews.Detailed.class)
    private Set<CustomOrderItemField> customFields;

    public OrderItem(String type) {
        this();
        setType(type);
    }

    /** @deprecated */
    public String getSelfRef() {
        return String.format("/order-items/%1$d", id);
    }

    public Object getCustomFieldValue(@NotNull String fieldName) {
        if (customFields == null) {
            return null;
        }
        for (CustomField field : getCustomFields()) {
            if (fieldName.equals(field.getName())) {
                return field.getValue();
            }
        }
        return null;
    }

    public void addCustomField(CustomOrderItemField newField) {
        newField.setOrderItem(this);

        boolean found = false;
        if (customFields == null) {
            customFields = new HashSet<CustomOrderItemField>();
        }
        for (CustomOrderItemField field : customFields) {
            if (field.getName().equals(newField.getName())) {
                field.setValue(newField.getValue() == null ? null : newField
                        .getValue().toString());
                found= true;
            }
        }
        if (!found) {
            newField.setOrderItem(this);
            customFields.add(newField);
        }
    }

    public boolean isPublished() {
        return "Published".equalsIgnoreCase(status);
    }

    @PreUpdate
    public void preUpdate() {
        lastUpdated = new Date();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        try {
        result = prime * result
                + ((customFields == null) ? 0 : customFields.hashCode());
        } catch (RuntimeException e) {
            // This seems to happen only when invoking
            // OrderController.findByContacts and appears to be ignorable
            // as custom fields are already in memory
            LOGGER.error("{} threw {}: {}", id, e.getClass().getName(), e.getMessage());
        }
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((price == null) ? 0 : price.hashCode());
        result = prime * result + ((status == null) ? 0 : status.hashCode());
        result = prime * result
                + ((stockItemId == null) ? 0 : stockItemId.hashCode());
        result = prime * result
                + ((tenantId == null) ? 0 : tenantId.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
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
        OrderItem other = (OrderItem) obj;
        if (customFields == null) {
            if (other.customFields != null)
                return false;
        } else if (!customFields.equals(other.customFields))
            return false;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        if (price == null) {
            if (other.price != null)
                return false;
        } else if (!price.equals(other.price))
            return false;
        if (status == null) {
            if (other.status != null)
                return false;
        } else if (!status.equals(other.status))
            return false;
        if (stockItemId == null) {
            if (other.stockItemId != null)
                return false;
        } else if (!stockItemId.equals(other.stockItemId))
            return false;
        if (tenantId == null) {
            if (other.tenantId != null)
                return false;
        } else if (!tenantId.equals(other.tenantId))
            return false;
        if (type == null) {
            if (other.type != null)
                return false;
        } else if (!type.equals(other.type))
            return false;
        return true;
    }
}
