package link.omny.acctmgmt.model;

import io.onedecision.engine.domain.model.EntityField;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class ExtendedEntityField extends EntityField {

    private String placeholder;

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
