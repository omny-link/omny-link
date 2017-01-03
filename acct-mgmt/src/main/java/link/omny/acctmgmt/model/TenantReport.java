package link.omny.acctmgmt.model;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class TenantReport extends TenantExtension {

    private static final long serialVersionUID = 6468503246969914361L;

    public TenantReport(String name, String url) {
        super(name, url);
    }

}
