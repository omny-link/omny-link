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
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.PreUpdate;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.rest.core.annotation.RestResource;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import link.omny.catalog.json.JsonCustomFeedbackFieldDeserializer;
import link.omny.catalog.views.OrderViews;
import link.omny.supportservices.json.JsonCustomFieldSerializer;
import link.omny.supportservices.model.CustomField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@EqualsAndHashCode(exclude = { "order" })
@ToString(exclude = { "order" })
@Entity
@Table(name = "OL_FEEDBACK")
@AllArgsConstructor
@NoArgsConstructor
public class Feedback implements Serializable {

    private static final long serialVersionUID = 8577876040188427429L;

    protected static final Logger LOGGER = LoggerFactory
            .getLogger(Feedback.class);

    public static final int DEFAULT_IMAGE_COUNT = 4;

    @Id
    @Column(name = "id")
    @SequenceGenerator(name = "feedbackIdSeq", sequenceName = "ol_feedback_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "feedbackIdSeq")
    @JsonProperty
    @JsonView(OrderViews.Detailed.class)
    private Long id;

    @JsonProperty
    @JsonView(OrderViews.Detailed.class)
    private String description;

    @JsonProperty
    @JsonView(OrderViews.Detailed.class)
    private String type;

    @Temporal(TemporalType.TIMESTAMP)
    // Since this is SQL 92 it should be portable
    @Column(columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP", updatable = false)
    @JsonProperty
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @JsonView(OrderViews.Detailed.class)
    private Date created;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="last_updated")
    @JsonProperty
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    // See OrderItem.lastUpdated for explanation
    //@JsonView(OrderViews.Detailed.class)
    private Date lastUpdated;

    @Column(name="tenant_id")
    @JsonProperty
    @JsonView(OrderViews.Detailed.class)
    private String tenantId;

    @OneToOne
    @RestResource(rel = "order")
    @JsonBackReference
    private Order order;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "feedback", orphanRemoval = true)
    @JsonDeserialize(using = JsonCustomFeedbackFieldDeserializer.class)
    @JsonSerialize(using = JsonCustomFieldSerializer.class)
    @JsonView(OrderViews.Detailed.class)
    private Set<CustomFeedbackField> customFields;

    public Feedback(String desc, String type) {
        this();
        setDescription(desc);
        setType(type);
    }

    public Set<CustomFeedbackField> getCustomFields() {
        if (customFields == null) {
            customFields = new HashSet<CustomFeedbackField>();
        }
        return customFields;
    }

    public void setCustomFields(Set<CustomFeedbackField> fields) {
        for (CustomFeedbackField newField : fields) {
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

    public void addCustomField(CustomFeedbackField customField) {
        customField.setFeedback(this);
        getCustomFields().add(customField);
    }

    protected void setCustomField(CustomFeedbackField newField) {
        boolean found = false; 
        for (CustomFeedbackField field : getCustomFields()) {
            if (field.getName().equals(newField.getName())) {
                field.setValue(newField.getValue() == null ? null : newField
                        .getValue().toString());
                found= true;
            }
        }
        if (!found) {
            newField.setFeedback(this);
            getCustomFields().add(newField);
            lastUpdated = new Date();
        }
    }

    @PreUpdate
    public void preUpdate() {
        lastUpdated = new Date();
    }

}
