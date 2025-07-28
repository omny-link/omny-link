/*******************************************************************************
 * Copyright 2015-2025 Tim Stephenson and contributors
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
package link.omny.catalog.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import link.omny.catalog.model.StockCategory;

@RepositoryRestResource(exported = false)
public interface StockCategoryRepository extends
        CrudRepository<StockCategory, Long> {

    @Override
    @EntityGraph(value = "stockCategoryWithAll")
    Optional<StockCategory> findById(Long id);

    @Query("SELECT c FROM StockCategory c WHERE c.name = :name AND c.tenantId = :tenantId")
    @EntityGraph("stockCategoryWithAll")
    StockCategory findByName(@Param("name") String name,
            @Param("tenantId") String tenantId);

    @Query("SELECT c FROM StockCategory c WHERE c.tenantId = :tenantId "
            + "AND (c.status IS NULL OR c.status != 'deleted') ORDER BY c.name ASC")
    List<StockCategory> findAllForTenant(@Param("tenantId") String tenantId);

    @Query("SELECT COUNT(c) FROM StockCategory c WHERE c.tenantId = :tenantId "
            + "AND (c.status IS NULL OR c.status != 'deleted')")
    long countForTenant(@Param("tenantId") String tenantId);

    @Query("SELECT c FROM StockCategory c WHERE c.tenantId = :tenantId "
            + "AND (c.status IS NULL OR c.status != 'deleted') ORDER BY c.name ASC")
    List<StockCategory> findPageForTenant(@Param("tenantId") String tenantId,
            Pageable pageable);

    @Query("SELECT c FROM StockCategory c WHERE c.status IN :status AND c.tenantId = :tenantId ORDER BY c.name ASC")
    List<StockCategory> findByStatusForTenant(
            @Param("tenantId") String tenantId, @Param("status") String status);

    @Query("SELECT c FROM StockCategory c "
            + "WHERE c.status IN :status AND c.tenantId = :tenantId "
            + "AND c.offerStatus = :offerStatus ORDER BY c.name ASC")
    List<StockCategory> findByStatusAndOffersForTenant(
            @Param("tenantId") String tenantId, @Param("status") String status,
            @Param("offerStatus") String offerStatus);

    @Query(value = "SELECT DISTINCT(cf.name) FROM OL_STOCK_CAT o "
            + "INNER JOIN OL_STOCK_CAT_CUSTOM cf on o.id = cf.stock_cat_id "
            + "WHERE o.tenant_id = :tenantId ", nativeQuery = true)
    List<String> findCustomFieldNames(@Param("tenantId") String tenantId);

    @Override
    @Query("UPDATE #{#entityName} x set x.status = 'deleted', lastUpdated = CURRENT_TIMESTAMP where x.id = :id")
    @Modifying(clearAutomatically = true)
    public void deleteById(@Param("id") Long id);
}
