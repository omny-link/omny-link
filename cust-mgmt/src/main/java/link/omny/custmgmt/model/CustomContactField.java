package link.omny.custmgmt.model;

import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToOne;

import lombok.NoArgsConstructor;

@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@NoArgsConstructor
public class CustomContactField extends CustomField {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7683896817261973079L;

	public CustomContactField(String key, String value) {
		super(key, value);
	}

	@ManyToOne
	// (fetch = FetchType.EAGER, cascade = { CascadeType.PERSIST,
	// CascadeType.MERGE })
	private Contact contact;

}
