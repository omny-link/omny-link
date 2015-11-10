package link.omny.custmgmt.repositories;

import java.util.List;

import link.omny.custmgmt.model.MemoDistribution;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(path = "/memo-distributions")
public interface MemoDistributionRepository extends CrudRepository<MemoDistribution, Long> {

    @Query("SELECT m FROM MemoDistribution m WHERE m.tenantId = :tenantId AND (status IS NULL OR status != 'deleted') ORDER BY m.lastUpdated DESC")
    List<MemoDistribution> findAllForTenant(@Param("tenantId") String tenantId);

    @Query("SELECT m FROM MemoDistribution m WHERE m.tenantId = :tenantId AND (status IS NULL OR status != 'deleted') ORDER BY m.lastUpdated DESC")
    List<MemoDistribution> findPageForTenant(@Param("tenantId") String tenantId,
            Pageable pageable);
}
