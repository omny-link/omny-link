package link.omny.catalog.model;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlElement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.hateoas.Link;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;

import link.omny.catalog.views.MediaResourceViews;
import link.omny.catalog.views.StockCategoryViews;
import link.omny.catalog.views.StockItemViews;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Table(name = "OL_MEDIA_RES")
@AllArgsConstructor
@NoArgsConstructor
public class MediaResource implements Serializable {

    private static final long serialVersionUID = 157180600778360331L;

    protected static final Logger LOGGER = LoggerFactory
            .getLogger(MediaResource.class);

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    @JsonProperty
    private Long id;

    @JsonProperty
    @JsonView({ MediaResourceViews.Summary.class,
        StockCategoryViews.Detailed.class, StockItemViews.Detailed.class })
    private boolean main;

    @JsonProperty
    @JsonView({ MediaResourceViews.Summary.class,
        StockCategoryViews.Detailed.class, StockItemViews.Detailed.class })
    private String author;

    @JsonProperty
    @JsonView({ MediaResourceViews.Summary.class,
        StockCategoryViews.Detailed.class, StockItemViews.Detailed.class })
    @Temporal(TemporalType.TIMESTAMP)
    private Date created;

    @Temporal(TemporalType.TIMESTAMP)
    @JsonProperty
    @JsonView({ MediaResourceViews.Summary.class,
        StockCategoryViews.Detailed.class, StockItemViews.Detailed.class })
    @Column(name = "last_updated")
    private Date lastUpdated;

    @JsonProperty
    @JsonView({ MediaResourceViews.Summary.class,
        StockCategoryViews.Detailed.class, StockItemViews.Detailed.class })
    private String url;

    @ManyToOne(targetEntity = StockCategory.class)
    @JoinColumn(name = "stock_cat_id")
    @RestResource(rel = "media")
    private StockCategory stockCategory;

    @ManyToOne(targetEntity = StockItem.class)
    @JoinColumn(name = "stock_item_id")
    @RestResource(rel = "media")
    private StockItem stockItem;

    @Transient
    @XmlElement(name = "link", namespace = Link.ATOM_NAMESPACE)
    @JsonProperty("links")
    @JsonView(MediaResourceViews.Summary.class)
    private List<Link> links;

    public MediaResource(String author, String url) {
        setAuthor(author);
        setUrl(url);
    }

    @PrePersist
    void preInsert() {
        created = new Date();
    }

    @PreUpdate
    public void preUpdate() {
        lastUpdated = new Date();
    }

    @Override
    public String toString() {
        return String.format(
                "MediaResource [id=%s, author=%s, created=%s, url=%s]",
                id, author, created, url);
    }
}
