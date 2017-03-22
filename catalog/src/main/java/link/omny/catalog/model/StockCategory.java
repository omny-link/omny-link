package link.omny.catalog.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.OneToMany;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import link.omny.catalog.json.JsonCustomStockCategoryFieldDeserializer;
import link.omny.catalog.model.api.ShortStockCategory;
import link.omny.catalog.views.StockCategoryViews;
import link.omny.catalog.views.StockItemViews;
import link.omny.custmgmt.json.JsonCustomFieldSerializer;
import link.omny.custmgmt.model.CustomField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "OL_STOCK_CAT")
@Data
@ToString(exclude = { "description", "stockItems" })
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class StockCategory implements ShortStockCategory, Serializable {

    public static final int DEFAULT_IMAGE_COUNT = 8;

    private static final long serialVersionUID = -6115608228931780960L;

    protected static final Logger LOGGER = LoggerFactory
            .getLogger(StockCategory.class);

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    @JsonProperty
    @JsonView({StockCategoryViews.Summary.class, StockItemViews.Detailed.class})
    private Long id;

    @JsonProperty
    @JsonView({StockCategoryViews.Summary.class, StockItemViews.Detailed.class})
    @Column(unique = true)
    @NotNull
    private String name;

    @JsonProperty
    @JsonView({StockCategoryViews.Detailed.class, StockItemViews.Detailed.class})
    @Lob
    private String description;

    @JsonProperty
    @JsonView({StockCategoryViews.Detailed.class, StockItemViews.Detailed.class})
    private String address1;

    @JsonProperty
    @JsonView({StockCategoryViews.Detailed.class, StockItemViews.Detailed.class})
    private String address2;

    @JsonProperty
    @JsonView({StockCategoryViews.Detailed.class, StockItemViews.Detailed.class})
    private String town;

    @JsonProperty
    @JsonView({StockCategoryViews.Detailed.class, StockItemViews.Detailed.class})
    private String countyOrCity;

    @JsonProperty
    @JsonView({StockCategoryViews.Detailed.class, StockItemViews.Detailed.class})
    private String postCode;

    @JsonProperty
    @JsonView({StockCategoryViews.Detailed.class, StockItemViews.Detailed.class})
    private String country;

    @JsonProperty
    @JsonView({StockCategoryViews.Detailed.class, StockItemViews.Detailed.class})
    private Double lat;

    @JsonProperty
    @JsonView({StockCategoryViews.Detailed.class, StockItemViews.Detailed.class})
    private Double lng;

    @JsonProperty
    @JsonView({StockCategoryViews.Detailed.class, StockItemViews.Detailed.class})
    @Transient
    private String tags;

    @JsonProperty
    @JsonView({StockCategoryViews.Detailed.class, StockItemViews.Detailed.class})
    private String mapUrl;

    @JsonProperty
    @JsonView({StockCategoryViews.Detailed.class, StockItemViews.Detailed.class})
    @Lob
    private String directionsByRoad;

    @JsonProperty
    @JsonView({StockCategoryViews.Detailed.class, StockItemViews.Detailed.class})
    @Lob
    private String directionsByPublicTransport;

    @JsonProperty
    @JsonView({StockCategoryViews.Detailed.class, StockItemViews.Detailed.class})
    @Lob
    private String directionsByAir;

    @JsonProperty
    @JsonView(StockCategoryViews.Detailed.class)
    private String videoCode;

    @JsonProperty
    @JsonView({StockCategoryViews.Detailed.class, StockItemViews.Detailed.class})
    @Size(max = 20)
    private String status;
    
    /**
     * A relative or absolute URL to a product sheet or brochure page, often a PDF.
     */
    @JsonProperty
    @JsonView({StockCategoryViews.Detailed.class, StockItemViews.Detailed.class})
    @Column(name="product_sheet")
    private String productSheetUrl;

    @JsonProperty
    @JsonView(StockCategoryViews.Detailed.class)
    @Size(max = 20)
    private String offerStatus;

    @JsonProperty
    @JsonView(StockCategoryViews.Detailed.class)
    @Size(max = 35)
    private String offerTitle;

    @JsonProperty
    @JsonView(StockCategoryViews.Detailed.class)
    @Size(max = 80)
    private String offerDescription;

    @JsonProperty
    @JsonView(StockCategoryViews.Detailed.class)
    @Size(max = 30)
    private String offerCallToAction;

    /**
     * A relative or absolute URL associated with the offer, i.e. where it goes
     * to on click.
     */
    @JsonProperty
    @JsonView(StockCategoryViews.Detailed.class)
    private String offerUrl;

    @JsonProperty
    @JsonView(StockCategoryViews.Detailed.class)
    @Transient
    // @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy =
    // "stockCategory")
    private List<MediaResource> images;

    @Temporal(TemporalType.TIMESTAMP)
    // Since this is SQL 92 it should be portable
    @Column(columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP", updatable = false)
    @JsonProperty
    @JsonView(StockCategoryViews.Detailed.class)
    private Date created = new Date();

    @Temporal(TemporalType.TIMESTAMP)
    @JsonProperty
    @JsonView(StockCategoryViews.Detailed.class)
    private Date lastUpdated;

    @JsonProperty
    @JsonView(StockCategoryViews.Detailed.class)
    private String tenantId;

    @Transient
    @JsonProperty
    @JsonView(StockCategoryViews.Detailed.class)
    private double distance;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "stockCategory")
    @JsonDeserialize(using = JsonCustomStockCategoryFieldDeserializer.class)
    @JsonSerialize(using = JsonCustomFieldSerializer.class)
    @JsonView(StockCategoryViews.Detailed.class)
    private List<CustomStockCategoryField> customFields;

    @JsonView(StockCategoryViews.Detailed.class)
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "stockCategory", targetEntity = StockItem.class)
    private List<StockItem> stockItems;

    public StockCategory(String name) {
        this();
        setName(name);
    }

    public List<CustomStockCategoryField> getCustomFields() {
        if (customFields == null) {
            customFields = new ArrayList<CustomStockCategoryField>();
        }
        return customFields;
    }

    public void setCustomFields(List<CustomStockCategoryField> fields) {
        for (CustomStockCategoryField newField : fields) {
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

    public void addCustomField(CustomStockCategoryField customField) {
        customField.setStockCategory(this);
        getCustomFields().add(customField);
    }

    protected void setCustomField(CustomStockCategoryField newField) {
        boolean found = false;
        for (CustomStockCategoryField field : getCustomFields()) {
            if (field.getName().equals(newField.getName())) {
                field.setValue(newField.getValue() == null ? null : newField
                        .getValue().toString());
                found = true;
            }
        }
        if (!found) {
            newField.setStockCategory(this);
            getCustomFields().add(newField);
        }
    }

    public List<MediaResource> getImages() {
        if (images == null || images.size() == 0) {
            images = new ArrayList<MediaResource>(DEFAULT_IMAGE_COUNT);
            for (int i = 0; i < DEFAULT_IMAGE_COUNT; i++) {
                images.add(new MediaResource(getTenantId(), String.format(
                        "/images/%1$s/%2$d.jpg",
                        name.toLowerCase().replaceAll(" ", "_"), i + 1)));
            }
        }
        return images;
    }

    public List<StockItem> getStockItems() {
        if (stockItems == null) {
            stockItems = new ArrayList<StockItem>();
        }
        return stockItems;
    }

    public StockCategory addStockItem(StockItem item) {
        getStockItems().add(item);
        return this;
    }

    public String getTags() {
        if (tags != null) {
            return tags;
        }
        if (stockItems == null) {
            return null;
        }
        List<String> tags = new ArrayList<String>();
        for (StockItem stockItem : stockItems) {
            if (stockItem != null && stockItem.isPublished()) {
                for (String tag : stockItem.getTagsAsList()) {
                    if (!tags.contains(tag)) {
                        tags.add(tag);
                    }
                }

            }
        }

        Collections.sort(tags, (o1, o2) -> o1.compareToIgnoreCase(o2));

        StringBuilder sb = new StringBuilder();
        for (String stockItem : tags) {
            sb.append(stockItem).append(",");
        }
        if (sb.toString().endsWith(",")) {
            return sb.deleteCharAt(sb.length() - 1).toString();
        } else {
            return sb.toString();
        }
    }

    public String getMapUrl() {
        if (mapUrl == null) {
            mapUrl = String.format("http://www.google.com/maps/place/%1$s",
                    postCode);
        }
        return mapUrl;
    }

    public String getVideoCode() {
        if (videoCode == null) {
            videoCode = "";
        }
        return videoCode;
    }

    public GeoPoint getGeoPoint() {
        if (getLat() == null || getLng() == null) {
            return null;
        }
        return new GeoPoint(getLat(), getLng());
    }

    public void setGeoPoint(GeoPoint point) {
        if (point == null) {
            return;
        }
        setLat(point.getLat());
        setLng(point.getLng());
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
