package com.knowprocess.bpm.decisions.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import com.knowprocess.bpm.decisions.model.DecisionModel;

@RepositoryRestResource(path = "/decision-models")
public interface DecisionModelRepository extends
        CrudRepository<DecisionModel, Long> {

    DecisionModel findByName(@Param("tenantId") String tenantId,
            @Param("decisionName") String decisionName);

    // @Query("SELECT c FROM DecisionModel c WHERE c.tenantId = :tenantId ORDER BY c.lastUpdated DESC")
    // List<DecisionModel> findAllForTenant(@Param("tenantId") String tenantId);
}
