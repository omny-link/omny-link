/*******************************************************************************
 *Copyright 2018 Tim Stephenson and contributors
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
