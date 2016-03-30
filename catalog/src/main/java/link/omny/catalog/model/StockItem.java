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
    private String unit;

    @JsonProperty
    private BigDecimal unitPrice;
    
    @JsonProperty
    private String mapUrl;
    
    @JsonProperty
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
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "stockItem")
    private List<MediaResource> images;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "stockItem")
    @JsonDeserialize(using = JsonCustomStockItemFieldDeserializer.class)
    @JsonSerialize(using = JsonCustomFieldSerializer.class)
    private List<CustomStockItemField> customFields;

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

    @PreUpdate
    public void preUpdate() {
        if (LOGGER.isWarnEnabled() && lastUpdated != null) {
            LOGGER.warn(String.format(
                    "Overwriting update date %1$s with 'now'.", lastUpdated));
        }
        lastUpdated = new Date();
    }

}
