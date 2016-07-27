package link.omny.acctmgmt.model;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import lombok.NoArgsConstructor;

@Entity
@DiscriminatorValue("template")
@NoArgsConstructor
public class TenantTemplate extends TenantExtension {

    private static final long serialVersionUID = -637266923265547235L;

    public TenantTemplate(String name, String url) {
        super(name, url);
    }

}
