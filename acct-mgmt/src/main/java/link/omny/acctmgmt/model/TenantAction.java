package link.omny.acctmgmt.model;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import lombok.NoArgsConstructor;

@Entity
@DiscriminatorValue("action")
@NoArgsConstructor
public class TenantAction extends TenantExtension {

    private static final long serialVersionUID = -1134185133273685783L;

    public TenantAction(String name, String url) {
        super(name, url);
    }

    /**
     * Convert from legacy property name.
     * 
     * @deprecated Use setName instead.
     */
    public void setLabel(String label) {
        setName(label);
    }

    /**
     * Convert from legacy property name.
     * 
     * @deprecated Use getName instead.
     */
    public String getLabel() {
        return getName();
    }

    /**
     * Convert from legacy property name.
     * 
     * @deprecated Use setRef instead.
     */
    public void setKey(String key) {
        setName(key);
    }

    /**
     * Convert from legacy property name.
     * 
     * @deprecated Use getRef instead.
     */
    public String getKey() {
        return getRef();
    }

    /**
     * Convert from legacy property name.
     * 
     * @deprecated Use setUrl instead.
     */
    public void setForm(String form) {
        setUrl(form);
    }

    /**
     * Convert from legacy property name.
     * 
     * @deprecated Use getUrl instead.
     */
    public String getForm() {
        return getUrl();
    }
}
