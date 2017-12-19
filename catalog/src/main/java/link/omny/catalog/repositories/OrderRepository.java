package link.omny.catalog.repositories;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import link.omny.catalog.model.Order;

@RepositoryRestResource(path = "/orders")
public interface OrderRepository extends CrudRepository<Order, Long> {

    @Override
    Order findOne(Long id);

    @Query("SELECT o FROM Order o WHERE (o.stage IS NULL OR o.stage != 'deleted') AND o.tenantId = :tenantId ORDER BY o.lastUpdated DESC")
    List<Order> findAllForTenant(@Param("tenantId") String tenantId);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.tenantId = :tenantId AND (o.stage IS NULL OR o.stage != 'deleted')")
    long countForTenant(@Param("tenantId") String tenantId);

    @Query("SELECT o.stage, COUNT(o) FROM Order o WHERE (o.stage IS NULL OR o.stage != 'deleted') AND o.tenantId = :tenantId GROUP BY o.stage")
    List<Object[]> findAllForTenantGroupByStage(
            @Param("tenantId") String tenantId);

    @Query("SELECT o FROM Order o WHERE (o.stage IS NULL OR o.stage != 'deleted') AND o.tenantId = :tenantId ORDER BY o.lastUpdated DESC")
    List<Order> findPageForTenant(@Param("tenantId") String tenantId,
            Pageable pageable);

    @Query("SELECT o FROM Order o WHERE o.stage IN :stage AND o.tenantId = :tenantId ORDER BY o.lastUpdated DESC")
    List<Order> findByStageForTenant(@Param("tenantId") String tenantId,
            @Param("stage") String stage);

    @Query("SELECT o FROM Order o WHERE o.stage IN :stage AND o.tenantId = :tenantId ORDER BY o.lastUpdated DESC")
    List<Order> findPageByStageForTenant(@Param("tenantId") String tenantId,
            @Param("stage") String stage, Pageable pageable);

    @Query("SELECT o FROM Order o WHERE (o.stage IS NULL OR o.stage != 'deleted') AND o.type IN :type AND o.tenantId = :tenantId ORDER BY o.lastUpdated DESC")
    List<Order> findByTypeForTenant(@Param("tenantId") String tenantId,
            @Param("type") String type);

    @Query("SELECT o FROM Order o WHERE (o.stage IS NULL OR o.stage != 'deleted') AND o.type IN :type AND o.tenantId = :tenantId ORDER BY o.lastUpdated DESC")
    List<Order> findPageByTypeForTenant(@Param("tenantId") String tenantId,
            @Param("type") String type, Pageable pageable);

    @Query("SELECT o FROM Order o WHERE o.parent.id = :parentId AND (o.stage IS NULL OR o.stage != 'deleted') AND o.tenantId = :tenantId ORDER BY o.lastUpdated DESC")
    List<Order> findByParentOrderForTenant(@Param("tenantId") String tenantId,
            @Param("parentId") Long parentId);

    @Query("SELECT o FROM Order o WHERE (o.stage IS NULL OR o.stage != 'deleted') AND o.tenantId = :tenantId AND o.contactId = :contactId ORDER BY o.lastUpdated DESC")
    List<Order> findAllForContact(@Param("tenantId") String tenantId,
            @Param("contactId") Long contactId);

    @Query("SELECT o FROM Order o WHERE (o.stage IS NULL OR o.stage != 'deleted') AND o.tenantId = :tenantId AND o.contactId = :contactId ORDER BY o.lastUpdated DESC")
    List<Order> findPageForContact(@Param("tenantId") String tenantId,
            @Param("contactId") Long contactId,
            Pageable pageable);

    @Query("SELECT o FROM Order o WHERE (o.stage IS NULL OR o.stage != 'deleted') AND o.tenantId = :tenantId AND o.contactId IN :contactIds ORDER BY o.lastUpdated DESC")
    List<Order> findAllForContacts(@Param("tenantId") String tenantId,
            @Param("contactIds") Long[] contactIds);

    @Query("SELECT o FROM Order o WHERE o.tenantId = :tenantId AND o.contactId IN :contactIds ORDER BY o.lastUpdated DESC")
    List<Order> findPageForContacts(@Param("tenantId") String tenantId,
            @Param("contactIds") Long[] contactIds, Pageable pageable);

    @Query(value = "SELECT DISTINCT(cf.name) FROM OL_ORDER o INNER JOIN OL_ORDER_CUSTOM cf on o.id = cf.order_id WHERE (o.stage IS NULL OR o.stage != 'deleted') AND o.tenant_id = :tenantId ", nativeQuery = true)
    List<String> findCustomFieldNames(@Param("tenantId") String tenantId);

    @Override
    @Query("UPDATE #{#entityName} x set x.stage = 'deleted' where x.id = :id")
    @Modifying(clearAutomatically = true)
    public void delete(@Param("id") Long id);

    @Query("DELETE FROM CustomOrderItemField i WHERE i.orderItem.id = :orderItemId")
    @Modifying(clearAutomatically = true)
    public void deleteItemCustomField(@Param("orderItemId") Long orderItemId);

    @Query("DELETE FROM OrderItem i WHERE i.order.id = :orderId AND i.id = :orderItemId")
    @Modifying(clearAutomatically = true)
    public void deleteItem(@Param("orderId") Long orderId, @Param("orderItemId") Long orderItemId);
}
