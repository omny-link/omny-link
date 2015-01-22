package link.omny.custmgmt.model;

import java.io.Serializable;
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
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonProperty;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Contact implements Serializable {

    private static final long serialVersionUID = -6080589981067789428L;

    protected static final Logger LOGGER = LoggerFactory
            .getLogger(Contact.class);

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    @JsonProperty
    private Long id;

    /**
     */
    @NotNull
    @JsonProperty
    @Column(nullable = false)
    private String firstName;

    /**
     */
    @NotNull
    @JsonProperty
    @Column(nullable = false)
    private String lastName;

    /**
     */
    @JsonProperty
    private String title;

    /**
     */
    @NotNull
    @JsonProperty
    @Column(nullable = false)
    private String email;

    /**
     */
    @Pattern(regexp = "[0-9, ]{0,13}")
    @JsonProperty
    private String landLine;

    /**
     */
    @Pattern(regexp = "[0-9, ]{0,13}")
    @JsonProperty
    private String mobile;

    @JsonProperty
    private String address1;

    @JsonProperty
    private String address2;

    @JsonProperty
    private String countyOrCity;

    @JsonProperty
    private String postCode;

    @JsonProperty
    private String enquiryType;

    @JsonProperty
    private float budget;

    @JsonProperty
    private String stage;

	@JsonProperty
	private String contact;

    /**
     * Intended to capture the source of the lead from Analytics.
     */
    @JsonProperty
    private String source;

    /**
     * Intended to capture the medium of the lead from Analytics.
     */
    @JsonProperty
    private String medium;

    /**
     */
    @Temporal(TemporalType.TIMESTAMP)
    @DateTimeFormat(style = "M-")
    @JsonProperty
    private Date firstContact;

    /**
     */
    @Temporal(TemporalType.TIMESTAMP)
    @DateTimeFormat(style = "M-")
    @JsonProperty
    private Date lastUpdated;

    /**
     */
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private AccountInfo accountInfo;

    /**
     */
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "contact")
    private List<CustomField> customFields;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "contact")
    private List<Note> notes;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "contact")
    private List<Document> documents;

    @PrePersist
    void preInsert() {
        firstContact = new Date();
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
