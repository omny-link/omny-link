package link.omny.custmgmt.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.rest.core.annotation.RestResource;

import com.fasterxml.jackson.annotation.JsonProperty;

@Entity
@Data
@Table(name = "OL_DOCUMENT")
@AllArgsConstructor
@NoArgsConstructor
public class Document implements Serializable {

    private static final long serialVersionUID = 157180600778360331L;

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
    private String name;

    @JsonProperty
    private String url;

    @RestResource(rel = "documentContact")
    @ManyToOne(targetEntity = Contact.class)
    @JoinColumn(name = "contact_id")
    private Contact contact;

    public Document(String author, String url) {
        setAuthor(author);
        setUrl(url);
    }

    @PrePersist
    void preInsert() {
        created = new Date();
    }

    @Override
    public String toString() {
        return String.format(
                "Document [id=%s, author=%s, created=%s, name=%s, url=%s]", id,
                author, created, name, url);
    }
}
