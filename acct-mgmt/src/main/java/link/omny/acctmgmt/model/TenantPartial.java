package link.omny.acctmgmt.model;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import lombok.NoArgsConstructor;

@Entity
@DiscriminatorValue("partial")
@NoArgsConstructor
public class TenantPartial extends TenantExtension {

    private static final long serialVersionUID = -1134185133273685783L;

    public TenantPartial(String name, String url) {
        super(name, url);
    }

}
