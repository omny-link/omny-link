package link.omny.acctmgmt.repositories;

import link.omny.acctmgmt.model.TenantConfig;

import org.springframework.data.repository.CrudRepository;

public interface TenantConfigRepository extends
        CrudRepository<TenantConfig, String> {

}
