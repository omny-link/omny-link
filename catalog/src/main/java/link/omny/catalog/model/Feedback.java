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
import javax.persistence.OneToOne;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlElement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.hateoas.Link;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import link.omny.catalog.json.JsonCustomFeedbackFieldDeserializer;
import link.omny.catalog.views.FeedbackViews;
import link.omny.catalog.views.OrderViews;
import link.omny.custmgmt.json.JsonCustomFieldSerializer;
import link.omny.custmgmt.model.CustomField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@JsonIgnoreProperties(allowGetters = true, value = { "selfRef" })
@Data
@EqualsAndHashCode(exclude = { "links", "order" })
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
    @GeneratedValue(strategy = GenerationType.AUTO)
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
    @JsonView(OrderViews.Detailed.class)
    private Date created;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="last_updated")
    @JsonProperty
    // See OrderItem.lastUpdated for explanation
    //@JsonView(OrderViews.Detailed.class)
    private Date lastUpdated;

    @Column(name="tenant_id")
    @JsonProperty
    @JsonView(OrderViews.Detailed.class)
    private String tenantId;

    @OneToOne
    @RestResource(rel = "order")
    private Order order;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "feedback", orphanRemoval = true)
    @JsonDeserialize(using = JsonCustomFeedbackFieldDeserializer.class)
    @JsonSerialize(using = JsonCustomFieldSerializer.class)
    @JsonView(OrderViews.Detailed.class)
    private List<CustomFeedbackField> customFields;

    @Transient
    @XmlElement(name = "link", namespace = Link.ATOM_NAMESPACE)
    @JsonProperty("links")
    @JsonView({ OrderViews.Detailed.class, FeedbackViews.Summary.class })
    private List<Link> links;

    public Feedback(String desc, String type) {
        this();
        setDescription(desc);
        setType(type);
    }

    public String getSelfRef() {
        return String.format("/order-items/%1$d", id);
    }

    public List<CustomFeedbackField> getCustomFields() {
        if (customFields == null) {
            customFields = new ArrayList<CustomFeedbackField>();
        }
        return customFields;
    }

    public void setCustomFields(List<CustomFeedbackField> fields) {
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
