package link.omny.acctmgmt.model;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class TenantProcess extends TenantExtension {

    private static final long serialVersionUID = -1490759532264845381L;

    public TenantProcess(String name, String url, String ref) {
        super(name, url);
        setRef(ref);
    }
}
