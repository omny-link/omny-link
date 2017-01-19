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
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import link.omny.catalog.json.JsonCustomOrderItemFieldDeserializer;
import link.omny.custmgmt.json.JsonCustomFieldSerializer;
import link.omny.custmgmt.model.CustomField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.rest.core.annotation.RestResource;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

// Property in Flexspace terminology 
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
    @GeneratedValue(strategy = GenerationType.AUTO)
    @JsonProperty
    private Long id;

    @JsonProperty
    private String description;

    @JsonProperty
    private String type;

    @JsonProperty
    @Size(max = 20)
    private String status = "Draft";

    @JsonProperty
    private BigDecimal price;

    @Temporal(TemporalType.TIMESTAMP)
    // Since this is SQL 92 it should be portable
    @Column(columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP", updatable = false)
    @JsonProperty
    private Date created;

    @Temporal(TemporalType.TIMESTAMP)
    @JsonProperty
    private Date lastUpdated;

    @JsonProperty
    private String tenantId;

    @ManyToOne
    @RestResource(rel = "order")
    private Order order;

    @ManyToOne(fetch = FetchType.EAGER)
    @RestResource(rel = "stockItem")
    private StockItem stockItem;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "orderItem", orphanRemoval = true)
    @JsonDeserialize(using = JsonCustomOrderItemFieldDeserializer.class)
    @JsonSerialize(using = JsonCustomFieldSerializer.class)
    private List<CustomOrderItemField> customFields;

    public OrderItem(String desc, String type) {
        this();
        setDescription(desc);
        setType(type);
    }

    public String getSelfRef() {
        return String.format("/order-items/%1$d", id);
    }

    public List<CustomOrderItemField> getCustomFields() {
        if (customFields == null) {
            customFields = new ArrayList<CustomOrderItemField>();
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
        if (LOGGER.isWarnEnabled() && lastUpdated != null) {
            LOGGER.warn(String.format(
                    "Overwriting update date %1$s with 'now'.", lastUpdated));
        }
        lastUpdated = new Date();
    }

}
