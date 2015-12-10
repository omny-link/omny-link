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
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import link.omny.custmgmt.json.JsonCustomContactFieldDeserializer;
import link.omny.custmgmt.json.JsonCustomFieldSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@Entity
// @DiscriminatorValue("account")
// @Table(name = "account_info")
@Data
// @ToString(exclude = "contact")
@AllArgsConstructor
@NoArgsConstructor
public class Account implements Serializable {

    private static final long serialVersionUID = -1955316248920138892L;

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
     */
    @Pattern(regexp = "[0-9]{8}")
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
    @DateTimeFormat(style = "M-")
    @JsonProperty
    private Date firstContact;

    /**
     */
    @Temporal(TemporalType.TIMESTAMP)
    @DateTimeFormat(style = "M-")
    @JsonProperty
    private Date lastUpdated;

    @OneToMany(mappedBy = "account", targetEntity = Contact.class)
    // TODO See Contact.account for details of limitation
    // @JsonBackReference
    private List<Contact> contact;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    // , mappedBy = "account", targetEntity = CustomAccountField.class)
    @JsonDeserialize(using = JsonCustomContactFieldDeserializer.class)
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
