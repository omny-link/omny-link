package link.omny.custmgmt.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.validation.constraints.Digits;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import link.omny.custmgmt.json.JsonCustomContactFieldDeserializer;
import link.omny.custmgmt.json.JsonCustomFieldSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@Entity
// @DiscriminatorValue("account")
// @Table(name = "account_info")
@Data
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
    @Digits(integer = 8, fraction = 0)
    @JsonProperty
    private Integer companyNumber;

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
    @Min(1L)
    private Integer noOfEmployees;

    /**
     */
    @NotNull
    @JsonProperty
    @Column(nullable = false)
    private String tenantId;

    @OneToMany(mappedBy = "account", targetEntity = Contact.class)
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

}
