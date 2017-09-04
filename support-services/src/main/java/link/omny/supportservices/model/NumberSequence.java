package link.omny.supportservices.model;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlElement;

import org.springframework.hateoas.Link;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;

import link.omny.supportservices.views.NumberSequenceViews;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Table(name = "OL_SEQ")
@NoArgsConstructor
public class NumberSequence {

    @JsonProperty
    @Id
    @GeneratedValue
    private Long id;

    @JsonView(NumberSequenceViews.Summary.class)
    @JsonProperty
    @NotNull
    @Size(max = 60)
    @Column(name = "name")
    private String name;

    @JsonView(NumberSequenceViews.Summary.class)
    @JsonProperty
    @NotNull
    @Size(max = 60)
    @Column(name = "tenant_id")
    private String tenantId;

    @JsonView(NumberSequenceViews.Summary.class)
    @JsonProperty
    @NotNull
    @Column(name = "last")
    private Long lastUsed;

    @Transient
    @XmlElement(name = "link", namespace = Link.ATOM_NAMESPACE)
    @JsonProperty("links")
    @JsonView({ NumberSequenceViews.Summary.class })
    private List<Link> links;

    public NumberSequence(String name, String tenantId) {
        this.name = name;
        this.tenantId = tenantId;
        this.lastUsed = 0l;
    }

    public Long getNext() {
        return getLastUsed() + 1l;
    }

    public void increment() {
        this.lastUsed = this.getLastUsed() + 1l;        
    }
    
}