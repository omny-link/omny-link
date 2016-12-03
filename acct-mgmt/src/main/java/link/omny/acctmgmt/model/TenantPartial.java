package link.omny.acctmgmt.model;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class TenantPartial extends TenantExtension {

    private static final long serialVersionUID = -1134185133273685783L;

    public TenantPartial(String name, String url) {
        super(name, url);
    }

}
