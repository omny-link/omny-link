package com.knowprocess.bpm.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlElement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.hateoas.Link;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Contact implements Serializable {

    private static final String DEFAULT_FIRST_NAME = "Unknown";

    private static final long serialVersionUID = -6080589981067789428L;

    protected static final Logger LOGGER = LoggerFactory
            .getLogger(Contact.class);

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    @JsonProperty
    private Long id;

    // @NotNull
    @JsonProperty
    @Column(name = "first_name")
    private String firstName;

    // @NotNull
    @JsonProperty
    @Column(name = "last_name")
    private String lastName;

    /**
     * Whether this is the primary contact for an account.
     */
    @JsonProperty
    @Column(name = "main_contact")
    private boolean mainContact;

    @JsonProperty
    @Column(name = "title")
    private String title;

    @JsonProperty
    @Column(name = "job_title")
    private String jobTitle;

    @JsonProperty
    @Column(name = "email")
    private String email;

    @JsonProperty
    @Column(name = "email_confirmed")
    private boolean emailConfirmed;

    @JsonProperty
    @Column(name = "email_optin")
    private boolean emailOptIn;

    @JsonProperty
    @Size(max = 32)
    @Column(name = "email_hash")
    private String emailHash;

    /**
     * A code to identify contact before registration and to be quoted in a
     * 'click to activate' email.
     */
    @JsonProperty
    @Size(max = 36)
    @Column(name = "uuid")
    private String uuid;

    @Pattern(regexp = "\\+?[0-9, \\-()]{0,15}")
    @Size(max = 15)
    @JsonProperty
    @Column(name = "phone1")
    private String phone1;

    @Pattern(regexp = "\\+?[0-9, \\-()]{0,15}")
    @Size(max = 15)
    @JsonProperty
    @Column(name = "phone2")
    private String phone2;

    @Pattern(regexp = "\\+?[0-9, \\-()]{0,15}")
    @JsonProperty
    private String phone3;

    @JsonProperty
    @Column(name = "address1")
    private String address1;

    @JsonProperty
    @Column(name = "address2")
    private String address2;

    @JsonProperty
    @Size(max = 60)
    @Column(name = "town")
    private String town;

    @JsonProperty
    @Size(max = 60)
    @Column(name = "county_or_city")
    private String countyOrCity;

    @JsonProperty
    @Size(max = 10)
    @Column(name = "post_code")
    private String postCode;

    @JsonProperty
    @Size(max = 60)
    @Column(name = "country")
    private String country;

    @JsonProperty
    @Column(name = "existing_customer")
    private boolean existingCustomer;

    @JsonProperty
    @Size(max = 30)
    @Column(name = "stage")
    private String stage;

    @JsonProperty
    @Column(name = "stage_reason")
    private String stageReason;

    @JsonProperty
    @Column(name = "stage_date")
    private Date stageDate;

    @JsonProperty
    @Size(max = 100)
    @Column(name = "enquiry_type")
    private String enquiryType;

    @JsonProperty
    @Size(max = 30)
    @Column(name = "account_type")
    private String accountType;

    @JsonProperty
    @Size(max = 50)
    @Column(name = "owner")
    private String owner;

    @JsonProperty
    @Column(name = "do_not_call")
    private boolean doNotCall;

    @JsonProperty
    @Column(name = "do_not_email")
    private boolean doNotEmail;

    @JsonProperty
    @Size(max = 16)
    @Column(name = "twitter")
    private String twitter;

    @JsonProperty
    @Column(name = "facebook")
    private String facebook;

    @JsonProperty
    @Column(name = "linked_in")
    private String linkedIn;

    @JsonProperty
    @Size(max = 1000)
    @Column(name = "description")
    private String description;

    /**
     * Intended to capture the source of the lead from Analytics or in some
     * cases the contact will declare the source.
     */
    @JsonProperty
    @Size(max = 30)
    @Column(name = "source")
    private String source;

    /**
     * In order to allow both Analytics source and a client declared one.
     */
    @JsonProperty
    @Column(name = "source2")
    private String source2;

    /**
     * Intended to capture the medium of the lead from Analytics.
     */
    @JsonProperty
    @Size(max = 30)
    @Column(name = "medium")
    private String medium;

    /**
     * Intended to capture the campaign of the lead from Analytics.
     */
    @JsonProperty
    @Size(max = 30)
    @Column(name = "campaign")
    private String campaign;

    /**
     * Intended to capture the keyword of the lead from Analytics.
     */
    @JsonProperty
    @Size(max = 30)
    @Column(name = "keyword")
    private String keyword;

    /**
     * Comma-separated set of alerts for the contact.
     */
    @JsonProperty
    @Column(name = "alerts")
    private String alerts;

    /**
     * Comma-separated set of arbitrary tags for the contact.
     */
    @JsonProperty
    @Column(name = "tags")
    private String tags;

    /**
     * The time the contact is created.
     *
     * Generally this field is managed by the application but this is not
     * rigidly enforced as exceptions such as data migration do exist.
     */
    @Temporal(TemporalType.TIMESTAMP)
    // Since this is SQL 92 it should be portable
    @Column(columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP", name = "first_contact", updatable = false)
    @JsonProperty
    private Date firstContact;

    @Temporal(TemporalType.TIMESTAMP)
    @JsonProperty
    @Column(name = "last_updated")
    private Date lastUpdated;

    @NotNull
    @JsonProperty
    @Size(max = 30)
    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @Transient
    @XmlElement(name = "link", namespace = Link.ATOM_NAMESPACE)
    @JsonProperty("links")
    private List<Link> links;

//     @JsonDeserialize(using = JsonCustomFieldDeserializer.class)
//     @JsonSerialize(using = JsonCustomFieldSerializer.class)
     private List<CustomField> customFields;

     public List<CustomField> getCustomFields() {
     if (customFields == null) {
     customFields = new ArrayList<CustomField>();
     }
     return customFields;
     }
    
    public void setCustomFields(List<CustomField> fields) {
        for (CustomField newField : fields) {
            setCustomField(newField);
        }
        setLastUpdated(new Date());
    }
    
    public String getCustomFieldValue(@NotNull String fieldName) {
        for (CustomField field : getCustomFields()) {
            if (fieldName.equals(field.getName())) {
                return field.getValue();
            }
        }
        return null;
    }
    
     public void addCustomField(CustomField customField) {
         setCustomField(customField);
     }
    
    protected void setCustomField(CustomField newField) {
        boolean found = false;
        for (CustomField field : getCustomFields()) {
            if (field.getName().equals(newField.getName())) {
                field.setValue(newField.getValue() == null ? null
                        : newField.getValue().toString());
                found = true;
            }
        }
        if (!found) {
            getCustomFields().add(newField);
        }
    }

    @JsonProperty
    public String getSelfRef() {
        return id == null ? null
                : String.format("/%1$s/contacts/%2$d", tenantId, id);
    }

    public Contact() {
    }

    public Contact(String firstName, String lastName, String email,
            String tenantId) {
        this();
        setFirstName(firstName);
        setLastName(lastName);
        setEmail(email);
        setTenantId(tenantId);
    }

    public String getFirstName() {
        return firstName == null ? DEFAULT_FIRST_NAME : firstName;
    }

    public boolean isFirstNameDefault() {
        return firstName == null;
    }

    public String getLastName() {
        return lastName == null ? uuid : lastName;
    }

    public boolean isLastNameDefault() {
        return lastName == null;
    }

    public String getFullName() {
        if (firstName == null && lastName == null) {
            return String.format("Unknown %1$s", uuid);
        } else {
            String fn = firstName == null ? "" : firstName;
            String ln = lastName == null ? "" : lastName;
            LOGGER.debug("  have firstName: " + fn);
            LOGGER.debug("  have lastName: " + ln);
            return String.format("%1$s %2$s", fn, ln).trim();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public boolean isMainContact() {
        return mainContact;
    }

    public void setMainContact(boolean mainContact) {
        this.mainContact = mainContact;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isEmailConfirmed() {
        return emailConfirmed;
    }

    public void setEmailConfirmed(boolean emailConfirmed) {
        this.emailConfirmed = emailConfirmed;
    }

    public boolean isEmailOptIn() {
        return emailOptIn;
    }

    public void setEmailOptIn(boolean emailOptIn) {
        this.emailOptIn = emailOptIn;
    }

    public String getEmailHash() {
        return emailHash;
    }

    public void setEmailHash(String emailHash) {
        this.emailHash = emailHash;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getPhone1() {
        return phone1;
    }

    public void setPhone1(String phone1) {
        this.phone1 = phone1;
    }

    public String getPhone2() {
        return phone2;
    }

    public void setPhone2(String phone2) {
        this.phone2 = phone2;
    }

    public String getPhone3() {
        return phone3;
    }

    public void setPhone3(String phone3) {
        this.phone3 = phone3;
    }

    public String getAddress1() {
        return address1;
    }

    public void setAddress1(String address1) {
        this.address1 = address1;
    }

    public String getAddress2() {
        return address2;
    }

    public void setAddress2(String address2) {
        this.address2 = address2;
    }

    public String getTown() {
        return town;
    }

    public void setTown(String town) {
        this.town = town;
    }

    public String getCountyOrCity() {
        return countyOrCity;
    }

    public void setCountyOrCity(String countyOrCity) {
        this.countyOrCity = countyOrCity;
    }

    public String getPostCode() {
        return postCode;
    }

    public void setPostCode(String postCode) {
        this.postCode = postCode;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public boolean isExistingCustomer() {
        return existingCustomer;
    }

    public void setExistingCustomer(boolean existingCustomer) {
        this.existingCustomer = existingCustomer;
    }

    public String getStage() {
        return stage;
    }

    public void setStage(String stage) {
        this.stage = stage;
    }

    public String getStageReason() {
        return stageReason;
    }

    public void setStageReason(String stageReason) {
        this.stageReason = stageReason;
    }

    public Date getStageDate() {
        return stageDate;
    }

    public void setStageDate(Date stageDate) {
        this.stageDate = stageDate;
    }

    public String getEnquiryType() {
        return enquiryType;
    }

    public void setEnquiryType(String enquiryType) {
        this.enquiryType = enquiryType;
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public boolean isDoNotCall() {
        return doNotCall;
    }

    public void setDoNotCall(boolean doNotCall) {
        this.doNotCall = doNotCall;
    }

    public boolean isDoNotEmail() {
        return doNotEmail;
    }

    public void setDoNotEmail(boolean doNotEmail) {
        this.doNotEmail = doNotEmail;
    }

    public String getTwitter() {
        return twitter;
    }

    public void setTwitter(String twitter) {
        this.twitter = twitter;
    }

    public String getFacebook() {
        return facebook;
    }

    public void setFacebook(String facebook) {
        this.facebook = facebook;
    }

    public String getLinkedIn() {
        return linkedIn;
    }

    public void setLinkedIn(String linkedIn) {
        this.linkedIn = linkedIn;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getSource2() {
        return source2;
    }

    public void setSource2(String source2) {
        this.source2 = source2;
    }

    public String getMedium() {
        return medium;
    }

    public void setMedium(String medium) {
        this.medium = medium;
    }

    public String getCampaign() {
        return campaign;
    }

    public void setCampaign(String campaign) {
        this.campaign = campaign;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String getAlerts() {
        return alerts;
    }

    public void setAlerts(String alerts) {
        this.alerts = alerts;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public Date getFirstContact() {
        return firstContact;
    }

    public void setFirstContact(Date firstContact) {
        this.firstContact = firstContact;
    }

    public Date getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public List<Link> getLinks() {
        return links;
    }

    public void setLinks(List<Link> links) {
        this.links = links;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    @JsonProperty
    public String getAddress() {
        StringBuilder sb = new StringBuilder();
        if (address1 != null && address1.length() > 0) {
            sb.append(address1).append(", ");
        }
        if (address2 != null && address2.length() > 0) {
            sb.append(address2).append(", ");
        }
        if (town != null && town.length() > 0) {
            sb.append(town).append(", ");
        }
        if (countyOrCity != null && countyOrCity.length() > 0) {
            sb.append(countyOrCity).append(", ");
        }
        if (postCode != null && postCode.length() > 0) {
            sb.append(postCode).append(". ");
        }
        if (country != null && country.length() > 0) {
            sb.append(country).append(".");
        }
        return sb.toString();
    }

    public class CustomField implements Serializable {
        private static final long serialVersionUID = 7496048564725313117L;

        @JsonProperty
        private Long id;

        @NotNull
        @JsonProperty
        private String name;

        @JsonProperty
        @Size(max = 1000)
        private String value;

        public CustomField() {
        }

        public CustomField(String key, Object value2) {
            this.name = key;
            this.value = value2 == null ? null : value2.toString();
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
        
    }
}
