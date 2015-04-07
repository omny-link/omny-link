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
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import link.omny.custmgmt.json.JsonCustomContactFieldDeserializer;
import link.omny.custmgmt.json.JsonCustomFieldSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@Entity
// @DiscriminatorValue("contact")
// @Table(name = "contact")
@Data
@AllArgsConstructor
@NoArgsConstructor
// TODO for some reason using these instead of jacksonBuilder in Application
// class results in StackOverflow
// @JsonSerialize(using = JsonContactSerializer.class)
// @JsonDeserialize(using = JsonContactDeserializer.class)
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
     */
    @JsonProperty
    private String title;

    /**
     */
    // @NotNull
    @JsonProperty
    // @Column(nullable = false)
    private String email;

    /**
     */
    @Pattern(regexp = "\\+?[0-9, ]{0,13}")
    @JsonProperty
    private String phone1;

    /**
     */
    @Pattern(regexp = "\\+?[0-9, ]{0,13}")
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
    private String stage;

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
    @NotNull
    @JsonProperty
    @Column(nullable = false)
    private String tenantId;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    // , mappedBy = "contact")
    // @JsonTypeInfo
    // @JsonTypeResolver(value = null)
    // @JsonIgnore
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
        this.customFields = fields;
    }

    public Object getField(@NotNull String fieldName) {
        for (CustomField field : getCustomFields()) {
            if (fieldName.equals(field.getName())) {
                return field.getValue();
            }
        }
        return null;
    }

    public void addCustomField(CustomContactField customField) {
        getCustomFields().add(customField);
    }

    public void setField(String key, Object value) {
        getCustomFields().add(
                new CustomContactField(key, value == null ? null : value
                        .toString()));
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

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "contact")
    private List<Note> notes;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "contact")
    private List<Activity> activity;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "contact")
    private List<Document> documents;

    public List<Activity> getActivities() {
        if (activity == null) {
            activity = new ArrayList<Activity>();
        }
        return activity;
    }

    public void setFullName(String name) {
        String fName;
        try {
            fName = name.substring(0, name.indexOf(' '));
            setLastName(name.substring(name.indexOf(' ') + 1));
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

    @PrePersist
    public void preInsert() {
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

    public long timeSinceLogin() {
        Activity lastLogin = getLastActivityOfType("login");
        return lastLogin == null ? -1 : new Date().getTime()
                - lastLogin.getOccurred().getTime();
    }

    public long timeSinceFirstLogin() {
        Activity firstLogin = getFirstActivityOfType("login");
        return firstLogin == null ? -1 : new Date().getTime()
                - firstLogin.getOccurred().getTime();
    }

    public long timeSinceRegistered() {
        Activity firstLogin = getFirstActivityOfType("register");
        return firstLogin == null ? -1 : new Date().getTime()
                - firstLogin.getOccurred().getTime();
    }

    public long timeSinceEmail() {
        Activity firstLogin = getFirstActivityOfType("email");
        return firstLogin == null ? -1 : new Date().getTime()
                - firstLogin.getOccurred().getTime();
    }

    public long timeSinceValuation() {
        Activity firstLogin = getFirstActivityOfType("valuation");
        return firstLogin == null ? -1 : new Date().getTime()
                - firstLogin.getOccurred().getTime();
    }

    public boolean haveSentEmail(String emailName) {
        for (Activity act : getActivities()) {
            if ("email".equals(act.getType())
                    && emailName.equals(act.getContent())) {
                return true;
            }
        }
        return false;
    }

    public boolean notYetSentEmail(String emailName) {
        return !haveSentEmail(emailName);
    }

    protected List<Activity> getActivitiesOfType(String type) {
        List<Activity> activities = new ArrayList<Activity>();
        for (Activity act : getActivities()) {
            if (type.equals(act.getType())) {
                activities.add(act);
            }
        }
        return activities;
    }

    protected Activity getLastActivityOfType(String type) {
        Activity lastLogin = null;
        for (Activity act : getActivities()) {
            if (type.equals(act.getType())
                    && (lastLogin == null || lastLogin.getOccurred().after(
                            act.getOccurred()))) {
                lastLogin = act;
            }
        }
        return lastLogin;
    }

    protected Activity getFirstActivityOfType(String type) {
        Activity firstLogin = null;
        for (Activity act : getActivities()) {
            if (type.equals(act.getType())
                    && (firstLogin == null || firstLogin.getOccurred().before(
                            act.getOccurred()))) {
                firstLogin = act;
            }
        }
        return firstLogin;
    }
}
