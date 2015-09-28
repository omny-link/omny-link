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

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.rest.core.annotation.RestResource;

import com.fasterxml.jackson.annotation.JsonProperty;

@Entity
@Data
// @ToString(exclude = "contact")
@AllArgsConstructor
@NoArgsConstructor
public class Note implements Serializable {

    private static final long serialVersionUID = 6032851169275605576L;

    protected static final Logger LOGGER = LoggerFactory.getLogger(Note.class);

    /**
     * Permits auto-filled columns such as created date to be suspended.
     */
    private static boolean bulkImport;

    public static void setBulkImport(boolean b) {
        bulkImport = b;
    }

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

    // This advises to avoid back reference in a composition relationship
    // http://stackoverflow.com/questions/25311978/posting-a-onetomany-sub-resource-association-in-spring-data-rest/25451662#25451662
    // However doing so means we are trapped in the POST sub-entity + PUT
    // association trap, hence doing it this way
    @RestResource(rel = "noteContact")
    @ManyToOne(targetEntity = Contact.class)
    @JoinColumn(name = "contact_id")
    // @JsonBackReference
    private Contact contact;



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

    @Override
    public String toString() {
        System.out.println("toString");
        System.out.println("  id:" + id);
        System.out.println("  author:" + author);
        System.out.println("  created: " + created);
        System.out.println("  content: " + content);
        return String.format("Note [id=%s, author=%s, created=%s, content=%s]",
                id, author, created, content);
    }

}
