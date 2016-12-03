package link.omny.acctmgmt.model;

import io.onedecision.engine.domain.model.EntityField;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class ContactField extends EntityField {

    /**
     * Convert from legacy property name.
     * 
     * @deprecated Use setName instead.
     */
    public void setKey(String key) {
        setName(key);
    }

    /**
     * Convert from legacy property name.
     * 
     * @deprecated Use getName instead.
     */
    public String getKey() {
        return getName();
    }

}
