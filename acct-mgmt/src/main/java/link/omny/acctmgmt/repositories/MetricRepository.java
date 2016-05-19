package link.omny.acctmgmt.repositories;

import java.util.Date;
import java.util.List;

import link.omny.acctmgmt.model.Metric;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(path = "/metrics")
public interface MetricRepository extends CrudRepository<Metric, String> {

    @Query("SELECT c FROM Metric c WHERE c.occurred >= :since ORDER BY c.occurred DESC")
    List<Metric> findAllSinceDate(@Param("since") Date date);

    @Query("SELECT c FROM Metric c WHERE c.tenantId = :tenantId AND c.occurred >= :since ORDER BY c.occurred DESC")
    List<Metric> findAllSinceDateForTenant(@Param("since") Date date,
            @Param("tenantId") String tenantId);

    @Query("SELECT c FROM Metric c WHERE c.name = :name AND c.tenantId = :tenantId AND c.occurred >= :since ORDER BY c.occurred DESC")
    List<Metric> findByNameSinceDateForTenant(@Param("name") String name,
            @Param("since") Date date, @Param("tenantId") String tenantId);

}
