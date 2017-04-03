package link.omny.custmgmt.repositories;

import java.util.Date;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import link.omny.custmgmt.model.Account;

@RepositoryRestResource(path = "/accounts")
public interface AccountRepository extends CrudRepository<Account, Long> {

    @Query("SELECT a FROM Account a WHERE (a.stage IS NULL OR a.stage != 'deleted') AND a.tenantId = :tenantId ORDER BY a.lastUpdated DESC")
    List<Account> findAllForTenant(@Param("tenantId") String tenantId);

    @Query("SELECT a FROM Account a WHERE (a.stage IS NULL OR a.stage != 'deleted') AND a.tenantId = :tenantId ORDER BY a.lastUpdated DESC")
    List<Account> findPageForTenant(@Param("tenantId") String tenantId,
            Pageable pageable);

    @Query(value = "UPDATE Account a set a.stage = :stage WHERE (a.lastUpdated < :before OR a.lastUpdated IS NULL) AND a.stage != 'deleted'AND a.tenantId = :tenantId")
    @Modifying(clearAutomatically = true)
    int updateStage(@Param("stage") String stage, @Param("before") Date before, @Param("tenantId") String tenantId);

    @Override
    @Query("UPDATE #{#entityName} x set x.stage = 'deleted' where x.id = :id")
    @Modifying(clearAutomatically = true)
    public void delete(@Param("id") Long id);
}
