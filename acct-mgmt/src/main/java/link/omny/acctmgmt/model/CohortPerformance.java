package link.omny.acctmgmt.model;

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
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import link.omny.custmgmt.json.JsonCustomContactFieldDeserializer;
import link.omny.custmgmt.json.JsonCustomFieldSerializer;
import link.omny.custmgmt.model.CustomField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * A picture of performance for a cohort across a number of metrics.
 *
 * @author Tim Stephenson
 */
@Entity
@Table(name = "OL_COHORT_PERF")
@Data
@EqualsAndHashCode(exclude = {})
@AllArgsConstructor
@NoArgsConstructor
public class CohortPerformance implements Serializable {

    private static final long serialVersionUID = -732569279016147577L;

    protected static final Logger LOGGER = LoggerFactory
            .getLogger(CohortPerformance.class);

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    @JsonProperty
    private Long id;

    @JsonProperty
    @Column(name = "subject_name")
    private String subjectName;

    @JsonProperty
    @Column(name = "subject_email")
    private String subjectEmail;

    @Pattern(regexp = "\\+?[0-9, \\-()]{0,15}")
    @JsonProperty
    @Column(name = "subject_phone")
    private String subjectPhone;

    @JsonProperty
    private String tags;

    @JsonProperty
    @Size(min = 0, max = 255)
    protected String description;

    /**
     * Time the performance measurement applies to.
     */
    @Temporal(TemporalType.TIMESTAMP)
    @JsonProperty
    private Date occurred;

    /**
     * The time the survey is created (may or may not be the same as the time
     * the performance applies to).
     *
     * Generally this field is managed by the application but this is not
     * rigidly enforced as exceptions such as data migration do exist.
     */
    @Temporal(TemporalType.TIMESTAMP)
    // Since this is SQL 92 it should be portable
    @Column(columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP", updatable = false)
    @JsonProperty
    private Date created;

    @Temporal(TemporalType.TIMESTAMP)
    @JsonProperty
    private Date lastUpdated;

    @NotNull
    @JsonProperty
    @Column(nullable = false)
    private String tenantId;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "cohort", targetEntity = CustomCohortField.class)
    @JsonDeserialize(using = JsonCustomContactFieldDeserializer.class)
    @JsonSerialize(using = JsonCustomFieldSerializer.class)
    private List<CustomCohortField> customFields;

    public List<CustomCohortField> getCustomFields() {
        if (customFields == null) {
            customFields = new ArrayList<CustomCohortField>();
        }
        return customFields;
    }

    public void setCustomFields(List<CustomCohortField> fields) {
        for (CustomCohortField newField : fields) {
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

    public void addCustomField(CustomCohortField customField) {
        customField.setCohort(this);
        getCustomFields().add(customField);
    }

    protected void setCustomField(CustomCohortField newField) {
        boolean found = false;
        for (CustomCohortField field : getCustomFields()) {
            if (field.getName().equals(newField.getName())) {
                field.setValue(newField.getValue() == null ? null : newField
                        .getValue().toString());
                found = true;
            }
        }
        if (!found) {
            newField.setCohort(this);
            getCustomFields().add(newField);
        }
    }

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "cohort")
    private List<Metric> metrics;

    public void addMetric(Metric metric) {
        if (getMetrics() == null) {
            setMetrics(new ArrayList<Metric>());
        }
        getMetrics().add(metric);
    }

    @PrePersist
    public void prePersist() {
        if (LOGGER.isWarnEnabled() && created != null) {
            LOGGER.warn(String.format(
                    "Overwriting create date %1$s with 'now'.", created));
        }
        created = new Date();
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
