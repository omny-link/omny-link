package link.omny.catalog.repositories;

import java.util.List;

import link.omny.catalog.model.CustomStockItemField;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(exported = false)
public interface CustomStockItemRepository extends
        CrudRepository<CustomStockItemField, Long> {

    @Query("SELECT o FROM CustomStockItemField o WHERE o.stockItem.id = :stockItemId AND o.stockItem.tenantId = :tenantId")
    List<CustomStockItemField> findByStockItemId(
            @Param("tenantId") String tenantId,
            @Param("stockItemId") Long stockItemId);
}
