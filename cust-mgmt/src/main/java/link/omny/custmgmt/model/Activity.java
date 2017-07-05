package link.omny.custmgmt.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;

import link.omny.custmgmt.model.views.AccountViews;
import link.omny.custmgmt.model.views.ContactViews;
import lombok.AllArgsConstructor;
import lombok.Data;

@Entity
@Table(name = "OL_ACTIVITY")
@Data
@AllArgsConstructor
public class Activity implements Serializable {

    private static final long serialVersionUID = -3132677793751164824L;

    protected static final Logger LOGGER = LoggerFactory
            .getLogger(Activity.class);

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    @JsonProperty
    private Long id;

    @NotNull
    @JsonProperty
    @JsonView({ AccountViews.Detailed.class, ContactViews.Detailed.class })
    @Column(name = "type")
    private String type;

    @JsonProperty
    @JsonView({ AccountViews.Detailed.class, ContactViews.Detailed.class })
    @Column(name = "content")
    private String content;

    @Temporal(TemporalType.TIMESTAMP)
    // Since this is SQL 92 it should be portable
    @Column(columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP", name="occurred", updatable = false)
    @JsonProperty
    @JsonView({ AccountViews.Detailed.class, ContactViews.Detailed.class })
    private Date occurred;

    @Temporal(TemporalType.TIMESTAMP)
    @JsonProperty
    @JsonView({ AccountViews.Detailed.class, ContactViews.Detailed.class })
    @Column(name = "last_updated")
    private Date lastUpdated;

    public Activity() {
        super();
        setOccurred(new Date());
    }

    public Activity(String type, Date occurred) {
        super();
        setType(type);
        setOccurred(occurred);
    }

    public Activity(String type, Date occurred, String content) {
        this(type, occurred);
        setContent(content);
    }

    @PreUpdate
    public void preUpdate() {
        if (LOGGER.isWarnEnabled() && lastUpdated != null) {
            LOGGER.warn(String.format(
                    "Overwriting update date %1$s with 'now'.", lastUpdated));
        }
        lastUpdated = new Date();
    }

    @Override
    public String toString() {
        return String
                .format("Activity [id=%s, type=%s, content=%s, occurred=%s, lastUpdated=%s]",
                        id, type, content, occurred, lastUpdated);
    }

}
