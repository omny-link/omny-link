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
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.PrePersist;
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
import org.springframework.hateoas.Link;

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
import link.omny.custmgmt.model.Document;
import link.omny.custmgmt.model.Note;
import link.omny.supportservices.internal.CsvUtils;
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
    @Size(max = 1500)
    private String description;

    @JsonProperty
    @JsonView({StockCategoryViews.Detailed.class, StockItemViews.Detailed.class})
    @Size(max = 60)
    private String address1;

    @JsonProperty
    @JsonView({StockCategoryViews.Detailed.class, StockItemViews.Detailed.class})
    @Size(max = 60)
    private String address2;

    @JsonProperty
    @JsonView({StockCategoryViews.Detailed.class, StockItemViews.Detailed.class})
    @Size(max = 60)
    private String town;

    @JsonProperty
    @JsonView({StockCategoryViews.Detailed.class, StockItemViews.Detailed.class})
    @Size(max = 60)
    @Column(name = "county_or_city")
    private String countyOrCity;

    @JsonProperty
    @JsonView({StockCategoryViews.Detailed.class, StockItemViews.Detailed.class})
    @Size(max = 10)
    @Column(name = "post_code")
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
    @Column(name = "map_url")
    private String mapUrl;

    @JsonProperty
    @JsonView({StockCategoryViews.Detailed.class, StockItemViews.Detailed.class})
    @Size(max = 1000)
    @Column(name = "directions_by_road")
    private String directionsByRoad;

    @JsonProperty
    @JsonView({StockCategoryViews.Detailed.class, StockItemViews.Detailed.class})
    @Size(max = 1000)
    @Column(name = "directions_by_public_transport")
    private String directionsByPublicTransport;

    @JsonProperty
    @JsonView({StockCategoryViews.Detailed.class, StockItemViews.Detailed.class})
    @Size(max = 1000)
    @Column(name = "directions_by_air")
    private String directionsByAir;

    @JsonProperty
    @JsonView(StockCategoryViews.Detailed.class)
    @Column(name = "video_code")
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
    @Column(name = "offer_status")
    private String offerStatus;

    @JsonProperty
    @JsonView(StockCategoryViews.Detailed.class)
    @Size(max = 35)
    @Column(name = "offer_title")
    private String offerTitle;

    @JsonProperty
    @JsonView(StockCategoryViews.Detailed.class)
    @Size(max = 80)
    @Column(name = "offer_description")
    private String offerDescription;

    @JsonProperty
    @JsonView(StockCategoryViews.Detailed.class)
    @Size(max = 30)
    @Column(name = "offer_call_to_action")
    private String offerCallToAction;

    /**
     * A relative or absolute URL associated with the offer, i.e. where it goes
     * to on click.
     */
    @JsonProperty
    @JsonView(StockCategoryViews.Detailed.class)
    @Column(name = "offer_url")
    private String offerUrl;

    @JsonProperty
    @JsonView(StockCategoryViews.Detailed.class)
    @Transient
    // @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy =
    // "stockCategory")
    private List<MediaResource> images;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "stock_cat_id", nullable = true)
    @JsonView({ StockCategoryViews.Detailed.class })
    private List<Note> notes;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "stock_cat_id", nullable = true)
    @JsonView({ StockCategoryViews.Detailed.class })
    private List<Document> documents;

    @Temporal(TemporalType.TIMESTAMP)
    // Since this is SQL 92 it should be portable
    @Column(columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP", updatable = false)
    @JsonProperty
    @JsonView(StockCategoryViews.Detailed.class)
    private Date created = new Date();

    @Temporal(TemporalType.TIMESTAMP)
    @JsonProperty
    @JsonView(StockCategoryViews.Detailed.class)
    @Column(name = "last_updated")
    private Date lastUpdated;

    @JsonProperty
    @JsonView(StockCategoryViews.Detailed.class)
    @Column(name = "tenant_id")
    private String tenantId;

    @Transient
    @JsonProperty
    @JsonView(StockCategoryViews.Detailed.class)
    private double distance;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "stockCategory")
    @JsonDeserialize(using = JsonCustomStockCategoryFieldDeserializer.class)
    @JsonSerialize(using = JsonCustomFieldSerializer.class)
    @JsonView({StockCategoryViews.Detailed.class, StockItemViews.Detailed.class})
    private List<CustomStockCategoryField> customFields;

    @JsonView(StockCategoryViews.Detailed.class)
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "stockCategory", targetEntity = StockItem.class)
    private List<StockItem> stockItems;

    @Transient
    @XmlElement(name = "link", namespace = Link.ATOM_NAMESPACE)
    @JsonProperty("links")
    @JsonView(StockCategoryViews.Summary.class)
    private List<Link> links;

    @Transient
    private List<String> customHeadings;

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

    public String getCustomFieldValue(@NotNull String fieldName) {
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

    @PrePersist
    public void preInsert() {
        if (LOGGER.isWarnEnabled() && created != null) {
            LOGGER.warn(String.format(
                    "Overwriting creation date %1$s with 'now'.", created));
        }
        created = new Date();
    }

    @PreUpdate
    public void preUpdate() {
        lastUpdated = new Date();
    }

    public String toCsv() {
        StringBuilder sb = new StringBuilder()
                .append(String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s",
                        id,
                        name,
                        description == null ? "" : CsvUtils.quoteIfNeeded(description),
                        address1 == null ? "" : CsvUtils.quoteIfNeeded(address1),
                        address2 == null ? "" : CsvUtils.quoteIfNeeded(address2),
                        town == null ? "" : CsvUtils.quoteIfNeeded(town),
                        countyOrCity == null ? "" : CsvUtils.quoteIfNeeded(countyOrCity),
                        postCode == null ? "" : CsvUtils.quoteIfNeeded(postCode),
                        country == null ? "" : CsvUtils.quoteIfNeeded(country),
                        lat == null ? "" : lat,
                        lng == null ? "" : lng,
                        tags == null ? "" : CsvUtils.quoteIfNeeded(tags),
                        mapUrl == null ? "" : CsvUtils.quoteIfNeeded(mapUrl),
                        directionsByRoad == null ? "" : CsvUtils.quoteIfNeeded(directionsByRoad),
                        directionsByPublicTransport == null ? "" : CsvUtils.quoteIfNeeded(directionsByPublicTransport),
                        directionsByAir == null ? "" : CsvUtils.quoteIfNeeded(directionsByAir),
                        videoCode == null ? "" : CsvUtils.quoteIfNeeded(videoCode),
                        status == null ? "Draft" : CsvUtils.quoteIfNeeded(status),
                        productSheetUrl == null ? "" : CsvUtils.quoteIfNeeded(productSheetUrl),
                        offerStatus == null ? "Draft" : CsvUtils.quoteIfNeeded(offerStatus),
                        offerTitle == null ? "" : CsvUtils.quoteIfNeeded(offerTitle),
                        offerDescription == null ? "" : CsvUtils.quoteIfNeeded(offerDescription),
                        offerCallToAction == null ? "" : CsvUtils.quoteIfNeeded(offerCallToAction),
                        offerUrl == null ? "" : CsvUtils.quoteIfNeeded(offerUrl),
                        tenantId, created, lastUpdated));
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
}
