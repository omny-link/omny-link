package link.omny.acctmgmt.model;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class TenantSequence extends TenantExtension {

    private static final long serialVersionUID = 3214983461624071147L;

    public TenantSequence(String name) {
        super();
        setName(name);
    }

}
