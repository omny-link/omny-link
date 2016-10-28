package link.omny.catalog.repositories;

import java.util.List;

import link.omny.catalog.model.StockItem;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(path = "/stock-items")
public interface StockItemRepository extends CrudRepository<StockItem, Long> {

    StockItem findByName(@Param("name") String name);

    @Query("SELECT i FROM StockItem i WHERE i.tenantId = :tenantId AND (i.status IS NULL OR i.status != 'deleted') ORDER BY i.lastUpdated DESC")
    List<StockItem> findAllForTenant(@Param("tenantId") String tenantId);

    @Query("SELECT i FROM StockItem i WHERE i.tenantId = :tenantId AND (i.status IS NULL OR i.status != 'deleted') ORDER BY i.lastUpdated DESC")
    List<StockItem> findPageForTenant(@Param("tenantId") String tenantId,
            Pageable pageable);

    @Query("SELECT i FROM StockItem i WHERE i.tenantId = :tenantId AND LOWER(i.status) = :status ORDER BY i.lastUpdated DESC")
    List<StockItem> findByStatusForTenant(@Param("status") String status,
            @Param("tenantId") String tenantId);

    @Query("SELECT i FROM StockItem i WHERE i.tenantId = :tenantId AND LOWER(i.status) = :status ORDER BY i.lastUpdated DESC")
    List<StockItem> findPageByStatusForTenant(@Param("status") String status,
            @Param("tenantId") String tenantId, Pageable pageable);

    @Query("SELECT i FROM StockItem i INNER JOIN i.stockCategory c WHERE i.tenantId = :tenantId AND i.name = :categoryName AND (i.status IS NULL OR i.status != 'deleted') ORDER BY i.lastUpdated DESC")
    List<StockItem> findAllForCategoryName(
            @Param("categoryName") String categoryName,
            @Param("tenantId") String tenantId);

    @Query(value = "UPDATE OL_STOCK_ITEM i SET i.stock_cat_id = ?2 WHERE i.id = ?1", nativeQuery = true)
    @Modifying(clearAutomatically = true)
    public void setStockCategory(Long itemId, Long categoryId);

    @Query(value = "DELETE OL_STOCK_ITEM i WHERE i.stock_cat_id = ?1", nativeQuery = true)
    @Modifying(clearAutomatically = true)
    public void deleteByStockCategory(Long stockCategoryId);
}
