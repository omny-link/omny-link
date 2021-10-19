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

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.PreUpdate;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.rest.core.annotation.RestResource;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import link.omny.catalog.json.JsonCustomOrderItemFieldDeserializer;
import link.omny.catalog.views.OrderViews;
import link.omny.supportservices.json.JsonCustomFieldSerializer;
import link.omny.supportservices.model.CustomField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@EqualsAndHashCode(exclude = { "order", "stockItem" })
@ToString(exclude = { "order", "stockItem" })
@Entity
@Table(name = "OL_ORDER_ITEM")
@AllArgsConstructor
@NoArgsConstructor
public class OrderItem implements Serializable {

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

    @Temporal(TemporalType.TIMESTAMP)
    // Since this is SQL 92 it should be portable
    @Column(columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP", updatable = false)
    @JsonProperty
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @JsonView(OrderViews.Detailed.class)
    private Date created;

    @Temporal(TemporalType.TIMESTAMP)
    @JsonProperty
    // If json view included as below, get exception:
    //DefaultHandlerExceptionResolver: Failed to write HTTP message: org.springframework.http.converter.HttpMessageNotWritableException: Could not write content: java.sql.Timestamp cannot be cast to java.lang.String (through reference chain: java.util.ArrayList[0]->link.omny.catalog.web.OrderResource["orderItems"]->org.hibernate.collection.internal.PersistentBag[0]->link.omny.catalog.model.OrderItem["lastUpdated"]); nested exception is com.fasterxml.jackson.databind.JsonMappingException: java.sql.Timestamp cannot be cast to java.lang.String (through reference chain: java.util.ArrayList[0]->link.omny.catalog.web.OrderResource["orderItems"]->org.hibernate.collection.internal.PersistentBag[0]->link.omny.catalog.model.OrderItem["lastUpdated"])
    //@JsonView(OrderViews.Detailed.class)
    // Attempts to resolve with these JsonFormats both fail
    // Although https://github.com/FasterXML/jackson-databind/issues/1154
    // appears relevant switching to jackson-databind-2.7.3 made no difference
    //@JsonFormat(shape=JsonFormat.Shape.STRING, pattern = "yyyy-mm-dd")
    //@JsonFormat(shape=JsonFormat.Shape.OBJECT)
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Column(name = "last_updated")
    private Date lastUpdated;

    @JsonProperty
    @JsonView(OrderViews.Detailed.class)
    @Column(name = "tenant_id")
    private String tenantId;

    @ManyToOne
    @RestResource(rel = "order")
    @JsonBackReference
    private Order order;

    @JsonProperty
    @JsonView(OrderViews.Detailed.class)
    @ManyToOne(fetch = FetchType.EAGER)
    @RestResource(rel = "stockItem")
    @JoinColumn(name = "stock_item_id")
    private StockItem stockItem;

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

    public Set<CustomOrderItemField> getCustomFields() {
        if (customFields == null) {
            customFields = new HashSet<CustomOrderItemField>();
        }
        return customFields;
    }

    public void setCustomFields(List<CustomOrderItemField> fields) {
        for (CustomOrderItemField newField : fields) {
            setCustomField(newField);
        }
        setLastUpdated(new Date());
    }

    public Object getCustomFieldValue(@NotNull String fieldName) {
        for (CustomField field : getCustomFields()) {
            if (fieldName.equals(field.getName())) {
                return field.getValue();
            }
        }
        return null;
    }

    public void addCustomField(CustomOrderItemField customField) {
        customField.setOrderItem(this);
        setCustomField(customField);
    }

    public void setCustomField(CustomOrderItemField newField) {
        boolean found = false; 
        for (CustomOrderItemField field : getCustomFields()) {
            if (field.getName().equals(newField.getName())) {
                field.setValue(newField.getValue() == null ? null : newField
                        .getValue().toString());
                found= true;
            }
        }
        if (!found) {
            newField.setOrderItem(this);
            getCustomFields().add(newField);
        }
    }

    public boolean isPublished() {
        return "Published".equalsIgnoreCase(status);
    }

    @PreUpdate
    public void preUpdate() {
        lastUpdated = new Date();
    }

}
