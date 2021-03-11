/*******************************************************************************
 *Copyright 2015-2018 Tim Stephenson and contributors
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
import java.util.Date;

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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.rest.core.annotation.RestResource;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;

import link.omny.catalog.views.MediaResourceViews;
import link.omny.catalog.views.StockCategoryViews;
import link.omny.catalog.views.StockItemViews;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Data
@Table(name = "OL_MEDIA_RES")
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = { "stockCategory", "stockItem" })
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
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @JsonView({ MediaResourceViews.Summary.class,
        StockCategoryViews.Detailed.class, StockItemViews.Detailed.class })
    @Temporal(TemporalType.TIMESTAMP)
    private Date created;

    @Temporal(TemporalType.TIMESTAMP)
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
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

}
