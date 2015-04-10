package link.omny.custmgmt.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
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
import org.springframework.data.rest.core.annotation.RestResource;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonProperty;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Note {

    protected static final Logger LOGGER = LoggerFactory.getLogger(Note.class);

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
    private String content;

    @RestResource(rel = "noteContact")
    @ManyToOne(targetEntity = Contact.class)
    @JoinColumn(name = "contact_id")
    @JsonBackReference
    private Contact contact;

    /**
     * Permits auto-filled columns such as created date to be suspended.
     */
    private static boolean bulkImport;

    @PrePersist
    void preInsert() {
        if (!bulkImport) {
            created = new Date();
        }
    }

    public Note(String author, String content) {
        super();
        setAuthor(author);
        setContent(content);
    }

    public static void setBulkImport(boolean b) {
        bulkImport = b;
    }
}
