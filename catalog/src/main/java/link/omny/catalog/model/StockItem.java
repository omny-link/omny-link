package link.omny.catalog.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.annotation.Nonnull;
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
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlElement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.hateoas.Link;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import link.omny.catalog.json.JsonCustomStockItemFieldDeserializer;
import link.omny.catalog.model.api.ShortStockItem;
import link.omny.catalog.views.OrderViews;
import link.omny.catalog.views.StockCategoryViews;
import link.omny.catalog.views.StockItemViews;
import link.omny.custmgmt.json.JsonCustomFieldSerializer;
import link.omny.custmgmt.model.CustomField;
import link.omny.custmgmt.model.Document;
import link.omny.custmgmt.model.Note;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

// Property in Flexspace terminology 
@Data
@ToString(exclude = { "sizeString" })
@Entity
@Table(name = "OL_STOCK_ITEM")
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class StockItem implements ShortStockItem, Serializable {

    private static final long serialVersionUID = 6825862597448133674L;

    protected static final Logger LOGGER = LoggerFactory
            .getLogger(StockItem.class);

    public static final int DEFAULT_IMAGE_COUNT = 4;

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    @JsonProperty
    @JsonView({OrderViews.Summary.class, StockCategoryViews.Detailed.class,
            StockItemViews.Detailed.class})
    private Long id;

    @JsonProperty
    @JsonView({ OrderViews.Summary.class, StockCategoryViews.Detailed.class,
            StockItemViews.Detailed.class })
    @Column(unique = true)
    @NotNull
    private String name;

    @JsonProperty
    @JsonView({ StockCategoryViews.Detailed.class,
        StockItemViews.Detailed.class })
    private String description;

    @JsonProperty
    @JsonView({ StockCategoryViews.Detailed.class,
        StockItemViews.Detailed.class })
    private String size;

    @JsonProperty
        @JsonView({ StockCategoryViews.Detailed.class,
            StockItemViews.Detailed.class })
    @Transient
    private String sizeString;

    @JsonProperty
        @JsonView({ StockCategoryViews.Detailed.class,
            StockItemViews.Detailed.class })
    private String unit;

    @JsonProperty
    @JsonView(StockItemViews.Summary.class)
    private BigDecimal price;

    /**
     * Comma separated set of tags for the item.
     */
    @JsonProperty
    @JsonView({ StockCategoryViews.Detailed.class,
        StockItemViews.Detailed.class })
    private String tags;

    @JsonProperty
    @JsonView({ StockCategoryViews.Detailed.class, StockItemViews.Detailed.class })
    @Column(name = "video_code")
    private String videoCode;

    @JsonProperty
    @JsonView({ OrderViews.Summary.class, StockCategoryViews.Detailed.class,
        StockItemViews.Detailed.class })
    private String status;

    @JsonProperty
    @JsonView({ StockCategoryViews.Detailed.class, StockItemViews.Detailed.class })
    @Size(max = 20)
    @Column(name = "offer_status")
    private String offerStatus;

    @JsonProperty
    @JsonView({ StockCategoryViews.Detailed.class, StockItemViews.Detailed.class })
    @Size(max = 35)
    @Column(name = "offer_title")
    private String offerTitle;

    @JsonProperty
    @JsonView({ StockCategoryViews.Detailed.class, StockItemViews.Detailed.class })
    @Size(max = 80)
    @Column(name = "offer_desc")
    private String offerDescription;

    @JsonProperty
    @JsonView({ StockCategoryViews.Detailed.class, StockItemViews.Detailed.class })
    @Size(max = 30)
    @Column(name = "offer_cta")
    private String offerCallToAction;

    /**
     * A relative or absolute URL associated with the offer, i.e. where it goes
     * to on click.
     */
    @JsonProperty
    @JsonView({ StockCategoryViews.Detailed.class, StockItemViews.Detailed.class })
    @Column(name = "offer_url")
    private String offerUrl;

    @Temporal(TemporalType.TIMESTAMP)
    // Since this is SQL 92 it should be portable
    @Column(columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP", updatable = false)
    @JsonProperty
    @JsonView({ StockCategoryViews.Detailed.class, StockItemViews.Detailed.class })
    private Date created;

    @Temporal(TemporalType.TIMESTAMP)
    @JsonView({ OrderViews.Summary.class, StockCategoryViews.Detailed.class,
        StockItemViews.Detailed.class })
    @Column(name = "last_updated")
    private Date lastUpdated;

    @JsonProperty
    @JsonView({ StockCategoryViews.Detailed.class,
        StockItemViews.Detailed.class })
    @Column(name = "tenant_id")
    private String tenantId;

    @ManyToOne(optional = true, targetEntity = StockCategory.class)
    @JoinColumn(name = "stock_cat_id")
    @JsonView({ StockItemViews.Detailed.class })
    private StockCategory stockCategory;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "stock_item_id")
    @JsonView({ StockItemViews.Detailed.class })
    private List<Note> notes;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "stock_item_id")
    @JsonView({ StockItemViews.Detailed.class })
    private List<Document> documents;

    @JsonProperty
    @JsonView({ StockCategoryViews.Detailed.class, StockItemViews.Detailed.class })
    @Transient
    // @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy =
    // "stockItem")
    private List<MediaResource> images;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "stockItem")
    @JsonDeserialize(using = JsonCustomStockItemFieldDeserializer.class)
    @JsonSerialize(using = JsonCustomFieldSerializer.class)
    @JsonView({ StockCategoryViews.Detailed.class, StockItemViews.Detailed.class })
    private List<CustomStockItemField> customFields;

    @Transient
    @XmlElement(name = "link", namespace = Link.ATOM_NAMESPACE)
    @JsonProperty("links")
    @JsonView({ StockCategoryViews.Detailed.class, StockItemViews.Detailed.class })
    private List<Link> links;

    public static final int CURRENCY_SCALE = 2;

    public StockItem(String name, String tag) {
        this();
        setName(name);
        addTag(tag);
    }

    public StockItem(String name, String tag, String status) {
        this(name, tag);
        setStatus(status);
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
        for (CustomStockItemField newField : fields) {
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

    public void addCustomField(CustomStockItemField customField) {
        customField.setStockItem(this);
        if (getCustomFields().contains(customField)) {
            LOGGER.warn(String
                    .format("Ignoring request to add %1$s as it already exists in the list",
                            customField));
        } else {
            getCustomFields().add(customField);
        }
    }

    protected void setCustomField(CustomStockItemField newField) {
        boolean found = false; 
        for (CustomStockItemField field : getCustomFields()) {
            if (field.getName().equals(newField.getName())) {
                field.setValue(newField.getValue() == null ? null : newField
                        .getValue().toString());
                found= true;
            }
        }
        if (!found) {
            newField.setStockItem(this);
            getCustomFields().add(newField);
        }
    }

    @JsonIgnore
    public List<String> getTagsAsList() {
        if (tags == null) {
            return Collections.emptyList();
        } else {
            return Arrays.asList(tags.split(","));
        }
    }

    @JsonIgnore
    public String getPrimeTag() {
        List<String> tagsAsList = getTagsAsList();
        if (tagsAsList.size() == 0) {
            return null;
        } else {
            return tagsAsList.get(0);
        }
    }

    public void addTag(@Nonnull String tag) {
        if (tags == null) {
            tags = tag;
        } else {
            tags += ("," + tag);
        }
    }

    public List<MediaResource> getImages() {
        if (images == null || images.size() == 0) {
            if (tags == null) {
                LOGGER.warn(
                        "Cannot provide default images because stock item {} ({}) of {} has no tags",
                        name, id, tenantId);
                images = Collections.emptyList();
            } else if (getStockCategory() == null) {
                LOGGER.warn(
                        "Cannot provide default images because stock item {} ({}) of {} has no category",
                        name, id, tenantId);
            } else {
                images = new ArrayList<MediaResource>(DEFAULT_IMAGE_COUNT);
                for (int i = 0; i < DEFAULT_IMAGE_COUNT; i++) {
                    images.add(new MediaResource(getTenantId(), String.format(
                            "/images/%1$s/%2$s/%3$d.jpg",
                            getStockCategory().getName().toLowerCase()
                                    .replaceAll(" ", "_"),
                            getPrimeTag().toLowerCase()
                                    .replaceAll(" ", "_"),
                            i + 1)));
                }
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

    public boolean isPublished() {
        return "Published".equalsIgnoreCase(status);
    }

    @PreUpdate
    public void preUpdate() {
        lastUpdated = new Date();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        StockItem other = (StockItem) obj;
        if (created == null) {
            if (other.created != null)
                return false;
        } else if (!created.equals(other.created))
            return false;
        if (customFields == null) {
            if (other.customFields != null)
                return false;
        } else if (!customFields.equals(other.customFields))
            return false;
        if (description == null) {
            if (other.description != null)
                return false;
        } else if (!description.equals(other.description))
            return false;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        if (images == null) {
            if (other.images != null)
                return false;
        } else if (!images.equals(other.images))
            return false;
        if (lastUpdated == null) {
            if (other.lastUpdated != null)
                return false;
        } else if (!lastUpdated.equals(other.lastUpdated))
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (price == null) {
            if (other.price != null)
                return false;
        } else if (!price.equals(other.price))
            return false;
        if (size == null) {
            if (other.size != null)
                return false;
        } else if (!size.equals(other.size))
            return false;
        if (status == null) {
            if (other.status != null)
                return false;
        } else if (!status.equals(other.status))
            return false;
        if (tenantId == null) {
            if (other.tenantId != null)
                return false;
        } else if (!tenantId.equals(other.tenantId))
            return false;
        if (tags == null) {
            if (other.tags != null)
                return false;
        } else if (!tags.equals(other.tags))
            return false;
        if (unit == null) {
            if (other.unit != null)
                return false;
        } else if (!unit.equals(other.unit))
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((created == null) ? 0 : created.hashCode());
        result = prime * result
                + ((customFields == null) ? 0 : customFields.hashCode());
        result = prime * result
                + ((description == null) ? 0 : description.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((images == null) ? 0 : images.hashCode());
        result = prime * result
                + ((lastUpdated == null) ? 0 : lastUpdated.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((price == null) ? 0 : price.hashCode());
        result = prime * result + ((size == null) ? 0 : size.hashCode());
        result = prime * result + ((status == null) ? 0 : status.hashCode());
        result = prime * result
                + ((tenantId == null) ? 0 : tenantId.hashCode());
        result = prime * result + ((tags == null) ? 0 : tags.hashCode());
        result = prime * result + ((unit == null) ? 0 : unit.hashCode());
        return result;
    }

}
