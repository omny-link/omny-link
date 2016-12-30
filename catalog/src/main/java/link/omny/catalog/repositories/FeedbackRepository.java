package link.omny.catalog.repositories;

import link.omny.catalog.model.Feedback;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(exported = false)
public interface FeedbackRepository extends CrudRepository<Feedback, Long> {

    @Query("SELECT o FROM Feedback o WHERE o.tenantId = :tenantId AND o.order.id = :orderId")
    Feedback findByOrder(@Param("tenantId") String tenantId,
            @Param("orderId") Long orderId);
}
