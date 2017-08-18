package link.omny.supportservices.repositories;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import link.omny.supportservices.model.NumberFountain;

@RepositoryRestResource(exported = false)
public interface NumberFountainRepository
        extends CrudRepository<NumberFountain, Long> {

    List<NumberFountain> findByEntityName(
            @Param("entityName") String entityName);

}
