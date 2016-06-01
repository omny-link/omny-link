package link.omny.acctmgmt.model;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@DiscriminatorValue("toolbarEntry")
@NoArgsConstructor
public class TenantToolbarEntry extends TenantExtension {

    private static final long serialVersionUID = -8327456294072464761L;

    private String role;

    public TenantToolbarEntry(String name, String url, String icon,
            String description) {
        super(name, url, description);
        setIcon(icon);
    }

    public String getUrl() {
        if (super.getUrl() != null && !super.getUrl().startsWith("/")) {
            setUrl("/" + super.getUrl());
        }
        return super.getUrl();
    }

    /**
     * Convert from legacy property name.
     * 
     * @deprecated Use setName instead.
     */
    public void setTitle(String selector) {
        setName(selector);
    }

    /**
     * Convert from legacy property name.
     * 
     * @deprecated Use getName instead.
     */
    public String getTitle() {
        return getName();
    }

    /**
     * Convert from legacy property name.
     * 
     * @deprecated Use setIcon instead.
     */
    public void setClasses(String classes) {
        setIcon(classes);
    }

    /**
     * Convert from legacy property name.
     * 
     * @deprecated Use getIcon instead.
     */
    public String getClasses() {
        if (getIcon().indexOf("glyphicon ") == -1) {
            return "glyphicon " + getIcon();
        } else {
        return getIcon();
        }
    }
}
