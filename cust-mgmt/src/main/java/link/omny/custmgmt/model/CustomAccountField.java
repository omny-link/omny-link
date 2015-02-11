package link.omny.custmgmt.model;

import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class CustomAccountField extends CustomField {

    private static final long serialVersionUID = -4837634414877249693L;

    // @ManyToOne(optional = false, targetEntity = Contact.class)
    // // (fetch = FetchType.EAGER, cascade = { CascadeType.PERSIST,
    // // CascadeType.MERGE })
    // private Account account;

}
