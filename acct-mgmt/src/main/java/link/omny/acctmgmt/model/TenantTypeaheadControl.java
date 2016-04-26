package link.omny.acctmgmt.model;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import lombok.NoArgsConstructor;

@Entity
@DiscriminatorValue("typeaheadControl")
@NoArgsConstructor
public class TenantTypeaheadControl extends TenantExtension {

    private static final long serialVersionUID = 2970916107407996670L;

    public TenantTypeaheadControl(String name, String url) {
        super(name, url);
    }

    /**
     * Convert from legacy property name.
     * 
     * @deprecated Use setRef instead.
     */
    public void setSelector(String selector) {
        setRef(selector);
    }

    /**
     * Convert from legacy property name.
     * 
     * @deprecated Use getRef instead.
     */
    public String getSelector() {
        return getRef();
    }
}
