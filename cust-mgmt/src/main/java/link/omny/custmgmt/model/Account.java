package link.omny.custmgmt.model;

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
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import link.omny.custmgmt.json.JsonCustomAccountFieldDeserializer;
import link.omny.custmgmt.json.JsonCustomFieldSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@Entity
@Table(name = "OL_ACCOUNT")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Account implements Serializable {

    private static final long serialVersionUID = -1955316248920138892L;

    protected static final Logger LOGGER = LoggerFactory
            .getLogger(Account.class);

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    @JsonProperty
    private Long id;

    /**
     */
    @NotNull
    @JsonProperty
    private String name;

    /**
     * <ul>
     * <li>UK company numbers are 8 digits.
     * <li>UK LLP numbers are OC then 6 digits.
     */
    @Pattern(regexp = "[0-9O][0-9C][0-9]{6}")
    @JsonProperty
    private String companyNumber;

    @JsonProperty
    private String aliases;

    @JsonProperty
    private String businessWebsite;

    /**
     */
    @Size(max = 120)
    @JsonProperty
    private String shortDesc;

    @JsonProperty
    private String description;

    /**
     */
    @Digits(integer = 4, fraction = 0)
    @JsonProperty
    private Integer incorporationYear;

    /**
     */
    @Size(max = 20)
    private String noOfEmployees;

    /**
     */
    @NotNull
    @JsonProperty
    @Column(nullable = false)
    private String tenantId;

    /**
     */
    @Temporal(TemporalType.TIMESTAMP)
    // Since this is SQL 92 it should be portable
    @Column(columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP", updatable = false)
    @JsonProperty
    private Date firstContact;

    /**
     */
    @Temporal(TemporalType.TIMESTAMP)
    @JsonProperty
    private Date lastUpdated;

    @OneToMany(mappedBy = "account", targetEntity = Contact.class)
    // TODO See Contact.account for details of limitation
    // @JsonBackReference
    private List<Contact> contact;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "account", targetEntity = CustomAccountField.class)
    @JsonDeserialize(using = JsonCustomAccountFieldDeserializer.class)
    @JsonSerialize(using = JsonCustomFieldSerializer.class)
    private List<CustomAccountField> customFields;

    public List<CustomAccountField> getCustomFields() {
        if (customFields == null) {
            customFields = new ArrayList<CustomAccountField>();
        }
        return customFields;
    }

    public void setCustomFields(List<CustomAccountField> fields) {
        this.customFields = fields;
        for (CustomAccountField customAccountField : fields) {
            customAccountField.setAccount(this);
        }
        setLastUpdated(new Date());
    }

    public Object getField(@NotNull String fieldName) {
        for (CustomField field : getCustomFields()) {
            if (fieldName.equals(field.getName())) {
                return field.getValue();
            }
        }
        return null;
    }

    public void addCustomField(CustomAccountField customField) {
        getCustomFields().add(customField);
    }

    public void setField(String key, String value) {
        boolean found = false;
        for (CustomAccountField field : getCustomFields()) {
            if (field.getName().equals(key)) {
                field.setValue(value);
                found = true;
                break;
            }
        }
        if (!found) {
            getCustomFields().add(
                new CustomAccountField(key, value == null ? null : value
                        .toString()));
        }
    }

    @PreUpdate
    public void preUpdate() {
        if (LOGGER.isWarnEnabled() && lastUpdated != null) {
            LOGGER.warn(String.format(
                    "Overwriting update date %1$s with 'now'.", lastUpdated));
        }
        lastUpdated = new Date();
    }

    @Override
    public String toString() {
        return String
                .format("Account [id=%s, name=%s, companyNumber=%s, aliases=%s, businessWebsite=%s, shortDesc=%s, description=%s, incorporationYear=%s, noOfEmployees=%s, tenantId=%s, firstContact=%s, lastUpdated=%s, customFields=%s]",
                        id, name, companyNumber, aliases, businessWebsite,
                        shortDesc, description, incorporationYear,
                        noOfEmployees, tenantId, firstContact, lastUpdated,
                        customFields);
    }

}
