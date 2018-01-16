/*******************************************************************************
 *Copyright 2011-2018 Tim Stephenson and contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
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
    @Query("SELECT m FROM ProcessModel m INNER JOIN m.issues i WHERE m.id = :id")
    public ProcessModel findOne(@Param("id") String id);

    @Query("SELECT m FROM ProcessModel m WHERE m.id LIKE CONCAT(:id,'%') AND m.tenantId = :tenantId AND m.version = (SELECT MAX(n.version) FROM ProcessModel n WHERE n.id LIKE CONCAT(:id,'%') AND n.tenantId = :tenantId)")
    ProcessModel findLatestForTenant(@Param("id") String id, @Param("tenantId") String tenantId);

    @Query("SELECT c FROM ProcessModel c WHERE c.tenantId = :tenantId ORDER BY c.lastUpdated DESC")
    List<ProcessModel> findAllForTenant(@Param("tenantId") String tenantId);

    @Query("SELECT c FROM ProcessModel c WHERE c.tenantId = :tenantId ORDER BY c.lastUpdated DESC")
    List<ProcessModel> findPageForTenant(@Param("tenantId") String tenantId,
            Pageable pageable);

}
