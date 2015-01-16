package link.omny.custmgmt.model;

import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonProperty;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Document {

    protected static final Logger LOGGER = LoggerFactory
            .getLogger(Document.class);

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    @JsonProperty
    private Long id;

    @JsonProperty
    private String author;

    @JsonProperty
    private Date created;

    @JsonProperty
    private String url;

    @ManyToOne(targetEntity = Contact.class, fetch = FetchType.EAGER, cascade = {
            CascadeType.PERSIST, CascadeType.MERGE })
    @JoinColumn(name = "contact_id")
    private Contact contact;

    @PrePersist
    void preInsert() {
        created = new Date();
    }
}
