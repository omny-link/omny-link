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

    // TODO compare perf of correlated sub-query
    // http://openjpa.apache.org/builds/1.2.0/apache-openjpa-1.2.0/docs/manual/jpa_langref.html#jpa_langref_exists
    @Query("SELECT o FROM Order o WHERE o.tenantId = :tenantId AND o.contactId IN (SELECT c FROM Contact c WHERE c.account.id = :accountId) ORDER BY o.lastUpdated DESC")
    List<Order> findAllForAccount(@Param("tenantId") String tenantId,
            @Param("accountId") Long accountId);

    @Query("SELECT o FROM Order o WHERE o.tenantId = :tenantId AND o.contactId IN (SELECT c FROM Contact c WHERE c.account.id = :accountId) ORDER BY o.lastUpdated DESC")
    List<Order> findPageForAccount(@Param("tenantId") String tenantId,
            @Param("accountId") Long accountId,
            Pageable pageable);

    @Query("SELECT o FROM Order o WHERE o.tenantId = :tenantId AND o.contactId = :contactId ORDER BY o.lastUpdated DESC")
    List<Order> findAllForContact(@Param("tenantId") String tenantId,
            @Param("contactId") Long contactId);

    @Query("SELECT o FROM Order o WHERE o.tenantId = :tenantId AND o.contactId = :contactId ORDER BY o.lastUpdated DESC")
    List<Order> findPageForContact(@Param("tenantId") String tenantId,
            @Param("contactId") Long contactId,
            Pageable pageable);
}
