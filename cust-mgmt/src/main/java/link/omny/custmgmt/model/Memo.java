package link.omny.custmgmt.model;

import java.io.Serializable;
import java.util.Date;

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
import javax.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Data;

import org.jsoup.Jsoup;

import com.fasterxml.jackson.annotation.JsonProperty;

@Entity
@Table(name = "OL_MEMO")
@Data
@AllArgsConstructor
public class Memo implements Serializable {

    private static final long serialVersionUID = 5885971394766699832L;

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    @JsonProperty
    private Long id;
    
    @JsonProperty
    private String owner;

    @JsonProperty
    @NotNull
    private String name;

    @JsonProperty
    private String title;

    @JsonProperty
    @Lob
    private String richContent;

    @JsonProperty
    @Lob
    private String plainContent;

    @JsonProperty
    @Size(max = 140)
    private String shortContent;

    @NotNull
    @JsonProperty
    @Column(nullable = false)
    private String tenantId;

    @JsonProperty
    private String status;

    @Temporal(TemporalType.TIMESTAMP)
    // Since this is SQL 92 it should be portable
    @Column(columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP", updatable = false)
    @JsonProperty
    private Date created;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(columnDefinition = "TIMESTAMP", updatable = true)
    @JsonProperty
    private Date lastUpdated;

    public Memo() {
        created = new Date();
    }
    
    public String getPlainContent() {
        if (plainContent == null && richContent != null) {
            plainContent = Jsoup.parse(richContent).text();
        }
        return plainContent;
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
        return String.format("%1$d,%2$s,%3$s,%4$s,%5$s,%6$s,%7$s", id, name,
                title, status, owner, richContent, plainContent);
    }

}
