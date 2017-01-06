package link.omny.catalog.repositories;

import java.util.List;

import link.omny.catalog.model.Order;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(path = "/orders")
public interface OrderRepository extends CrudRepository<Order, Long> {

    @Override
    Order findOne(Long id);

    @Query("SELECT o FROM Order o WHERE o.stage != 'deleted' AND o.tenantId = :tenantId ORDER BY o.lastUpdated DESC")
    List<Order> findAllForTenant(@Param("tenantId") String tenantId);

    @Query("SELECT o FROM Order o WHERE o.stage != 'deleted' AND o.tenantId = :tenantId ORDER BY o.lastUpdated DESC")
    List<Order> findPageForTenant(@Param("tenantId") String tenantId,
            Pageable pageable);

    @Query("SELECT o FROM Order o WHERE o.stage IN :stage AND o.tenantId = :tenantId ORDER BY o.lastUpdated DESC")
    List<Order> findByStageForTenant(@Param("tenantId") String tenantId,
            @Param("stage") String stage);

    @Query("SELECT o FROM Order o WHERE o.stage != 'deleted' AND o.tenantId = :tenantId AND o.contactId = :contactId ORDER BY o.lastUpdated DESC")
    List<Order> findAllForContact(@Param("tenantId") String tenantId,
            @Param("contactId") Long contactId);

    @Query("SELECT o FROM Order o WHERE o.stage != 'deleted' AND o.tenantId = :tenantId AND o.contactId = :contactId ORDER BY o.lastUpdated DESC")
    List<Order> findPageForContact(@Param("tenantId") String tenantId,
            @Param("contactId") Long contactId,
            Pageable pageable);

    @Query("SELECT o FROM Order o WHERE o.stage != 'deleted' AND o.tenantId = :tenantId AND o.contactId IN :contactIds ORDER BY o.lastUpdated DESC")
    List<Order> findAllForContacts(@Param("tenantId") String tenantId,
            @Param("contactIds") Long[] contactIds);

    @Query("SELECT o FROM Order o WHERE o.tenantId = :tenantId AND o.contactId IN :contactIds ORDER BY o.lastUpdated DESC")
    List<Order> findPageForContacts(@Param("tenantId") String tenantId,
            @Param("contactIds") Long[] contactIds, Pageable pageable);

    @Override
    @Query("UPDATE #{#entityName} x set x.stage = 'deleted' where x.id = ?1")
    @Modifying(clearAutomatically = true)
    public void delete(Long id);

    @Query("DELETE FROM CustomOrderItemField i WHERE i.orderItem.id = ?1")
    @Modifying(clearAutomatically = true)
    public void deleteItemCustomField(Long orderItemId);

    @Query("DELETE FROM OrderItem i WHERE i.order.id = ?1 AND i.id = ?2")
    @Modifying(clearAutomatically = true)
    public void deleteItem(Long orderId, Long orderItemId);
}
