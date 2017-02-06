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
import javax.persistence.Lob;
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

import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import link.omny.custmgmt.internal.NullAwareBeanUtils;
import link.omny.custmgmt.json.JsonCustomContactFieldDeserializer;
import link.omny.custmgmt.json.JsonCustomFieldSerializer;
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
        @NamedEntityGraph(name = "contactWithActivities", attributeNodes = { @NamedAttributeNode("activity") }) })
@Data
@EqualsAndHashCode(exclude = { "fullName" })
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
    private Long id;

    /**
     * Convenience for forms that want to have just one field for first name and
     * last name. This will be transposed into firstName and lastName by
     * splitting at the first space which evidence suggests usually works (2
     * failures in 122 sample from gardenatics).
     */
    @Transient
    private String fullName;

    // @NotNull
    @JsonProperty
    // @Column(nullable = false)
    private String firstName;

    /**
     */
    // @NotNull
    @JsonProperty
    // @Column(nullable = false)
    private String lastName;

    /**
     * Whether this is the primary contact for an account.
     */
    @JsonProperty
    private boolean mainContact;

    /**
     */
    @JsonProperty
    private String title;

    @JsonProperty
    private String jobTitle;

    /**
     */
    @JsonProperty
    private String email;

    @JsonProperty
    private boolean emailConfirmed;

    @JsonProperty
    private String emailHash;

    /**
     * A code to identify contact before registration and to be quoted in a
     * 'click to activate' email.
     */
    @JsonProperty
    private String uuid;

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

    @JsonProperty
    private boolean doNotCall;

    @JsonProperty
    private boolean doNotEmail;

    @JsonProperty
    private String twitter;

    @JsonProperty
    private String facebook;

    @JsonProperty
    private String linkedIn;

    @JsonProperty
    @Lob
    private String description;

    /**
     * Intended to capture the source of the lead from Analytics or in some
     * cases the contact will declare the source.
     */
    @JsonProperty
    private String source;

    /**
     * In order to allow both Analytics source and a client declared one.
     */
    @JsonProperty
    private String source2;

    /**
     * Intended to capture the medium of the lead from Analytics.
     */
    @JsonProperty
    private String medium;

    /**
     * Intended to capture the campaign of the lead from Analytics.
     */
    @JsonProperty
    private String campaign;

    /**
     * Intended to capture the keyword of the lead from Analytics.
     */
    @JsonProperty
    private String keyword;

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
     * The time the contact is created.
     * 
     * Generally this field is managed by the application but this is not
     * rigidly enforced as exceptions such as data migration do exist.
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

    /**
     */
    @NotNull
    @JsonProperty
    @Column(nullable = false)
    private String tenantId;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "contact", targetEntity = CustomContactField.class)
    @JsonDeserialize(using = JsonCustomContactFieldDeserializer.class)
    @JsonSerialize(using = JsonCustomFieldSerializer.class)
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

    public Object getCustomFieldValue(@NotNull String fieldName) {
        for (CustomField field : getCustomFields()) {
            if (fieldName.equals(field.getName())) {
                return field.getValue();
            }
        }
        return null;
    }

    public void addCustomField(CustomContactField customField) {
        customField.setContact(this);
        getCustomFields().add(customField);
    }

    protected void setCustomField(CustomContactField newField) {
        boolean found = false;
        for (CustomContactField field : getCustomFields()) {
            if (field.getName().equals(newField.getName())) {
                field.setValue(newField.getValue() == null ? null : newField
                        .getValue().toString());
                found = true;
            }
        }
        if (!found) {
            newField.setContact(this);
            getCustomFields().add(newField);
        }
    }

    @JsonProperty
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
    private transient Long accountId;

    public Long getAccountId() {
        return getAccount() == null ? (accountId == null ? null : accountId)
                : getAccount().getId();
    }

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "contact")
    private List<Note> notes;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "contact")
    private List<Activity> activity;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "contact")
    private List<Document> documents;

    @Transient
    private Date now;

    public Contact(String firstName, String lastName, String email,
            String tenantId) {
        this();
        setFirstName(firstName);
        setLastName(lastName);
        setEmail(email);
        setTenantId(tenantId);
    }

    public void setActivities(List<Activity> activities) {
        this.activity = activities;
    }

    public List<Activity> getActivities() {
        if (activity == null) {
            activity = new ArrayList<Activity>();
        }
        return activity;
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
        if (email==null) {
            return;
        }
        try {
            byte[] bytes = MessageDigest.getInstance("MD5").digest(email.getBytes());

          //convert the byte to hex format method 1
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < bytes.length; i++) {
              sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
            }
            emailHash=sb.toString();
        } catch (NoSuchAlgorithmException e) {
            // JDK required to support MD5
            // http://docs.oracle.com/javase/8/docs/api/java/security/MessageDigest.html
            LOGGER.error(e.getMessage(), e);
        }
    }

    @PreUpdate
    public void preUpdate() {
        if (LOGGER.isWarnEnabled() && lastUpdated != null) {
            LOGGER.warn(String.format(
                    "Overwriting update date %1$s with 'now'.", lastUpdated));
        }
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

    private long getTimeSinceLast(String type) {
        long time = -1;
        try {
            Activity lastEmail = getLastActivityOfType(type);
            time = lastEmail == null ? -1 : getNow().getTime()
                    - lastEmail.getOccurred().getTime();
        } catch (NullPointerException e) {
            LOGGER.warn(
                    String.format(
                            "Exception in getTimeSinceLast('%1$s'), assume no such activity",
                            type), e);
        }
        LOGGER.info(String.format("determined time since %1$s: %2$d", type,
                time));
        return time;
    }

    private long getTimeSinceFirst(String type) {
        long time = -1;
        try {
            Activity lastEmail = getFirstActivityOfType(type);
            time = lastEmail == null ? -1 : getNow().getTime()
                    - lastEmail.getOccurred().getTime();
        } catch (NullPointerException e) {
            LOGGER.warn(
                    String.format(
                            "Exception in getTimeSinceLast('%1$s'), assume no such activity",
                            type), e);
        }
        LOGGER.info(String.format("determined time since %1$s: %2$d", type,
                time));
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
    @Transient
    public int getEmailsSent() {
        try {
            return getActivitiesOfType("email").size();
        } catch (RuntimeException e) {
            LOGGER.warn(String.format(
                    "No activities available for contact %1$d", id));
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
        for (Activity act : getActivities()) {
            if (type.equalsIgnoreCase(act.getType())
                    && (lastAct == null || lastAct.getOccurred().before(
                            act.getOccurred()))) {
                lastAct = act;
            }
        }
        LOGGER.info("  found last activity: " + lastAct);
        return lastAct;
    }

    public Activity getFirstActivityOfType(String type) {
        LOGGER.info("getFirstActivityOfType: " + type);
        Activity firstAct = null;
        for (Activity act : getActivities()) {
            if (type.equalsIgnoreCase(act.getType())
                    && (firstAct == null || firstAct.getOccurred().after(
                            act.getOccurred()))) {
                firstAct = act;
            }
        }
        LOGGER.info("  found last activity: " + firstAct);
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

    @Override
    public String toString() {
        return String
                .format("Contact [id=%s, firstName=%s, lastName=%s, title=%s, jobTitle=%s, email=%s, emailConfirmed=%s, emailConfirmationCode=%s, phone1=%s, phone2=%s, address1=%s, address2=%s, town=%s, countyOrCity=%s, postCode=%s, country=%s, stage=%s, enquiryType=%s, accountType=%s, owner=%s, doNotCall=%s, doNotEmail=%s, source=%s, medium=%s, campaign=%s, keyword=%s, alerts=%s, tags=%s, firstContact=%s, lastUpdated=%s, tenantId=%s, customFields=%s, account=%d]",
                        id, firstName, lastName, title, jobTitle, email,
                        emailConfirmed, uuid, phone1, phone2, address1,
                        address2, town, countyOrCity, postCode, country, stage,
                        enquiryType, accountType, owner, doNotCall, doNotEmail,
                        source, medium, campaign, keyword, alerts, tags,
                        firstContact,
                        lastUpdated, tenantId, customFields,
                        account == null ? null : account.getId());
    }

}
