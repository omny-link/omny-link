package link.omny.acctmgmt.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import link.omny.acctmgmt.model.Tenant;

@RepositoryRestResource(exported = false)
public interface TenantRepository extends CrudRepository<Tenant, String> {

    @Query("SELECT t FROM Tenant t WHERE t.status IS NULL OR t.status != 'deleted' ORDER BY t.name ASC")
    List<Tenant> findAll();

    @Query("SELECT COUNT(t) FROM Tenant t WHERE t.status IS NULL OR t.status != 'deleted')")
    long count();
}
