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
import java.text.DecimalFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.NamedAttributeNode;
import javax.persistence.NamedEntityGraph;
import javax.persistence.OneToMany;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.SecondaryTable;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.annotation.CreatedDate;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import link.omny.custmgmt.json.JsonCustomAccountFieldDeserializer;
import link.omny.custmgmt.model.views.AccountViews;
import link.omny.custmgmt.model.views.ContactViews;
import link.omny.supportservices.internal.CsvUtils;
import link.omny.supportservices.json.JsonCustomFieldSerializer;
import link.omny.supportservices.model.Activity;
import link.omny.supportservices.model.Auditable;
import link.omny.supportservices.model.CustomField;
import link.omny.supportservices.model.Document;
import link.omny.supportservices.model.Note;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Entity
@NamedEntityGraph(name = "accountOnly",
    attributeNodes = {
        @NamedAttributeNode("customFields"),
    }
)
@NamedEntityGraph(name = "accountWithAll",
    attributeNodes = {
        @NamedAttributeNode("activities"),
        @NamedAttributeNode("customFields"),
        @NamedAttributeNode("notes"),
        @NamedAttributeNode("documents")
    }
)
@Table(name = "OL_ACCOUNT")
@SecondaryTable(name = "OL_ACCOUNT_CUSTOM",
    pkJoinColumns = @PrimaryKeyJoinColumn(name = "account_id"))
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
public class Account extends Auditable<String> implements Serializable {

    private static final long serialVersionUID = -1955316248920138892L;

    protected static final Logger LOGGER = LoggerFactory
            .getLogger(Account.class);

    protected static DecimalFormat decimalFormat = new DecimalFormat("#.00");

    @Id
    @Column(name = "id")
    @SequenceGenerator(name = "accountIdSeq", sequenceName = "ol_account_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "accountIdSeq")
    @JsonProperty
    @JsonView( { AccountViews.Pair.class, ContactViews.Summary.class } )
    private Long id;

    @NotNull
    @JsonProperty
    @JsonView( { AccountViews.Pair.class, ContactViews.Summary.class } )
    @Size(max = 150)
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
    @Size(max = 8)
    @Column(name = "company_number")
    private String companyNumber;

    @JsonProperty
    @JsonView( { AccountViews.Detailed.class, ContactViews.Detailed.class } )
    // sufficient for UK sector industry codes in 2021
    @Column(name = "sic", length = 110)
    private String sic;

    @JsonProperty
    @JsonView( { AccountViews.Detailed.class, ContactViews.Detailed.class } )
    @Size(max = 150)
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
    @JsonView({ AccountViews.Detailed.class, ContactViews.Detailed.class })
    @Column(name = "email_optin")
    private boolean emailOptIn;

    @JsonProperty
    @JsonView( { AccountViews.Detailed.class, ContactViews.Detailed.class } )
    @Column(name = "email_hash")
    private String emailHash;

    @Pattern(regexp = "\\+?[0-9, \\-()]{0,15}")
    @JsonProperty
    @JsonView( { AccountViews.Summary.class, ContactViews.Detailed.class } )
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
    @Size(max = 60)
    @Column(name = "town")
    private String town;

    @JsonProperty
    @JsonView( { AccountViews.Detailed.class, ContactViews.Detailed.class } )
    @Size(max = 60)
    @Column(name = "county_or_city")
    private String countyOrCity;

    @JsonProperty
    @JsonView( { AccountViews.Detailed.class, ContactViews.Detailed.class } )
    @Size(max = 60)
    @Column(name = "post_code")
    private String postCode;

    @JsonProperty
    @JsonView( { AccountViews.Detailed.class, ContactViews.Detailed.class } )
    @Size(max = 60)
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
    @Column(name = "description")
    @Size(max = 1000)
    private String description;

    @Digits(integer = 4, fraction = 0)
    @JsonProperty
    @JsonView( { AccountViews.Detailed.class, ContactViews.Detailed.class } )
    @Column(name = "incorporation_year")
    private Integer incorporationYear;

    @JsonView( { AccountViews.Detailed.class, ContactViews.Detailed.class } )
    @Size(max = 20)
    @Column(name = "no_of_employees")
    private String noOfEmployees;

    @JsonProperty
    @JsonView( { AccountViews.Detailed.class, ContactViews.Detailed.class } )
    @Column(name = "existing_customer")
    private boolean existingCustomer;

    @JsonProperty
    @JsonView( { AccountViews.Summary.class } )
    @Size(max = 30)
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
    @Size(max = 50)
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
    @JsonProperty
    @JsonView( { AccountViews.Summary.class } )
    @CreatedDate
    // Since this is SQL 92 it should be portable
    @Column(columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP", name = "first_contact", updatable = false)
    private Date firstContact;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "account", targetEntity = CustomAccountField.class)
    @JsonDeserialize(using = JsonCustomAccountFieldDeserializer.class)
    @JsonSerialize(using = JsonCustomFieldSerializer.class)
    @JsonView( { AccountViews.Detailed.class } )
    private Set<CustomAccountField> customFields;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @JoinColumn(name = "account_id")
    @JsonView({ AccountViews.Detailed.class })
    private Set<Note> notes;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "account_id")
    @JsonView({ AccountViews.Detailed.class })
    private Set<Activity> activities;

    @Transient
    @JsonView({ AccountViews.Detailed.class })
    private List<String> customHeadings;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "account_id")
    @JsonView({ AccountViews.Detailed.class })
    private Set<Document> documents;

    public Set<CustomAccountField> getCustomFields() {
        if (customFields == null) {
            customFields = new HashSet<CustomAccountField>();
        }
        return customFields;
    }

    public void setCustomFields(List<CustomAccountField> fields) {
        for (CustomAccountField newField : fields) {
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

    public void addCustomField(CustomAccountField customField) {
        customField.setAccount(this);
        setCustomField(customField);
    }

    protected void setCustomField(CustomAccountField newField) {
        // check if it's a number in exponential form
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

    public Set<Activity> getActivities() {
        if (activities == null) {
            activities = new HashSet<Activity>();
        }
        return activities;
    }

    public Set<Note> getNotes() {
        if (notes == null) {
            notes = new HashSet<Note>();
        }
        return notes;
    }

    public Set<Document> getDocuments() {
        if (documents == null) {
            documents = new HashSet<Document>();
        }
        return documents;
    }

    public void addNote(Note note) {
        getNotes().add(note);
    }

    public void addDocument(Document doc) {
        getDocuments().add(doc);
    }

    public String toCsv() {
        StringBuilder sb = new StringBuilder()
                .append(String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,"
                        + "%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,"
                        + "%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,"
                        + "%s,%s,%s,%s",
                        id, name,
                        companyNumber == null ? "" : companyNumber,
                        aliases == null ? "" : aliases,
                        businessWebsite == null ? "" : businessWebsite,
                        address1 == null ? "" : CsvUtils.quoteIfNeeded(address1),
                        address2 == null ? "" : CsvUtils.quoteIfNeeded(address2),
                        town == null ? "" : town,
                        countyOrCity == null ? "" : countyOrCity,
                        country == null ? "" : country,
                        postCode == null ? "" : postCode,
                        email == null ? "" : email,
                        phone1 == null ? "" : phone1,
                        phone2 == null ? "" : phone2,
                        owner == null ? "" : owner,
                        stage == null ? "" : stage,
                        stageReason == null ? "" : stageReason,
                        stageDate == null ? "" : stageDate,
                        enquiryType == null ? "" : enquiryType,
                        accountType == null ? "" : accountType,
                        isExistingCustomer(),
                        getTags() == null ? "" : CsvUtils.quoteIfNeeded(getTags()),
                        twitter == null ? "" : twitter,
                        linkedIn == null ? "" : linkedIn,
                        facebook == null ? "" : facebook,
                        shortDesc == null ? "" : CsvUtils.quoteIfNeeded(shortDesc),
                        description == null ? ""
                                : CsvUtils.quoteIfNeeded(
                                        description.replace('"', '\'')),
                        incorporationYear == null ? "" : incorporationYear,
                        noOfEmployees == null ? "" : noOfEmployees,
                        tenantId, firstContact, lastUpdated,
                        getConsolidatedNotes(),
                        getConsolidatedDocuments()));
        if (customHeadings == null) {
            LOGGER.warn("No custom headings specified, so only standard fields can be included");
        } else {
            for (String fieldName : customHeadings) {
                String val = getCustomFieldValue(fieldName);
                sb.append(',').append(val == null ? ""
                        : CsvUtils.quoteIfNeeded(val.replace('"', '\'')));
            }
        }
        return sb.toString();
    }

    private String getConsolidatedNotes() {
        StringBuffer sb = new StringBuffer();
        for (Note note : getNotes()) {
            if (note.getContent() != null) {
                // Note that content containing quotes or new lines is fine
                // but containing quotenewline messes up spreadsheet imports
                sb.append(String.format("%s %s: %s;",
                        note.getCreated(), note.getAuthor(),
                        note.getContent().replace("\"\n", "\";")));
            }
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
        return String
                .format("Account [id=%s, name=%s, companyNumber=%s, aliases=%s, businessWebsite=%s, shortDesc=%s, description=%s, incorporationYear=%s, noOfEmployees=%s, tenantId=%s, firstContact=%s, lastUpdated=%s, customFields=%s]",
                        id, name, companyNumber, aliases, businessWebsite,
                        shortDesc, description, incorporationYear,
                        noOfEmployees, tenantId, firstContact, lastUpdated,
                        customFields);
    }
}
