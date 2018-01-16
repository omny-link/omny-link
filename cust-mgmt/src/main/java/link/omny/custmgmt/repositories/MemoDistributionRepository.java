/*******************************************************************************
 *Copyright 2015-2018 Tim Stephenson and contributors
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
package link.omny.custmgmt.repositories;

import java.util.List;

import link.omny.custmgmt.model.MemoDistribution;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(path = "/memo-distributions")
public interface MemoDistributionRepository extends CrudRepository<MemoDistribution, Long> {

    @Query("SELECT m FROM MemoDistribution m WHERE m.tenantId = :tenantId AND (status IS NULL OR status != 'deleted') ORDER BY m.lastUpdated DESC")
    List<MemoDistribution> findAllForTenant(@Param("tenantId") String tenantId);

    @Query("SELECT m FROM MemoDistribution m WHERE m.tenantId = :tenantId AND (status IS NULL OR status != 'deleted') ORDER BY m.lastUpdated DESC")
    List<MemoDistribution> findPageForTenant(@Param("tenantId") String tenantId,
            Pageable pageable);
}
