package link.omny.custmgmt.model;

import java.io.Serializable;
import java.text.DecimalFormat;
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
import javax.persistence.Lob;
import javax.persistence.OneToMany;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import link.omny.custmgmt.json.JsonCustomAccountFieldDeserializer;
import link.omny.custmgmt.json.JsonCustomFieldSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "OL_ACCOUNT")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Account implements Serializable {

    private static final long serialVersionUID = -1955316248920138892L;

    protected static final Logger LOGGER = LoggerFactory
            .getLogger(Account.class);

    protected static DecimalFormat decimalFormat = new DecimalFormat("#.00");

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
     * <li>UK company numbers are 7 or 8 digits (Companies House has started
     * left zero padding).
     * <li>Scottish company numbers are SC then 6 digits.
     * <li>UK LLP numbers are OC then 6 digits.
     * <li>Northern Irish numbers are NI then 6 digits.
     * <li>Edubase URNs are 6 digits.
     */
    @Pattern(regexp = "[0-9OSN]?[0-9CI]?[0-9]{5,6}")
    @JsonProperty
    private String companyNumber;

    @JsonProperty
    private String sic;

    @JsonProperty
    private String aliases;

    @JsonProperty
    private String businessWebsite;

    /**
     */
    @JsonProperty
    private String email;

    @JsonProperty
    private boolean emailConfirmed;

    @JsonProperty
    private String emailHash;

    /**
     */
    @Pattern(regexp = "\\+?[0-9, \\-()]{0,15}")
    @JsonProperty
    private String phone1;

    /**
     */
    @Pattern(regexp = "\\+?[0-9, \\-()]{0,15}")
    @JsonProperty
    private String phone2;

    @JsonProperty
    private String address1;

    @JsonProperty
    private String address2;

    @JsonProperty
    private String town;

    @JsonProperty
    private String countyOrCity;

    @JsonProperty
    private String postCode;

    @JsonProperty
    private String country;

    @JsonProperty
    private String twitter;

    @JsonProperty
    private String facebook;

    @JsonProperty
    private String linkedIn;

    @Size(max = 120)
    @JsonProperty
    private String shortDesc;

    @JsonProperty
    @Lob
    private String description;

    @Digits(integer = 4, fraction = 0)
    @JsonProperty
    private Integer incorporationYear;

    @Size(max = 20)
    private String noOfEmployees;

    @JsonProperty
    private boolean existingCustomer;

    @JsonProperty
    private String stage;

    @JsonProperty
    private String stageReason;

    @JsonProperty
    private Date stageDate;

    @JsonProperty
    private String enquiryType;

    @JsonProperty
    private String accountType;

    @JsonProperty
    private String owner;

    /**
     * Comma-separated set of alerts for the contact.
     */
    @JsonProperty
    private String alerts;

    /**
     * Comma-separated set of arbitrary tags for the contact.
     */
    @JsonProperty
    private String tags;

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

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "account", targetEntity = CustomAccountField.class)
    @JsonDeserialize(using = JsonCustomAccountFieldDeserializer.class)
    @JsonSerialize(using = JsonCustomFieldSerializer.class)
    private List<CustomAccountField> customFields;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "account")
    private List<Note> notes;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "account")
    private List<Activity> activity;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "account")
    private List<Document> documents;

    public List<CustomAccountField> getCustomFields() {
        if (customFields == null) {
            customFields = new ArrayList<CustomAccountField>();
        }
        return customFields;
    }

    public void setCustomFields(List<CustomAccountField> fields) {
        for (CustomAccountField newField : fields) {
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

    public void addCustomField(CustomAccountField customField) {
        customField.setAccount(this);
        getCustomFields().add(customField);
    }

    protected void setCustomField(CustomAccountField newField) {
        try {
            double d = Double.parseDouble(newField.getValue());
            newField.setValue(decimalFormat.format(d));
        } catch (NumberFormatException e) {
            // that's ok, continue as string
        }

        boolean found = false;
        for (CustomAccountField field : getCustomFields()) {
            if (field.getName().equals(newField.getName())) {
                field.setValue(newField.getValue() == null ? null : newField
                        .getValue().toString());
                found = true;
            }
        }
        if (!found) {
            newField.setAccount(this);
            getCustomFields().add(newField);
        }
    }

    @JsonProperty
    public Long getAccountId() {
        return getId();
    }

    public String getCompanyNumber() {
        if (companyNumber != null && companyNumber.trim().length() == 0) {
            companyNumber = null;
        }else if (companyNumber != null && companyNumber.length() < 8) {
            companyNumber = String.format("%08d",
                    Integer.parseInt(companyNumber));
        }
        return companyNumber;
    }

    @JsonProperty
    public String getAddress() {
        StringBuilder sb = new StringBuilder();
        if (address1 != null && address1.length()>0) {
            sb.append(address1).append(", ");
        }
        if (address2 != null && address2.length()>0) {
            sb.append(address2).append(", ");
        }
        if (town != null && town.length()>0) {
            sb.append(town).append(", ");
        }
        if (countyOrCity != null && countyOrCity.length()>0) {
            sb.append(countyOrCity).append(", ");
        }
        if (postCode != null && postCode.length()>0) {
            sb.append(postCode).append(". ");
        }
        if (country != null && country.length()>0) {
            sb.append(country).append(".");
        }
        return sb.toString();
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
