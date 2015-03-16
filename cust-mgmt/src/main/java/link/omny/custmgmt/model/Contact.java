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
    private String countyOrCity;

    @JsonProperty
    private String postCode;

    @JsonProperty
    private String enquiryType;

    @JsonProperty
    private String stage;

    @JsonProperty
    private String owner;

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
        // if (customFields.size() > 0) {
            // extension = new Extension(customFields);
        // }
        return customFields;
    }

    public void setCustomFields(List<CustomContactField> fields) {
        this.customFields = fields;
        // extension = new Extension(fields);
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
        // extension.customFields.add(customField);
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

    public void setField(String key, Object value) {
        getCustomFields().add(
                new CustomContactField(key, value == null ? null : value
                        .toString()));
    }

}
