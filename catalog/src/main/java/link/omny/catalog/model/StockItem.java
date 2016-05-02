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
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import link.omny.catalog.json.JsonCustomStockItemFieldDeserializer;
import link.omny.custmgmt.json.JsonCustomFieldSerializer;
import link.omny.custmgmt.model.CustomField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

// Property in Flexspace terminology 
@Data
@Entity
@Table(name = "OL_STOCK_ITEM")
@AllArgsConstructor
@NoArgsConstructor
public class StockItem implements Serializable {

    private static final long serialVersionUID = 6825862597448133674L;

    protected static final Logger LOGGER = LoggerFactory
            .getLogger(StockItem.class);

    public static final int DEFAULT_IMAGE_COUNT = 4;

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    @JsonProperty
    private Long id;

    @JsonProperty
    @Column(unique = true)
    private String name;

    @JsonProperty
    private String description;

    @JsonProperty
    private String size;

    @JsonProperty
    @Transient
    private String sizeString;

    @JsonProperty
    private String unit;

    @JsonProperty
    private BigDecimal price;

    // @JsonProperty
    // @Transient
    // private String priceString;

    @JsonProperty
    // @NotNull
    private String type;

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

    @ManyToOne(targetEntity = StockCategory.class)
    @JoinColumn(name = "stock_cat_id")
    private StockCategory stockCategory;

    @JsonProperty
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "stockItem")
    private List<MediaResource> images;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "stockItem")
    @JsonDeserialize(using = JsonCustomStockItemFieldDeserializer.class)
    @JsonSerialize(using = JsonCustomFieldSerializer.class)
    private List<CustomStockItemField> customFields;

    public static final int CURRENCY_SCALE = 2;

    public StockItem(String name, String type) {
        setName(name);
        setType(type);
    }

    public String getSelfRef() {
        return String.format("/stock-items/%1$d", id);
    }

    public List<CustomStockItemField> getCustomFields() {
        if (customFields == null) {
            customFields = new ArrayList<CustomStockItemField>();
        }
        return customFields;
    }

    public void setCustomFields(List<CustomStockItemField> fields) {
        this.customFields = fields;
        // setLastUpdated(new Date());
    }

    public Object getField(@NotNull String fieldName) {
        for (CustomField field : getCustomFields()) {
            if (fieldName.equals(field.getName())) {
                return field.getValue();
            }
        }
        return null;
    }

    public void addCustomField(CustomStockItemField customField) {
        getCustomFields().add(customField);
    }

    public void setField(String key, Object value) {
        getCustomFields().add(
                new CustomStockItemField(key, value == null ? null : value
                        .toString()));
    }

    public StockCategory getStockCategory() {
        if (stockCategory == null) {
            stockCategory = new StockCategory();
        }
        return stockCategory;
    }

    public String getDescription() {
        if (description == null && stockCategory != null) {
            return stockCategory.getDescription();
        } else {
            return description;
        }
    }

    public List<MediaResource> getImages() {
        if (images == null || images.size() == 0) {
            images = new ArrayList<MediaResource>(DEFAULT_IMAGE_COUNT);
            for (int i = 0; i < DEFAULT_IMAGE_COUNT; i++) {
                images.add(new MediaResource(getTenantId(), String.format(
                        "/images/%1$s/%2$s/%3$d.jpg", name.toLowerCase()
                                .replaceAll(" ", "_"), type.toLowerCase()
                                .replaceAll(" ", "_"), i)));
            }
        }
        return images;
    }

    public String getSizeString() {
        return String.format("%1$s %2$s", size, unit);
    }

    @JsonProperty
    @Transient
    public String getPriceString() {
        if (price == null) {
            return "";
        } else {
            return price.setScale(CURRENCY_SCALE, BigDecimal.ROUND_HALF_DOWN)
                    .toPlainString();
        }
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
