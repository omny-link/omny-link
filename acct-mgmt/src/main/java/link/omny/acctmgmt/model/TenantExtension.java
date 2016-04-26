package link.omny.acctmgmt.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import lombok.Data;
import lombok.NoArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonProperty;

@Data
@Entity
@Table(name = "OL_TENANT_EXT")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "TYPE")
@NoArgsConstructor
public class TenantExtension implements Serializable {

    private static final long serialVersionUID = 4993644181746182288L;

    protected static final Logger LOGGER = LoggerFactory
            .getLogger(TenantExtension.class);

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    @JsonProperty
    private Long id;

    @NotNull
    @JsonProperty
    private String name;

    @JsonProperty
    private String icon;

    @JsonProperty
    private String description;

    @JsonProperty
    private String url;

    @JsonProperty
    private String ref;

    @Transient
    @JsonProperty
    private boolean valid;

    @ManyToOne(optional = false, targetEntity = TenantConfig.class)
    public TenantConfig tenant;

    public TenantExtension(String name, String url) {
        super();
        this.name = name;
        this.url = url;
    }

    public TenantExtension(String name, String url, String description) {
        this(name, url);
        setIcon(icon);
        setDescription(description);
    }

    @Override
    public String toString() {
        return String
                .format("TenantExtension [id=%s, name=%s, icon=%s, description=%s, url=%s, ref=%s, tenant=%s]",
                        id, name, icon, description, url, ref, tenant.getId());
    }

}
