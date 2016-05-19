package link.omny.acctmgmt.repositories;

import link.omny.acctmgmt.model.TenantConfig;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(path = "/admin/tenants")
public interface TenantConfigRepository extends
        CrudRepository<TenantConfig, String> {

}
