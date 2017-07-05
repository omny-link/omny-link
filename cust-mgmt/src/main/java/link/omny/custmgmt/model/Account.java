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
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.OneToMany;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlElement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.hateoas.Link;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import link.omny.custmgmt.json.JsonCustomAccountFieldDeserializer;
import link.omny.custmgmt.json.JsonCustomFieldSerializer;
import link.omny.custmgmt.model.views.AccountViews;
import link.omny.custmgmt.model.views.ContactViews;
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
    @JsonView( { AccountViews.Summary.class } )
    private Long id;

    @NotNull
    @JsonProperty
    @JsonView( { AccountViews.Summary.class, ContactViews.Summary.class } )
    @Column(name = "name")
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
    @JsonView( { AccountViews.Detailed.class, ContactViews.Detailed.class } )
    @Column(name = "company_number")
    private String companyNumber;

    @JsonProperty
    @JsonView( { AccountViews.Detailed.class, ContactViews.Detailed.class } )
    @Column(name = "sic")
    private String sic;

    @JsonProperty
    @JsonView( { AccountViews.Detailed.class, ContactViews.Detailed.class } )
    @Column(name = "aliases")
    private String aliases;

    @JsonProperty
    @JsonView( { AccountViews.Detailed.class, ContactViews.Detailed.class } )
    @Column(name = "business_website")
    private String businessWebsite;

    @JsonProperty
    @JsonView( { AccountViews.Summary.class } )
    @Column(name = "email")
    private String email;

    @JsonProperty
    @JsonView( { AccountViews.Detailed.class, ContactViews.Detailed.class } )
    @Column(name = "email_confirmed")
    private boolean emailConfirmed;

    @JsonProperty
    @JsonView( { AccountViews.Detailed.class, ContactViews.Detailed.class } )
    @Column(name = "email_hash")
    private String emailHash;

    @Pattern(regexp = "\\+?[0-9, \\-()]{0,15}")
    @JsonProperty
    @JsonView( { AccountViews.Summary.class } )
    @Column(name = "phone1")
    private String phone1;

    @Pattern(regexp = "\\+?[0-9, \\-()]{0,15}")
    @JsonProperty
    @JsonView( { AccountViews.Detailed.class, ContactViews.Detailed.class } )
    @Column(name = "phone2")
    private String phone2;

    @JsonProperty
    @JsonView( { AccountViews.Detailed.class, ContactViews.Detailed.class } )
    @Column(name = "address1")
    private String address1;

    @JsonProperty
    @JsonView( { AccountViews.Detailed.class, ContactViews.Detailed.class } )
    @Column(name = "address2")
    private String address2;

    @JsonProperty
    @JsonView( { AccountViews.Detailed.class, ContactViews.Detailed.class } )
    @Column(name = "town")
    private String town;

    @JsonProperty
    @JsonView( { AccountViews.Detailed.class, ContactViews.Detailed.class } )
    @Column(name = "county_or_city")
    private String countyOrCity;

    @JsonProperty
    @JsonView( { AccountViews.Detailed.class, ContactViews.Detailed.class } )
    @Column(name = "post_code")
    private String postCode;

    @JsonProperty
    @JsonView( { AccountViews.Detailed.class, ContactViews.Detailed.class } )
    @Column(name = "country")
    private String country;

    @JsonProperty
    @JsonView( { AccountViews.Detailed.class, ContactViews.Detailed.class } )
    @Column(name = "twitter")
    private String twitter;

    @JsonProperty
    @JsonView( { AccountViews.Detailed.class, ContactViews.Detailed.class } )
    @Column(name = "facebook")
    private String facebook;

    @JsonProperty
    @JsonView( { AccountViews.Detailed.class, ContactViews.Detailed.class } )
    @Column(name = "linked_in")
    private String linkedIn;

    @Size(max = 120)
    @JsonProperty
    @JsonView( { AccountViews.Detailed.class, ContactViews.Detailed.class } )
    @Column(name = "short_desc")
    private String shortDesc;

    @JsonProperty
    @JsonView( { AccountViews.Detailed.class, ContactViews.Detailed.class } )
    @Lob
    @Column(name = "description")
    private String description;

    @Digits(integer = 4, fraction = 0)
    @JsonProperty
    @JsonView( { AccountViews.Detailed.class, ContactViews.Detailed.class } )
    @Column(name = "incorporation_year")
    private Integer incorporationYear;

    @Size(max = 20)
    @Column(name = "no_of_employees")
    private String noOfEmployees;

    @JsonProperty
    @JsonView( { AccountViews.Detailed.class, ContactViews.Detailed.class } )
    @Column(name = "existing_customer")
    private boolean existingCustomer;

    @JsonProperty
    @JsonView( { AccountViews.Summary.class } )
    @Column(name = "stage")
    private String stage;

    @JsonProperty
    @JsonView( { AccountViews.Detailed.class, ContactViews.Detailed.class } )
    @Column(name = "stage_reason")
    private String stageReason;

    @JsonProperty
    @JsonView( { AccountViews.Detailed.class, ContactViews.Detailed.class } )
    @Column(name = "stage_date")
    private Date stageDate;

    @JsonProperty
    @JsonView( { AccountViews.Summary.class } )
    @Column(name = "enquiry_type")
    private String enquiryType;

    @JsonProperty
    @JsonView( { AccountViews.Summary.class } )
    @Column(name = "account_type")
    private String accountType;

    @JsonProperty
    @JsonView( { AccountViews.Summary.class } )
    @Column(name = "owner")
    private String owner;

    /**
     * Comma-separated set of alerts for the contact.
     */
    @JsonProperty
    @JsonView( { AccountViews.Summary.class } )
    @Column(name = "alerts")
    private String alerts;

    /**
     * Comma-separated set of arbitrary tags for the contact.
     */
    @JsonProperty
    @JsonView( { AccountViews.Summary.class } )
    @Column(name = "tags")
    private String tags;

    @JsonProperty
    @JsonView( { AccountViews.Detailed.class, ContactViews.Detailed.class } )
    @Column(name = "parent_org")
    private String parentOrg;

    @NotNull
    @JsonProperty
    @JsonView( { AccountViews.Summary.class } )
    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @Temporal(TemporalType.TIMESTAMP)
    // Since this is SQL 92 it should be portable
    @Column(columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP", name = "first_contact", updatable = false)
    @JsonProperty
    @JsonView( { AccountViews.Summary.class } )
    private Date firstContact;

    @Temporal(TemporalType.TIMESTAMP)
    @JsonProperty
    @JsonView( { AccountViews.Summary.class } )
    @Column(name = "last_updated")
    private Date lastUpdated;

    @Transient
    @XmlElement(name = "link", namespace = Link.ATOM_NAMESPACE)
    @JsonProperty("links")
    @JsonView({ AccountViews.Summary.class })
    private List<Link> links;

    @JsonProperty
    @JsonView({ AccountViews.Summary.class })
    public String getSelfRef() {
        return id == null ? null
                : String.format("/%1$s/accounts/%2$d", tenantId, id);
    }

    @OneToMany(mappedBy = "account", targetEntity = Contact.class)
    // TODO See Contact.account for details of limitation
    // @JsonBackReference
    private List<Contact> contact;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "account", targetEntity = CustomAccountField.class)
    @JsonDeserialize(using = JsonCustomAccountFieldDeserializer.class)
    @JsonSerialize(using = JsonCustomFieldSerializer.class)
    @JsonView({ AccountViews.Detailed.class })
    private List<CustomAccountField> customFields;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "account_id")
    @JsonView({ AccountViews.Detailed.class })
    private List<Note> notes;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "account_id")
    @JsonView({ AccountViews.Detailed.class })
    private List<Activity> activities;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "account_id")
    @JsonView({ AccountViews.Detailed.class })
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
            if (newField.getValue().toLowerCase().contains("e")) {
                newField.setValue(decimalFormat.format(d));
            }
        } catch (NumberFormatException e) {
            // that's ok, continue as string
        }
        if (newField == null || newField.getValue() == null
                || newField.getValue().trim().length() == 0) {
            return;
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
    @JsonView( { AccountViews.Detailed.class, ContactViews.Detailed.class } )
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
    @JsonView( { AccountViews.Detailed.class, ContactViews.Detailed.class } )
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
