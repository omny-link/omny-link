package link.omny.custmgmt.model;

import java.io.Serializable;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents an individual tenant's instance of the Customer Management module.
 * 
 * @author Tim Stephenson
 *
 */
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TenantConfig implements Serializable {

    private static final long serialVersionUID = 4886215941519231066L;

    protected static final Logger LOGGER = LoggerFactory
            .getLogger(TenantConfig.class);

    @Id
    @NotNull
    @JsonProperty
    @Column(nullable = false)
    private String name;

    /**
     * In seconds.
     */
    @JsonProperty
    @Min(0L)
    private Integer sessionTimeout;

    @Embedded
    @JsonProperty
    private Map<String, String> contactFields;

    @Embedded
    @JsonProperty
    private Map<String, String> accountFields;

    /**
     * Map of fieldName to URL containing the drop down values.
     * 
     * <p>
     * e.g. selector: '#curEnquiryType', url:
     * '/data/firmgains/enquiry-types.json'
     */
    @Embedded
    @JsonProperty
    private Map<String, String> comboFields;

}
