package link.omny.catalog.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
import javax.persistence.OneToOne;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlElement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.hateoas.Link;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import link.omny.catalog.json.JsonCustomOrderFieldDeserializer;
import link.omny.catalog.model.api.OrderWithSubEntities;
import link.omny.catalog.views.OrderViews;
import link.omny.custmgmt.json.JsonCustomFieldSerializer;
import link.omny.custmgmt.model.CustomField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;


@Entity
@Table(name = "OL_ORDER")
@Data
@ToString(exclude = { "orderItems" })
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true, allowGetters = true)
public class Order implements OrderWithSubEntities, Serializable {

    private static final long serialVersionUID = -2334761729349848501L;

    protected static final Logger LOGGER = LoggerFactory
            .getLogger(Order.class);

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    @JsonProperty
//    @JsonFormat(shape= JsonFormat.Shape.NUMBER)
    @JsonView(OrderViews.Summary.class)
    private Long id;

    @JsonProperty
    @JsonView(OrderViews.Summary.class)
    private String name;

    @JsonProperty
    @Size(max = 60)
    @JsonView(OrderViews.Summary.class)
    private String type;

    @JsonProperty
    @JsonView(OrderViews.Summary.class)
    private String description;

    @Temporal(TemporalType.DATE)
    @JsonProperty
    @JsonView(OrderViews.Summary.class)
    private Date date;

    @JsonProperty
    @Size(max = 20)
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

    // Although this annotation does not change the name, without it Jackson
    // gets confused and attempts to cast id:Long to String. Why this property
    // affects a completely different one is a mystery I have not investigated
    @JsonProperty("stockItem")
    @JsonView(OrderViews.Detailed.class)
    @RestResource(rel = "stockItem")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "stock_item_id")
    private StockItem stockItem;

    @JsonProperty
    @Size(max = 30)
    @JsonView(OrderViews.Summary.class)
    @Column(name = "invoice_ref")
    private String invoiceRef;

    @JsonProperty
    @JsonView(OrderViews.Summary.class)
    @Column(name = "contact_id")
    private Long contactId;

    @Temporal(TemporalType.TIMESTAMP)
    // Since this is SQL 92 it should be portable
    @Column(columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP", updatable = false)
    @JsonProperty
    @JsonView(OrderViews.Summary.class)
    private Date created = new Date();

    @Temporal(TemporalType.TIMESTAMP)
    @JsonProperty
    @JsonView(OrderViews.Summary.class)
    @Column(name = "last_updated")
    private Date lastUpdated;

    @JsonProperty
    @JsonView(OrderViews.Summary.class)
    @Column(name = "tenant_id")
    private String tenantId;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "order", orphanRemoval = true)
    @JsonDeserialize(using = JsonCustomOrderFieldDeserializer.class)
    @JsonSerialize(using = JsonCustomFieldSerializer.class)
    @JsonView(OrderViews.Detailed.class)
    private List<CustomOrderField> customFields;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "order", targetEntity = OrderItem.class)
    @JsonView(OrderViews.Detailed.class)
    private List<OrderItem> orderItems;

    @OneToOne(fetch = FetchType.EAGER)
    @JsonView( { OrderViews.Detailed.class } )
    private Feedback feedback;

    @Transient
    @XmlElement(name = "link", namespace = Link.ATOM_NAMESPACE)
    @JsonProperty("links")
    @JsonView({ OrderViews.Summary.class })
    private List<Link> links;

    public Order(String name) {
        this();
        setName(name);
    }

    @JsonProperty
    @JsonView(OrderViews.Summary.class)
    public Long getLocalId() {
        return id;
    }

    public List<CustomOrderField> getCustomFields() {
        if (customFields == null) {
            customFields = new ArrayList<CustomOrderField>();
        }
        return customFields;
    }

    public void setCustomFields(List<CustomOrderField> fields) {
        for (CustomOrderField newField : fields) {
            setCustomField(newField);
        }
        // setLastUpdated(new Date());
    }

    public Object getCustomFieldValue(@NotNull String fieldName) {
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

    public void setOrderItems(List<OrderItem> newItems) {
        orderItems = newItems;
        if (newItems == null ) {
            return ;
        }
        for (OrderItem item : newItems) {
            item.setOrder(this);
            item.setTenantId(tenantId);
        }
    }

    public List<OrderItem> getOrderItems() {
        if (orderItems == null) {
            orderItems = new ArrayList<OrderItem>();
        }
        return orderItems;
    }

    public Order addOrderItem(OrderItem item) {
        item.setOrder(this);
        item.setTenantId(tenantId);
        getOrderItems().add(item);
        return this;
    }

    @PreUpdate
    public void preUpdate() {
        if (LOGGER.isWarnEnabled() && lastUpdated != null) {
            LOGGER.warn(String.format(
                    "Overwriting update date %1$s with 'now'.", lastUpdated));
        }
        lastUpdated = new Date();
    }

}
