package link.omny.acctmgmt.model;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import lombok.NoArgsConstructor;

@Entity
@DiscriminatorValue("process")
@NoArgsConstructor
public class TenantProcess extends TenantExtension {

    private static final long serialVersionUID = -1490759532264845381L;

    public TenantProcess(String name, String url, String ref) {
        super(name, url);
        setRef(ref);
    }
}
