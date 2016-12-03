package link.omny.acctmgmt.model;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class TenantTemplate extends TenantExtension {

    private static final long serialVersionUID = -637266923265547235L;

    public TenantTemplate(String name, String url) {
        super(name, url);
    }

}
