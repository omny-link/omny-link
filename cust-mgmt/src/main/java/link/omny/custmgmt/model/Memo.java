package link.omny.custmgmt.model;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
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
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlElement;

import org.jsoup.Jsoup;
import org.springframework.hateoas.Link;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;

import link.omny.custmgmt.model.views.MemoViews;
import lombok.AllArgsConstructor;
import lombok.Data;

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
    @JsonView({ MemoViews.Summary.class })
    private Long id;
    
    @JsonProperty
    @JsonView({ MemoViews.Detailed.class })
    @Size(max = 50)
    @Column(name = "owner")
    private String owner;

    @JsonProperty
    @JsonView({ MemoViews.Summary.class })
    @NotNull
    @Size(max = 50)
    @Column(name = "name")
    private String name;

    @JsonProperty
    @JsonView({ MemoViews.Detailed.class })
    @Size(max = 50)
    @Column(name = "title")
    private String title;

    @JsonProperty
    @JsonView({ MemoViews.Detailed.class })
    @Column(name = "required_vars")
    private String requiredVars;

    @JsonProperty
    @JsonView({ MemoViews.Detailed.class })
    @Lob
    @Column(name = "rich_content")
    private String richContent;

    @JsonProperty
    @JsonView({ MemoViews.Detailed.class })
    @Lob
    @Column(name = "plain_content")
    private String plainContent;

    @JsonProperty
    @JsonView({ MemoViews.Detailed.class })
    @Size(max = 140)
    @Column(name = "short_content")
    private String shortContent;

    @NotNull
    @JsonProperty
    @JsonView({ MemoViews.Summary.class })
    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @JsonProperty
    @JsonView({ MemoViews.Summary.class })
    @Size(max = 30)
    @Column(name = "status")
    private String status;

    @Temporal(TemporalType.TIMESTAMP)
    // Since this is SQL 92 it should be portable
    @Column(columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP", name = "created", updatable = false)
    @JsonProperty
    @JsonView({ MemoViews.Summary.class })
    private Date created;

    @Temporal(TemporalType.TIMESTAMP)
    @JsonProperty
    @JsonView({ MemoViews.Summary.class })
    @Column(name = "last_updated")
    private Date lastUpdated;

    @Transient
    @XmlElement(name = "link", namespace = Link.ATOM_NAMESPACE)
    @JsonProperty("links")
    @JsonView({ MemoViews.Summary.class })
    private List<Link> links;

    public Memo() {
        created = new Date();
    }

    @JsonIgnore
    public void setRequiredVarList(List<String> vars) {
        if (vars == null || vars.size()==0) {
            requiredVars = null;
        } else {
            requiredVars = vars.toString();
        }
    }

    @JsonIgnore
    public List<String> getRequiredVarList() {
        if (requiredVars == null || requiredVars.length()==0) {
            return Collections.emptyList();
        } else {
            return Arrays.asList(requiredVars.split(","));
        }
    }

    public String getPlainContent() {
        if ((plainContent == null || plainContent.trim().length() == 0)
                && richContent != null) {
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
        cleanHtml();
        lastUpdated = new Date();
    }

    private void cleanHtml() {
        if (getRichContent()!=null) {
          setRichContent(getRichContent().replace("&#39;", "\'"));
        }
    }

    public String toCsv() {
        return String.format("%1$d,%2$s,%3$s,%4$s,%5$s,%6$s,%7$s", id, name,
                title, status, owner, richContent, plainContent);
    }

}
