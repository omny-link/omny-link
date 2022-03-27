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
import javax.persistence.NamedAttributeNode;
import javax.persistence.NamedEntityGraph;
import javax.persistence.NamedSubgraph;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.PrePersist;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import link.omny.catalog.json.JsonCustomOrderFieldDeserializer;
import link.omny.catalog.views.OrderViews;
import link.omny.supportservices.internal.CsvUtils;
import link.omny.supportservices.json.JsonCustomFieldSerializer;
import link.omny.supportservices.model.Auditable;
import link.omny.supportservices.model.CustomField;
import link.omny.supportservices.model.Document;
import link.omny.supportservices.model.Note;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@NamedEntityGraph(name = "orderWithAll",
    attributeNodes = {
        @NamedAttributeNode("customFields"),
        @NamedAttributeNode(value = "feedback", subgraph = "feedback-subgraph"),
        @NamedAttributeNode(value = "orderItems", subgraph = "item-subgraph"),
        @NamedAttributeNode("notes"),
        @NamedAttributeNode("documents")
    },
    subgraphs = {
        @NamedSubgraph(
                name = "feedback-subgraph",
                attributeNodes = { @NamedAttributeNode("customFields") }
        ),
        @NamedSubgraph(
                name = "item-subgraph",
                attributeNodes = { @NamedAttributeNode("customFields") }
        )
    }
)
@NamedEntityGraph(name = "orderWithItems",
    attributeNodes = {
        @NamedAttributeNode(value = "orderItems", subgraph = "order-item-subgraph"),
        @NamedAttributeNode("customFields")
    },
    subgraphs = {
        @NamedSubgraph(
                name = "item-subgraph",
                attributeNodes = {
                        @NamedAttributeNode("customFields")
                })
        }
)
@NamedEntityGraph(name = "orderOnly",
  attributeNodes = { @NamedAttributeNode("customFields") }
)
@Table(name = "OL_ORDER")
@Data
@ToString(exclude = { "documents", "notes", "orderItems" })
@EqualsAndHashCode(callSuper = true, exclude = { "documents", "notes", "orderItems" })
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true, allowGetters = true)
public class Order extends Auditable<String> implements Serializable {

    private static final long serialVersionUID = -2334761729349848501L;

    protected static final Logger LOGGER = LoggerFactory
            .getLogger(Order.class);

    @Id
    @Column(name = "id")
    @SequenceGenerator(name = "orderIdSeq", sequenceName = "ol_order_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "orderIdSeq")
    @JsonProperty
    @JsonView(OrderViews.Summary.class)
    private Long id;

    /**
     * A publicly visible identifier typically populated with a per-tenant
     * unique and contiguous integer.
     */
    @Column(name = "ref")
    @JsonProperty
    @JsonView(OrderViews.Summary.class)
    private Long ref;

    @JsonProperty
    @JsonView(OrderViews.Summary.class)
    private String name;

    @JsonProperty
    @Size(max = 60)
    @JsonView(OrderViews.Summary.class)
    private String type;

    @JsonProperty
    @Size(max = 1000)
    @JsonView(OrderViews.Summary.class)
    private String description;

    @Temporal(TemporalType.DATE)
    @JsonProperty
    @JsonFormat(pattern = "yyyy-MM-dd")
    @JsonView(OrderViews.Summary.class)
    private Date date;

    @JsonProperty
    @Size(max = 50)
    @JsonFormat(pattern = "yyyy-MM-dd")
    @JsonView(OrderViews.Summary.class)
    @Column(name = "due_date")
    private String dueDate;

    @JsonProperty
    @Size(max = 60)
    @JsonView(OrderViews.Summary.class)
    private String stage = "New enquiry";

    @JsonProperty
    @JsonView(OrderViews.Summary.class)
    private BigDecimal price;

    @JsonProperty
    @JsonView(OrderViews.Summary.class)
    private BigDecimal tax;

    @JsonProperty
    @JsonView(OrderViews.Summary.class)
    @Column(name = "stock_item_id")
    private Long stockItemId;

    @JsonProperty
    @Size(max = 30)
    @JsonView(OrderViews.Summary.class)
    @Column(name = "invoice_ref")
    private String invoiceRef;

    @JsonProperty
    @JsonView({ OrderViews.Summary.class })
    @Size(max = 50)
    @Column(name = "owner")
    private String owner;

    @JsonProperty
    @JsonView(OrderViews.Summary.class)
    @Column(name = "contact_id")
    private Long contactId;

    @JsonProperty
    @JsonView(OrderViews.Summary.class)
    @Column(name = "tenant_id")
    private String tenantId;

    @JoinColumn(name = "parent_id")
    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER, optional = true)
    private Order parent;

    @Transient
    @JsonProperty
    @JsonView(OrderViews.Detailed.class)
    private List<Order> childOrders;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "order", orphanRemoval = true)
    @JsonDeserialize(using = JsonCustomOrderFieldDeserializer.class)
    @JsonSerialize(using = JsonCustomFieldSerializer.class)
    @JsonView(OrderViews.Detailed.class)
    private Set<CustomOrderField> customFields;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "order", targetEntity = OrderItem.class)
    @JsonView(OrderViews.Detailed.class)
    @JsonManagedReference
    private Set<OrderItem> orderItems;

    @OneToOne
    @JsonView( { OrderViews.Detailed.class } )
    @JsonManagedReference
    private Feedback feedback;

    @OneToMany(cascade = CascadeType.ALL, targetEntity = Note.class)
    @JoinColumn(name = "order_id")
    @JsonView({ OrderViews.Enhanced.class })
    private Set<Note> notes;

    @OneToMany(cascade = CascadeType.ALL, targetEntity = Document.class)
    @JoinColumn(name = "order_id")
    @JsonView({ OrderViews.Enhanced.class })
    private Set<Document> documents;

    @Transient
    private List<String> customHeadings;

    public Order(String name) {
        this();
        setName(name);
    }

    public Order(Long id, Long ref, String name, String desc, String type,
            Date date, Date dueDate, String stage, BigDecimal price, BigDecimal tax) {
        this(name);
        setId(id);
        setRef(ref);
        setDescription(desc);
        setType(type);
        setStage(stage);
        setPrice(price);
        setTax(tax);
    }

    @JsonProperty
    @JsonView(OrderViews.Summary.class)
    public Long getLocalId() {
        return id;
    }

    public Set<CustomOrderField> getCustomFields() {
        if (customFields == null) {
            customFields = new HashSet<CustomOrderField>();
        }
        return customFields;
    }

    public void setCustomFields(Set<CustomOrderField> fields) {
        for (CustomOrderField newField : fields) {
            setCustomField(newField);
        }
        setLastUpdated(new Date());
    }

    public String getCustomFieldValue(@NotNull String fieldName) {
        for (CustomField field : getCustomFields()) {
            if (fieldName.equals(field.getName())) {
                return field.getValue();
            }
        }
        return null;
    }

    public void addCustomField(CustomOrderField customField) {
        customField.setOrder(this);
        getCustomFields().add(customField);
    }

    public void setCustomField(CustomOrderField newField) {
        boolean found = false;
        for (CustomOrderField field : getCustomFields()) {
            if (field.getName().equals(newField.getName())) {
                field.setValue(newField.getValue() == null ? null : newField
                        .getValue().toString());
                found = true;
            }
        }
        if (!found) {
            newField.setOrder(this);
            getCustomFields().add(newField);
        }
    }

    public void setOrderItems(Set<OrderItem> newItems) {
        orderItems = newItems;
        if (newItems == null ) {
            return ;
        }
        for (OrderItem item : newItems) {
            item.setOrder(this);
            item.setTenantId(tenantId);
        }
    }

    public Set<OrderItem> getOrderItems() {
        if (orderItems == null) {
            orderItems = new HashSet<OrderItem>();
        }
        return orderItems;
    }

    public Order addOrderItem(OrderItem item) {
        item.setOrder(this);
        item.setTenantId(tenantId);
        getOrderItems().add(item);
        return this;
    }

    public Set<Note> getNotes() {
        if (notes == null) {
            notes = new HashSet<Note>();
        }
        return notes;
    }

    public Set<Document> getDocuments() {
        if (documents == null) {
            documents = new HashSet<Document>();
        }
        return documents;
    }

    public void addNote(Note note) {
        getNotes().add(note);
    }

    public void addDocument(Document doc) {
        getDocuments().add(doc);
    }

    @PrePersist
    public void prePersist() {
        if (type == null) {
            type = "order";
        }
    }

    public String toCsv() {
        StringBuilder sb = new StringBuilder()
                .append(String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,"
                        + "%s,%s,%s,%s,%s,%s",
                        id,
                        name,
                        ref == null ? "" : ref,
                        type == null ? "" : type,
                        description == null ? "" : CsvUtils.quoteIfNeeded(description),
                        date == null ? "" : date,
                        dueDate == null ? "" : dueDate,
                        stage == null ? "" : "New enquiry",
                        price == null ? "" : price,
                        tax == null ? "" : tax,
                        invoiceRef == null ? "" : invoiceRef,
                        owner == null ? "" : owner,
                        contactId == null ? "" : contactId,
//                        stockItem == null ? "" : stockItem.getId(),
                        stockItemId  == null ? "" : stockItemId,
                        tenantId, created, lastUpdated,
                        getConsolidatedNotes(),
                        getConsolidatedDocuments()));
        if (customHeadings == null) {
            LOGGER.warn("No custom headings specified, so only standard fields can be included");
        } else {
            for (String fieldName : customHeadings) {
                String val = getCustomFieldValue(fieldName);
                sb.append(',').append(val == null ? "" : CsvUtils.quoteIfNeeded(val));
            }
        }
        return sb.toString();
    }

    private String getConsolidatedNotes() {
        StringBuffer sb = new StringBuffer();
        for (Note note : getNotes()) {
            if (note.getContent() != null) {
                sb.append(String.format("%s %s: %s;",
                        note.getCreated(), note.getAuthor(),
                        note.getContent()));
            }
        }
        return CsvUtils.quoteIfNeeded(sb.toString());
    }

    private String getConsolidatedDocuments() {
        StringBuffer sb = new StringBuffer();
        for (Document doc : getDocuments()) {
            sb.append(String.format("%s %s: %s %s;",
                    doc.getCreated(), doc.getAuthor(), doc.getName(), doc.getUrl()));
        }
        return CsvUtils.quoteIfNeeded(sb.toString());
    }

}
