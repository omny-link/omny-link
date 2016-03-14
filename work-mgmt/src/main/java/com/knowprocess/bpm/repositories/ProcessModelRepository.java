package com.knowprocess.bpm.repositories;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import com.knowprocess.bpm.model.ProcessModel;

@RepositoryRestResource(path = "/process-models")
public interface ProcessModelRepository extends
        CrudRepository<ProcessModel, String> {

    @Override
    @Query("SELECT m FROM ProcessModel m INNER JOIN m.issues i WHERE m.id = ?1")
    public ProcessModel findOne(String id);

    @Query("SELECT c FROM ProcessModel c WHERE c.tenantId = :tenantId ORDER BY c.lastUpdated DESC")
    List<ProcessModel> findAllForTenant(@Param("tenantId") String tenantId);

    @Query("SELECT c FROM ProcessModel c WHERE c.tenantId = :tenantId ORDER BY c.lastUpdated DESC")
    List<ProcessModel> findPageForTenant(@Param("tenantId") String tenantId,
            Pageable pageable);

}
