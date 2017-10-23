package link.omny.catalog.repositories;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import link.omny.catalog.model.StockCategory;

@RepositoryRestResource(path = "/stock-categories", exported = false)
public interface StockCategoryRepository extends
        CrudRepository<StockCategory, Long> {

    @Query("SELECT c FROM StockCategory c WHERE c.name = :name AND c.tenantId = :tenantId")
    StockCategory findByName(@Param("name") String name,
            @Param("tenantId") String tenantId);

    @Query("SELECT c FROM StockCategory c WHERE c.tenantId = :tenantId ORDER BY c.name ASC")
    List<StockCategory> findAllForTenant(@Param("tenantId") String tenantId);

    @Query("SELECT COUNT(c) FROM StockCategory c WHERE c.tenantId = :tenantId AND (c.status IS NULL OR c.status != 'deleted')")
    long countForTenant(@Param("tenantId") String tenantId);

    @Query("SELECT c FROM StockCategory c WHERE c.tenantId = :tenantId ORDER BY c.name ASC")
    List<StockCategory> findPageForTenant(@Param("tenantId") String tenantId,
            Pageable pageable);

    @Query("SELECT c FROM StockCategory c WHERE c.status IN :status AND c.tenantId = :tenantId ORDER BY c.name ASC")
    List<StockCategory> findByStatusForTenant(
            @Param("tenantId") String tenantId, @Param("status") String status);

    @Query("SELECT c FROM StockCategory c WHERE c.status IN :status AND c.tenantId = :tenantId AND c.offerStatus = :offerStatus ORDER BY c.name ASC")
    List<StockCategory> findByStatusAndOffersForTenant(
            @Param("tenantId") String tenantId, @Param("status") String status,
            @Param("offerStatus") String offerStatus);
}
