package link.omny.acctmgmt.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a single tenant.
 *
 * @author Tim Stephenson
 */
@Data
@Entity
@Table(name = "OL_TENANT")
@NoArgsConstructor
@AllArgsConstructor
public class Tenant implements Serializable {

    private static final long serialVersionUID = -4729049290436298887L;

    protected static final Logger LOGGER = LoggerFactory
            .getLogger(Tenant.class);

    @Id
    protected String id;

    @JsonProperty
    protected String name;

    @JsonProperty
    @Column(name = "remote_url")
    protected String remoteUrl;

    @Transient
    private TenantConfig config;

    public Tenant(String id, String configUrl) {
        this();
        setId(id);
        setRemoteUrl(configUrl);
    }

    public String getName() {
        if (name == null) {
            return id;
        } else {
            return name;
        }
    }

}
