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
package link.omny.custmgmt.model;

import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedAttributeNode;
import javax.persistence.NamedEntityGraph;
import javax.persistence.NamedEntityGraphs;
import javax.persistence.OneToMany;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlElement;

import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.hateoas.Link;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import link.omny.custmgmt.internal.NullAwareBeanUtils;
import link.omny.custmgmt.json.CustomBooleanDeserializer;
import link.omny.custmgmt.json.JsonCustomContactFieldDeserializer;
import link.omny.custmgmt.json.JsonCustomFieldSerializer;
import link.omny.custmgmt.model.views.ContactViews;
import link.omny.supportservices.internal.CsvUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "OL_CONTACT")
@NamedEntityGraphs({
        @NamedEntityGraph(name = "contactWithAccount", attributeNodes = {
                @NamedAttributeNode("account"),
                @NamedAttributeNode("customFields") }),
        @NamedEntityGraph(name = "contactWithActivities", attributeNodes = {
                @NamedAttributeNode("activities") }) })
@Data
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class Contact implements Serializable {

    private static final String DEFAULT_FIRST_NAME = "Unknown";

    private static final long serialVersionUID = -6080589981067789428L;

    protected static final Logger LOGGER = LoggerFactory
            .getLogger(Contact.class);

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    @JsonProperty
    @JsonView({ ContactViews.Detailed.class })
    private Long id;

    // @NotNull
    @JsonProperty
    @JsonView({ ContactViews.Summary.class })
    @Column(name = "first_name")
    private String firstName;

    // @NotNull
    @JsonProperty
    @JsonView({ ContactViews.Summary.class })
    @Column(name = "last_name")
    private String lastName;

    /**
     * Whether this is the primary contact for an account.
     */
    @JsonProperty
    @JsonView({ ContactViews.Summary.class })
    @Column(name = "main_contact")
    private boolean mainContact;

    @JsonProperty
    @JsonView({ ContactViews.Summary.class })
    @Column(name = "title")
    private String title;

    @JsonProperty
    @JsonView({ ContactViews.Summary.class })
    @Column(name = "job_title")
    private String jobTitle;

    @JsonProperty
    @JsonView({ ContactViews.Summary.class })
    @Column(name = "email")
    private String email;

    @JsonProperty
    @JsonView({ ContactViews.Detailed.class })
    @Column(name = "email_confirmed")
    private boolean emailConfirmed;

    @JsonProperty
    @JsonView({ ContactViews.Detailed.class })
    @JsonDeserialize(using = CustomBooleanDeserializer.class)
    @Column(name = "email_optin")
    private Boolean emailOptIn;

    @JsonProperty
    @JsonView({ ContactViews.Detailed.class })
    @Size(max = 32)
    @Column(name = "email_hash")
    private String emailHash;

    /**
     * A code to identify contact before registration and to be quoted in a
     * 'click to activate' email.
     */
    @JsonProperty
    @JsonView({ ContactViews.Detailed.class })
    @Size(max = 36)
    @Column(name = "uuid")
    private String uuid;

    @Pattern(regexp = "\\+?[0-9, \\-()]{0,15}")
    @Size(max = 15)
    @JsonProperty
    @JsonView({ ContactViews.Summary.class })
    @Column(name = "phone1")
    private String phone1;

    @Pattern(regexp = "\\+?[0-9, \\-()]{0,15}")
    @Size(max = 15)
    @JsonProperty
    @JsonView({ ContactViews.Detailed.class })
    @Column(name = "phone2")
    private String phone2;

    @Pattern(regexp = "\\+?[0-9, \\-()]{0,15}")
    @JsonProperty
    @JsonView({ ContactViews.Detailed.class })
    private String phone3;

    @JsonProperty
    @JsonView({ ContactViews.Detailed.class })
    @Column(name = "address1")
    private String address1;

    @JsonProperty
    @JsonView({ ContactViews.Detailed.class })
    @Column(name = "address2")
    private String address2;

    @JsonProperty
    @JsonView({ ContactViews.Detailed.class })
    @Size(max = 60)
    @Column(name = "town")
    private String town;

    @JsonProperty
    @JsonView({ ContactViews.Detailed.class })
    @Size(max = 60)
    @Column(name = "county_or_city")
    private String countyOrCity;

    @JsonProperty
    @JsonView({ ContactViews.Detailed.class })
    @Size(max = 10)
    @Column(name = "post_code")
    private String postCode;

    @JsonProperty
    @JsonView({ ContactViews.Detailed.class })
    @Size(max = 60)
    @Column(name = "country")
    private String country;

    @JsonProperty
    @JsonView({ ContactViews.Detailed.class })
    @Column(name = "existing_customer")
    private boolean existingCustomer;

    @JsonProperty
    @JsonView({ ContactViews.Summary.class })
    @Size(max = 30)
    @Column(name = "stage")
    private String stage;

    @JsonProperty
    @JsonView({ ContactViews.Detailed.class })
    @Column(name = "stage_reason")
    private String stageReason;

    @JsonProperty
    @JsonView({ ContactViews.Detailed.class })
    @Column(name = "stage_date")
    private Date stageDate;

    @JsonProperty
    @JsonView({ ContactViews.Summary.class })
    @Size(max = 100)
    @Column(name = "enquiry_type")
    private String enquiryType;

    @JsonProperty
    @JsonView({ ContactViews.Summary.class })
    @Size(max = 30)
    @Column(name = "account_type")
    private String accountType;

    @JsonProperty
    @JsonView({ ContactViews.Summary.class })
    @Size(max = 50)
    @Column(name = "owner")
    private String owner;

    @JsonProperty
    @JsonView({ ContactViews.Detailed.class })
    @Column(name = "do_not_call")
    private boolean doNotCall;

    @JsonProperty
    @JsonView({ ContactViews.Detailed.class })
    @Column(name = "do_not_email")
    private boolean doNotEmail;

    @JsonProperty
    @JsonView({ ContactViews.Detailed.class })
    @Size(max = 16)
    @Column(name = "twitter")
    private String twitter;

    @JsonProperty
    @JsonView({ ContactViews.Detailed.class })
    @Column(name = "facebook")
    private String facebook;

    @JsonProperty
    @JsonView({ ContactViews.Detailed.class })
    @Column(name = "linked_in")
    private String linkedIn;

    @JsonProperty
    @JsonView({ ContactViews.Detailed.class })
    @Size(max = 1000)
    @Column(name = "description")
    private String description;

    /**
     * Intended to capture the source of the lead from Analytics or in some
     * cases the contact will declare the source.
     */
    @JsonProperty
    @JsonView({ ContactViews.Detailed.class })
    @Size(max = 30)
    @Column(name = "source")
    private String source;

    /**
     * In order to allow both Analytics source and a client declared one.
     */
    @JsonProperty
    @JsonView({ ContactViews.Detailed.class })
    @Column(name = "source2")
    private String source2;

    /**
     * Intended to capture the medium of the lead from Analytics.
     */
    @JsonProperty
    @JsonView({ ContactViews.Detailed.class })
    @Size(max = 30)
    @Column(name = "medium")
    private String medium;

    /**
     * Intended to capture the campaign of the lead from Analytics.
     */
    @JsonProperty
    @JsonView({ ContactViews.Detailed.class })
    @Size(max = 30)
    @Column(name = "campaign")
    private String campaign;

    /**
     * Intended to capture the keyword of the lead from Analytics.
     */
    @JsonProperty
    @JsonView({ ContactViews.Detailed.class })
    @Size(max = 30)
    @Column(name = "keyword")
    private String keyword;

    /**
     * Comma-separated set of alerts for the contact.
     */
    @JsonProperty
    @JsonView({ ContactViews.Detailed.class })
    @Column(name = "alerts")
    private String alerts;

    /**
     * Comma-separated set of arbitrary tags for the contact.
     */
    @JsonProperty
    @JsonView({ ContactViews.Summary.class })
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
    @JsonView({ ContactViews.Summary.class })
    private Date firstContact;

    @Temporal(TemporalType.TIMESTAMP)
    @JsonProperty
    @JsonView({ ContactViews.Summary.class })
    @Column(name = "last_updated")
    private Date lastUpdated;

    @NotNull
    @JsonProperty
    @JsonView({ ContactViews.Summary.class })
    @Size(max = 30)
    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @Transient
    @XmlElement(name = "link", namespace = Link.ATOM_NAMESPACE)
    @JsonProperty("links")
    @JsonView({ ContactViews.Summary.class })
    private List<Link> links;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "contact", targetEntity = CustomContactField.class)
    @JsonDeserialize(using = JsonCustomContactFieldDeserializer.class)
    @JsonSerialize(using = JsonCustomFieldSerializer.class)
    @JsonView({ ContactViews.Detailed.class })
    private List<CustomContactField> customFields;

    public List<CustomContactField> getCustomFields() {
        if (customFields == null) {
            customFields = new ArrayList<CustomContactField>();
        }
        return customFields;
    }

    public void setCustomFields(List<CustomContactField> fields) {
        for (CustomContactField newField : fields) {
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

    public void addCustomField(CustomContactField customField) {
        customField.setContact(this);
        setCustomField(customField);
    }

    protected void setCustomField(CustomContactField newField) {
        boolean found = false;
        for (CustomContactField field : getCustomFields()) {
            if (field.getName().equals(newField.getName())) {
                field.setValue(newField.getValue() == null ? null
                        : newField.getValue().toString());
                found = true;
            }
        }
        if (!found) {
            newField.setContact(this);
            getCustomFields().add(newField);
        }
    }

    @JsonProperty
    @JsonView({ ContactViews.Summary.class })
    @ManyToOne(cascade = CascadeType.ALL, optional = true, fetch = FetchType.EAGER)
    @NotFound(action = NotFoundAction.IGNORE)
    // TODO this fixes the inifite recursion but instead we get
    // [2015-03-04 15:09:15.977] boot - 15472 ERROR [http-nio-8082-exec-7] ---
    // AbstractRepositoryRestController: Can not handle managed/back reference
    // 'defaultReference': type: value deserializer of type
    // org.springframework.data.rest.webmvc.json.PersistentEntityJackson2Module$UriStringDeserializer
    // does not support them
    // java.lang.IllegalArgumentException: Can not handle managed/back reference
    // 'defaultReference': type: value deserializer of type
    // org.springframework.data.rest.webmvc.json.PersistentEntityJackson2Module$UriStringDeserializer
    // does not support them
    // at
    // com.fasterxml.jackson.databind.JsonDeserializer.findBackReference(JsonDeserializer.java:310)
    // @JsonManagedReference
    private Account account;

    @JsonProperty
    @JsonView({ ContactViews.Detailed.class })
    private transient Long accountId;

    public Long getAccountId() {
        return getAccount() == null ? (accountId == null ? null : accountId)
                : getAccount().getId();
    }

    @JsonProperty
    @JsonView({ ContactViews.Summary.class })
    public String getSelfRef() {
        return id == null ? null
                : String.format("/%1$s/contacts/%2$d", tenantId, id);
    }

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "contact_id")
    @JsonView({ ContactViews.Detailed.class })
    private List<Note> notes;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "contact_id")
    @JsonView({ ContactViews.Detailed.class })
    private List<Activity> activities;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "contact_id")
    @JsonView({ ContactViews.Detailed.class })
    private List<Document> documents;

    @Transient
    private Date now;

    @Transient
    private List<String> customHeadings;

    public Contact(String firstName, String lastName, String email,
            String tenantId) {
        this();
        setFirstName(firstName);
        setLastName(lastName);
        setEmail(email);
        setTenantId(tenantId);
    }

    public List<Activity> getActivities() {
        if (activities == null) {
            activities = new ArrayList<Activity>();
        }
        return activities;
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

    /**
     * Convenience for forms that want to have just one field for first name and
     * last name. This will be transposed into firstName and lastName by
     * splitting at the first space which evidence suggests usually works (2
     * failures in 122 sample from GS import).
     */
    @JsonProperty
    @JsonView({ ContactViews.Detailed.class })
    public void setFullName(String name) {
        String fName;
        try {
            fName = name.substring(0, name.indexOf(' '));
            String remainder = name.substring(name.indexOf(' ') + 1);
            if (remainder.contains(" ")) {
                setFirstName(remainder.substring(0, remainder.indexOf(' ')));
                remainder = remainder.substring(remainder.indexOf(' ') + 1);
            }
            setLastName(remainder);
        } catch (StringIndexOutOfBoundsException e) {
            fName = name;
        }
        switch (fName) {
        case "Dr":
        case "Mr":
        case "Mrs":
        case "Miss":
            setTitle(fName);
            break;
        default:
            setFirstName(fName);
        }
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

    public List<String> getAlertsAsList() {
        if (alerts == null) {
            return Collections.emptyList();
        } else {
            return Arrays.asList(alerts.split(","));
        }
    }

    public String initials() {
        return String.format("%1$c%2$c", getFirstName().charAt(0),
                getLastName().charAt(0));
    }

    public List<Note> getNotes() {
        if (notes == null) {
            notes = new ArrayList<Note>();
        }
        return notes;
    }

    public List<Document> getDocuments() {
        if (documents == null) {
            documents = new ArrayList<Document>();
        }
        return documents;
    }

    public void addNote(Note note) {
        getNotes().add(note);
    }

    public void addDocument(Document doc) {
        getDocuments().add(doc);
    }

    @PrePersist
    public void prePersist() {
        if (LOGGER.isWarnEnabled() && firstContact != null) {
            LOGGER.warn(String.format(
                    "Overwriting create date %1$s with 'now'.", firstContact));
        }
        firstContact = new Date();

        if (uuid == null) {
            uuid = UUID.randomUUID().toString();
        }

        initEmailHash();
        NullAwareBeanUtils.trimStringProperties(this);
    }

    private void initEmailHash() {
        // Can happen in the event of anon contacts
        if (email == null) {
            return;
        }
        try {
            byte[] bytes = MessageDigest.getInstance("MD5")
                    .digest(email.getBytes());

            // convert the byte to hex format method 1
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < bytes.length; i++) {
                sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16)
                        .substring(1));
            }
            emailHash = sb.toString();
        } catch (NoSuchAlgorithmException e) {
            // JDK required to support MD5
            // http://docs.oracle.com/javase/8/docs/api/java/security/MessageDigest.html
            LOGGER.error(e.getMessage(), e);
        }
    }

    @PreUpdate
    public void preUpdate() {
        lastUpdated = new Date();

        initEmailHash();
        NullAwareBeanUtils.trimStringProperties(this);
    }

    private Date getNow() {
        if (now == null) {
            now = new Date();

        }
        return now;
    }

    @JsonProperty
    @JsonView({ ContactViews.Detailed.class })
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

    private long getTimeSinceLast(String type) {
        long time = -1;
        try {
            Activity lastEmail = getLastActivityOfType(type);
            time = lastEmail == null ? -1
                    : getNow().getTime() - lastEmail.getOccurred().getTime();
        } catch (NullPointerException e) {
            LOGGER.warn(String.format(
                    "Exception in getTimeSinceLast('%1$s'), assume no such activity",
                    type), e);
        }
        LOGGER.info(
                String.format("determined time since %1$s: %2$d", type, time));
        return time;
    }

    private long getTimeSinceFirst(String type) {
        long time = -1;
        try {
            Activity lastEmail = getFirstActivityOfType(type);
            time = lastEmail == null ? -1
                    : getNow().getTime() - lastEmail.getOccurred().getTime();
        } catch (NullPointerException e) {
            LOGGER.warn(String.format(
                    "Exception in getTimeSinceLast('%1$s'), assume no such activity",
                    type), e);
        }
        LOGGER.info(
                String.format("determined time since %1$s: %2$d", type, time));
        return time;
    }

    @JsonProperty("timeSinceBusinessPlanDownload")
    public long getTimeSinceBusinessPlanDownload() {
        return getTimeSinceLast("businessPlanDownload");
    }

    @JsonProperty("timeSinceLogin")
    public long getTimeSinceLogin() {
        return getTimeSinceLast("login");
    }

    @JsonProperty("timeSinceFirstLogin")
    public long getTimeSinceFirstLogin() {
        return getTimeSinceFirst("login");
    }

    @JsonProperty("timeSinceRegistered")
    public long getTimeSinceRegistered() {
        return getTimeSinceLast("register");
    }

    @JsonProperty("timeSinceEmail")
    public long getTimeSinceEmail() {
        return getTimeSinceLast("email");
    }

    @JsonProperty("timeSinceValuation")
    public long getTimeSinceValuation() {
        return getTimeSinceLast("valuation");
    }

    public boolean haveSentEmail(String emailName) {
        for (Activity act : getActivities()) {
            if ("email".equalsIgnoreCase(act.getType())
                    && act.getContent().contains(emailName)) {
                return true;
            }
        }
        return false;
    }

    public boolean notYetSentEmail(String emailName) {
        return !haveSentEmail(emailName);
    }

    @JsonProperty
    @JsonView({ ContactViews.Detailed.class })
    @Transient
    public int getEmailsSent() {
        try {
            return getActivitiesOfType("email").size();
        } catch (RuntimeException e) {
            LOGGER.warn(String
                    .format("No activities available for contact %1$d", id));
            return 0;
        }
    }

    public void setEmailsSent(int sent) {
        LOGGER.info("Discarding derived value");
        ; // discard derived
    }

    public List<Activity> getActivitiesOfType(String type) {
        List<Activity> activities = new ArrayList<Activity>();
        for (Activity act : getActivities()) {
            if (type.equalsIgnoreCase(act.getType())) {
                activities.add(act);
            }
        }
        return activities;
    }

    public Activity getLastActivityOfType(String type) {
        LOGGER.info("getLastActivityOfType: " + type);
        Activity lastAct = null;
        try {
            for (Activity act : getActivities()) {
                if (type.equalsIgnoreCase(act.getType()) && (lastAct == null
                        || lastAct.getOccurred().before(act.getOccurred()))) {
                    lastAct = act;
                }
            }
            LOGGER.info("  found last activity: " + lastAct);
        } catch (Exception e) {
            LOGGER.debug("  no activity of type: {} found", type);
        }
        return lastAct;
    }

    public Activity getFirstActivityOfType(String type) {
        LOGGER.info("getFirstActivityOfType: " + type);
        Activity firstAct = null;
        try {
            for (Activity act : getActivities()) {
                if (type.equalsIgnoreCase(act.getType()) && (firstAct == null
                        || firstAct.getOccurred().after(act.getOccurred()))) {
                    firstAct = act;
                }
            }
            LOGGER.info("  found last activity: " + firstAct);
        } catch (Exception e) {
            LOGGER.debug("  no activity of type: {} found", type);
        }
        return firstAct;
    }

    public String getEmailConfirmationCode() {
        if (uuid == null) {
            setUuid(UUID.randomUUID().toString());
        }

        return uuid;
    }

    public void confirmEmail(String code) {
        if (uuid != null && uuid.equals(code)) {
            setEmailConfirmed(true);
            uuid = null;
        } else {
            throw new IllegalArgumentException();
        }
    }

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    public void setUtm_source(String source) {
        setSource(source);
    }

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    public void setUtm_medium(String medium) {
        setMedium(medium);
    }

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    public void setUtm_campaign(String campaign) {
        setCampaign(campaign);
    }

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    public void setUtm_keyword(String keyword) {
        setKeyword(keyword);
    }

    public String toCsv() {
        StringBuilder sb = new StringBuilder() ;
        sb.append(String.format(
                "%1$d,%2$d,%3$s,%4$s,%5$s,%6$b,%7$s,%8$s,%9$s,%10$s,"
                + "%11$s,%12$s,%13$s,%14$s,%15$s,%16$b,%17$s,%18$s,%19$s,"
                + "%20$s,%21$s,%22$s,%23$s,%24$s,%25$s,%26$b,%27$s,%28$s,%29$s,"
                + "%30$s,%31$s,%32$s,%33$s,%34$b,%35$s,%36$s,%37$s,%38$s,%39$s,"
                + "%40$s,%41$s,%42$s,%43$s",
                getId(),
                getAccountId(),
                getFirstName(),
                getLastName(),
                title == null ? "" : title,
                isMainContact(),
                address1 == null ? "" : CsvUtils.quoteIfNeeded(address1),
                address2 == null ? "" : CsvUtils.quoteIfNeeded(address2),
                town == null ? "" : town,
                countyOrCity == null ? "" : countyOrCity,
                country == null ? "" : country,
                postCode == null ? "" : postCode,
                email == null ? "" : email,
                // isMailConfirmed(),
                jobTitle == null ? "" : jobTitle,
                phone1 == null ? "" : phone1,
                phone2 == null ? "" : phone2,
                // getAccountName(),
                owner == null ? "" : owner,
                stage == null ? "" : stage,
                stageReason == null ? "" : stageReason,
                stageDate == null ? "" : stageDate,
                enquiryType == null ? "" : enquiryType,
                accountType == null ? "" : accountType,
                isExistingCustomer(),
                source == null ? "" : source,
                medium == null ? "" : medium,
                campaign == null ? "" : campaign,
                keyword == null ? "" : keyword,
                isDoNotCall(),
                isDoNotEmail(),
                getTags() == null ? "" : CsvUtils.quoteIfNeeded(getTags()),
                getUuid(),
                twitter == null ? "" : twitter,
                linkedIn == null ? "" : linkedIn,
                facebook == null ? "" : facebook,
                // getSkype(),
                tenantId,
                firstContact == null ? "" : firstContact,
                getLastUpdated(),
                getTimeSinceLogin(),
                getTimeSinceFirstLogin(),
                getTimeSinceRegistered(),
                getTimeSinceEmail(),
                getConsolidatedNotes(),
                getConsolidatedDocuments()));
        if (customHeadings == null) {
            LOGGER.warn("No custom headings specified, so only standard fields can be included");
        } else {
            for (String fieldName : customHeadings) {
                String val = getCustomFieldValue(fieldName);
                sb.append(',').append(val == null ? "" : CsvUtils.quoteIfNeeded(val));
            }
        }
        return sb.toString();
    }

    private String getConsolidatedNotes() {
        StringBuffer sb = new StringBuffer();
        for (Note note : getNotes()) {
            sb.append(String.format("%s %s: %s;",
                    note.getCreated(), note.getAuthor(),
                    note.getContent()));
        }
        return CsvUtils.quoteIfNeeded(sb.toString());
    }

    private String getConsolidatedDocuments() {
        StringBuffer sb = new StringBuffer();
        for (Document doc : getDocuments()) {
            sb.append(String.format("%s %s: %s %s;",
                    doc.getCreated(), doc.getAuthor(), doc.getName(), doc.getUrl()));
        }
        return CsvUtils.quoteIfNeeded(sb.toString());
    }

    @Override
    public String toString() {
        return String.format(
                "Contact [id=%s, firstName=%s, lastName=%s, title=%s, jobTitle=%s, email=%s, emailConfirmed=%s, emailConfirmationCode=%s, phone1=%s, phone2=%s, address1=%s, address2=%s, town=%s, countyOrCity=%s, postCode=%s, country=%s, stage=%s, enquiryType=%s, accountType=%s, owner=%s, doNotCall=%s, doNotEmail=%s, source=%s, medium=%s, campaign=%s, keyword=%s, alerts=%s, tags=%s, firstContact=%s, lastUpdated=%s, tenantId=%s, customFields=%s, account=%d]",
                id, firstName, lastName, title, jobTitle, email, emailConfirmed,
                uuid, phone1, phone2, address1, address2, town, countyOrCity,
                postCode, country, stage, enquiryType, accountType, owner,
                doNotCall, doNotEmail, source, medium, campaign, keyword,
                alerts, tags, firstContact, lastUpdated, tenantId, customFields,
                account == null ? null : account.getId());
    }

}
