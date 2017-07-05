package link.omny.custmgmt.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import javax.validation.constraints.Size;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;

import link.omny.custmgmt.model.views.AccountViews;
import link.omny.custmgmt.model.views.ContactViews;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Table(name = "OL_NOTE")
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
    @JsonView({ AccountViews.Detailed.class, ContactViews.Detailed.class })
    private Long id;

    @JsonProperty
    @JsonView({ AccountViews.Detailed.class, ContactViews.Detailed.class })
    @Size(max = 50)
    @Column(name = "author")
    private String author;

    @JsonProperty
    @JsonView({ AccountViews.Detailed.class, ContactViews.Detailed.class })
    @Column(name = "created")
    private Date created;

    @JsonProperty
    @JsonView({ AccountViews.Detailed.class, ContactViews.Detailed.class })
    @Column(name = "content")
    private String content;

    @JsonProperty
    @JsonView({ AccountViews.Detailed.class, ContactViews.Detailed.class })
    @Column(name = "favorite")
    private boolean favorite;

    // This advises to avoid back reference in a composition relationship
    // http://stackoverflow.com/questions/25311978/posting-a-onetomany-sub-resource-association-in-spring-data-rest/25451662#25451662
    // However doing so means we are trapped in the POST sub-entity + PUT
    // association trap, hence doing it this way
    // @RestResource(rel = "noteAccount")
    // @ManyToOne(targetEntity = Account.class)
    // @JoinColumn(name = "account_id")
    //// @JsonIgnore
    //// @JsonBackReference
    // private Account account;
    //
    // @RestResource(rel = "noteContact")
    // @ManyToOne(targetEntity = Contact.class)
    // @JoinColumn(name = "contact_id")
    //// @JsonIgnore
    //// @JsonBackReference
    // private Contact contact;

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

    public Note(String author, String content, boolean favorite) {
        this(author, content);
        setFavorite(favorite);
    }

    @Override
    public String toString() {
        return String.format(
                "Note [id=%s, author=%s, created=%s, favorite=%b, content=%s]",
                id, author, created, favorite, content);
    }

}
