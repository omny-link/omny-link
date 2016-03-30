package link.omny.catalog.repositories;

import java.util.List;

import link.omny.catalog.model.StockCategory;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(path = "/stock-categories")
public interface StockCategoryRepository extends
        CrudRepository<StockCategory, Long> {

    StockCategory findByName(@Param("name") String name);

    @Query("SELECT c FROM StockCategory c WHERE c.tenantId = :tenantId ORDER BY c.lastUpdated DESC")
    List<StockCategory> findAllForTenant(@Param("tenantId") String tenantId);

    @Query("SELECT c FROM StockCategory c WHERE c.tenantId = :tenantId ORDER BY c.lastUpdated DESC")
    List<StockCategory> findPageForTenant(@Param("tenantId") String tenantId,
            Pageable pageable);

}
