package link.omny.acctmgmt.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Encapsulate a single metric (key performance indicator) for a tenant.
 *
 * @author Tim Stephenson
 */
@Data
@Entity
@Table(name = "OL_METRIC")
@NoArgsConstructor
@AllArgsConstructor
public class Metric implements Serializable {

    private static final long serialVersionUID = 3351879795834155373L;

    protected static final Logger LOGGER = LoggerFactory
            .getLogger(Metric.class);
    
    @Id
    @GeneratedValue
    protected Long id;

    @NotNull
    @JsonProperty
    protected String tenantId;

    @NotNull
    @JsonProperty
    protected String name;
    
    @NotNull
    @JsonProperty
    protected Long value;

    @JsonProperty
    @Size(min = 0, max = 255)
    protected String description;

    @JsonProperty
    protected String category1;

    @JsonProperty
    protected String category2;

    @NotNull
    @Temporal(TemporalType.TIMESTAMP)
    // Since this is SQL 92 it should be portable
    @Column(columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP", updatable = false)
    @JsonProperty
    protected Date occurred;

    @ManyToOne(optional = false, targetEntity = CohortPerformance.class)
    private CohortPerformance cohort;

    public Metric(String tenantId, String name, boolean bool) {
        this(tenantId, name, bool ? 0 : -1l);
    }

    public Metric(String tenantId, String name, Long value) {
        setTenantId(tenantId);
        setName(name);
        setValue(value);
    }

    public Metric(String tenantId, String name, Long value, Date occurred) {
        this(tenantId, name, value);
        setOccurred(occurred);
    }

}
