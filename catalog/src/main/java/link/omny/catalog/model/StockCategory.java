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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.NamedAttributeNode;
import jakarta.persistence.NamedEntityGraph;
import jakarta.persistence.NamedSubgraph;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.SecondaryTable;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import link.omny.catalog.json.JsonCustomStockCategoryFieldDeserializer;
import link.omny.catalog.model.api.ShortStockCategory;
import link.omny.catalog.views.StockCategoryViews;
import link.omny.catalog.views.StockItemViews;
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
@NamedEntityGraph(name = "stockCategoryWithAll",
    attributeNodes = {
        @NamedAttributeNode(value = "stockItems", subgraph = "item-subgraph"),
        @NamedAttributeNode("customFields"),
        @NamedAttributeNode("notes"),
        @NamedAttributeNode("documents")
    },
    subgraphs = {
        @NamedSubgraph(
                name = "item-subgraph",
                attributeNodes = { @NamedAttributeNode("customFields") }
        )
    }
)
@Table(name = "OL_STOCK_CAT")
@SecondaryTable(name = "OL_STOCK_CAT_CUSTOM",
    pkJoinColumns = @PrimaryKeyJoinColumn(name = "stock_cat_id"))
@Data
@EqualsAndHashCode(callSuper = true, exclude = "tags")
@ToString(exclude = { "description", "stockItems" })
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class StockCategory extends Auditable<String> implements ShortStockCategory, Serializable {

    public static final int DEFAULT_IMAGE_COUNT = 8;

    private static final long serialVersionUID = -6115608228931780960L;

    protected static final Logger LOGGER = LoggerFactory
            .getLogger(StockCategory.class);

    @Id
    @Column(name = "id")
    @SequenceGenerator(name = "stockCategoryIdSeq", sequenceName = "ol_stock_cat_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "stockCategoryIdSeq")
    @JsonProperty
    @JsonView({StockCategoryViews.Summary.class, StockItemViews.Detailed.class})
    private Long id;

    @JsonProperty
    @JsonView({StockCategoryViews.Summary.class, StockItemViews.Detailed.class})
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
    @JsonView({StockCategoryViews.Summary.class, StockItemViews.Detailed.class})
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
    private Set<Note> notes;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "stock_cat_id", nullable = true)
    @JsonView({ StockCategoryViews.Detailed.class })
    private Set<Document> documents;

    @JsonProperty
    @JsonView(StockCategoryViews.Summary.class)
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
    private Set<CustomStockCategoryField> customFields;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "stockCategory", targetEntity = StockItem.class)
    @JsonBackReference
    @JsonView(StockCategoryViews.Detailed.class)
    private Set<StockItem> stockItems;

    @Transient
    private List<String> customHeadings;

    public StockCategory(String name) {
        this();
        setName(name);
    }

    public Set<CustomStockCategoryField> getCustomFields() {
        if (customFields == null) {
            customFields = new HashSet<CustomStockCategoryField>();
        }
        return customFields;
    }

    public void setCustomFields(Set<CustomStockCategoryField> fields) {
        for (CustomStockCategoryField newField : fields) {
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

    public Set<StockItem> getStockItems() {
        if (stockItems == null) {
            stockItems = new HashSet<StockItem>();
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
