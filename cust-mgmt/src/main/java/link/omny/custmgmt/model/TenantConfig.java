package link.omny.custmgmt.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents an individual tenant's instance of the Customer Management module.
 * 
 * @author Tim Stephenson
 *
 */
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TenantConfig implements Serializable {

	private static final long serialVersionUID = 4886215941519231066L;

	protected static final Logger LOGGER = LoggerFactory
			.getLogger(TenantConfig.class);

	@Id
	@NotNull
	@JsonProperty
	@Column(nullable = false)
	private String name;

	@JsonProperty
	private String[] contactFields;

	@JsonProperty
	private String[] accountFields;

}
