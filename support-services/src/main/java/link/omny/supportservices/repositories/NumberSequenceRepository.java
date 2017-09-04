package link.omny.supportservices.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import link.omny.supportservices.model.NumberSequence;

@RepositoryRestResource(exported = false)
public interface NumberSequenceRepository
        extends CrudRepository<NumberSequence, Long> {

    @Query("SELECT o FROM NumberSequence o WHERE o.name = :name AND o.tenantId = :tenantId")
    List<NumberSequence> findByEntityNameForTenant(
            @Param("name") String name,
            @Param("tenantId") String tenantId);

}
