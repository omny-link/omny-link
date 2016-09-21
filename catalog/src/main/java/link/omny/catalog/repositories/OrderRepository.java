package link.omny.catalog.repositories;

import java.util.List;

import link.omny.catalog.model.Order;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(path = "/orders")
public interface OrderRepository extends CrudRepository<Order, Long> {

    @Query("SELECT o FROM Order o WHERE o.tenantId = :tenantId ORDER BY o.lastUpdated DESC")
    List<Order> findAllForTenant(@Param("tenantId") String tenantId);

    @Query("SELECT o FROM Order o WHERE o.tenantId = :tenantId ORDER BY o.lastUpdated DESC")
    List<Order> findPageForTenant(@Param("tenantId") String tenantId,
            Pageable pageable);

    @Query("SELECT o FROM Order o WHERE o.status IN :status AND o.tenantId = :tenantId ORDER BY o.lastUpdated DESC")
    List<Order> findByStatusForTenant(
            @Param("tenantId") String tenantId, @Param("status") String status);

    @Query("SELECT o FROM Order o WHERE o.contactId = :contactId ORDER BY o.lastUpdated DESC")
    List<Order> findAllForContact(@Param("contactId") Long tenantId);

    @Query("SELECT o FROM Order o WHERE o.contactId = :contactId ORDER BY o.lastUpdated DESC")
    List<Order> findPageForContact(@Param("contactId") Long contactId,
            Pageable pageable);
}
