package link.omny.custmgmt.model;

import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "OL_ACCOUNT_CUSTOM")
@Data
@ToString(exclude = "account")
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@NoArgsConstructor
public class CustomAccountField extends CustomField {

    private static final long serialVersionUID = -4837634414877249693L;

    public CustomAccountField(String key, String value) {
        super(key, value);
    }

    @ManyToOne(optional = false, targetEntity = Account.class)
    private Account account;

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        CustomAccountField other = (CustomAccountField) obj;
        if (account == null) {
            if (other.account != null)
                return false;
        } else if (!account.getId().equals(other.account.getId()))
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result
                + ((account == null) ? 0 : account.getId().hashCode());
        return result;
    }

}
