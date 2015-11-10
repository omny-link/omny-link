package link.omny.custmgmt.model;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Data;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Relates a Message to its recipients.
 * 
 * @author Tim Stephenson
 */
@Entity
@Table(name = "OL_MEMO_DIST")
@Data
@AllArgsConstructor
public class MemoDistribution implements Serializable {

    private static final long serialVersionUID = 1237181996013717501L;

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    @JsonProperty
    private Long id;

    @JsonProperty
    private String name;

    @JsonProperty
    private String owner;
    
    @JsonProperty
    @Lob
    private String recipients;

    @JsonProperty
    private String status;

    @JsonProperty
    private String memoRef;

    @NotNull
    @JsonProperty
    @Column(nullable = false)
    private String tenantId;

    @Temporal(TemporalType.TIMESTAMP)
    // Since this is SQL 92 it should be portable
    @Column(columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP", updatable = false)
    @JsonProperty
    private Date created;

    @Temporal(TemporalType.TIMESTAMP)
    // Since this is SQL 92 it should be portable
    @Column(columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP", updatable = true)
    @JsonProperty
    private Date lastUpdated;

    public MemoDistribution() {
        created = new Date();
    }

    public List<String> getRecipientList() {
        return Arrays.asList(getRecipients().split(","));
    }

    public void setRecipientList(List<String> recipientList) {
        setRecipients(recipientList.toString());
    }

    @PrePersist
    public void preInsert() {
        if (created == null) {
            created = new Date();
        }
    }

    @PreUpdate
    public void preUpdate() {
        lastUpdated = new Date();
    }

    public String toCsv() {
        return String.format("%1$d,%2$s,%3$s,%4$s,%5$s,%6$s", id, name, status,
                owner, memoRef, recipients);
    }

}
