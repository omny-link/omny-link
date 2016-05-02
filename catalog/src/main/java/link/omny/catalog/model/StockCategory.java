package link.omny.catalog.model;

import java.io.Serializable;
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
import javax.persistence.OneToMany;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import link.omny.custmgmt.json.JsonCustomContactFieldDeserializer;
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

@Entity
@Table(name = "OL_STOCK_CAT")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class StockCategory implements Serializable {

    public static final int DEFAULT_IMAGE_COUNT = 8;

    private static final long serialVersionUID = -6115608228931780960L;

    protected static final Logger LOGGER = LoggerFactory
            .getLogger(StockCategory.class);

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
    private String address1;

    @JsonProperty
    private String address2;

    @JsonProperty
    private String town;

    @JsonProperty
    private String countyOrCity;

    @JsonProperty
    private String postCode;

    @JsonProperty
    private String country;

    @JsonProperty
    private double lat;

    @JsonProperty
    private double lng;

    @JsonProperty
    @Transient
    private String types;

    @JsonProperty
    private String mapUrl;

    @JsonProperty
    private String videoCode;

    @JsonProperty
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "stockCategory")
    private List<MediaResource> images;

    @Temporal(TemporalType.TIMESTAMP)
    // Since this is SQL 92 it should be portable
    @Column(columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP", updatable = false)
    @JsonProperty
    private Date created = new Date();

    @Temporal(TemporalType.TIMESTAMP)
    @JsonProperty
    private Date lastUpdated;

    @JsonProperty
    private String tenantId;

    @Transient
    @JsonProperty
    private double distance;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "stockCategory")
    @JsonDeserialize(using = JsonCustomContactFieldDeserializer.class)
    @JsonSerialize(using = JsonCustomFieldSerializer.class)
    private List<CustomStockCategoryField> customFields;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "stockCategory", targetEntity = StockItem.class)
    private List<StockItem> stockItems;

    public List<CustomStockCategoryField> getCustomFields() {
        if (customFields == null) {
            customFields = new ArrayList<CustomStockCategoryField>();
        }
        return customFields;
    }

    public void setCustomFields(List<CustomStockCategoryField> fields) {
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

    public void addCustomField(CustomStockCategoryField customField) {
        getCustomFields().add(customField);
    }

    public void setField(String key, Object value) {
        getCustomFields().add(
                new CustomStockCategoryField(key, value == null ? null : value
                        .toString()));
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

    public String getTypes() {
        if (stockItems == null) {
            return null;
        }
        List<String> types = new ArrayList<String>();
        for (StockItem stockItem : stockItems) {
            if (stockItem != null && !types.contains(stockItem.getType())) {
                types.add(stockItem.getType());
            }
        }
        StringBuilder sb = new StringBuilder();
        for (String stockItem : types) {
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
        return new GeoPoint(getLat(), getLng());
    }

    public void setGeoPoint(GeoPoint point) {
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
