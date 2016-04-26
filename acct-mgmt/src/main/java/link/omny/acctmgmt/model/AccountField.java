package link.omny.acctmgmt.model;

import io.onedecision.engine.domain.model.EntityField;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import lombok.NoArgsConstructor;

@Entity
@DiscriminatorValue("account")
@NoArgsConstructor
public class AccountField extends EntityField {

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
