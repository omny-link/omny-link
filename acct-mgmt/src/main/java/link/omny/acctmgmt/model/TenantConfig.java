package link.omny.acctmgmt.model;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The model class to encapsulate a single tenant's configuration of Omny.
 *
 * @author Tim Stephenson
 */
@Data
@Entity
@Table(name = "OL_TENANT")
@NoArgsConstructor
@AllArgsConstructor
// TODO This is intended to replace the current JSON file, in particular to
// allow user-management of drop down lists and also but also to remove the need
// for redeployment to update those JSON files.
public class TenantConfig implements Serializable {

    private static final long serialVersionUID = -2041167810028725542L;

    @Id
    protected String tenantId;

}
