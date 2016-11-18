package link.omny.catalog.repositories;

import link.omny.catalog.model.OrderItem;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(exported = false)
public interface OrderItemRepository extends CrudRepository<OrderItem, Long> {

}
