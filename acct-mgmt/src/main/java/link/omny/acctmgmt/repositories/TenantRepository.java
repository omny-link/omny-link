package link.omny.acctmgmt.repositories;

import link.omny.acctmgmt.model.Tenant;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(exported = false)
public interface TenantRepository extends CrudRepository<Tenant, String> {

}
