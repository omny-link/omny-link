package link.omny.acctmgmt.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.ElementCollection;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class TenantTypeaheadControl extends TenantExtension {

    private static final long serialVersionUID = 2970916107407996670L;

    @ElementCollection
    private List<TenantTypeaheadValue> values;

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

    public void addValue(TenantTypeaheadValue value) {
        if (values == null) {
            values = new ArrayList<TenantTypeaheadValue>();
        }
        values.add(value);
    }

}
