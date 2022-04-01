/*******************************************************************************
 * Copyright 2015-2022 Tim Stephenson and contributors
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

import link.omny.catalog.model.StockItem;

@RepositoryRestResource(exported = false)
public interface StockItemRepository extends CrudRepository<StockItem, Long> {

    @Override
    @EntityGraph(value = "stockItemWithAll")
    Optional<StockItem> findById(Long id);

    StockItem findByName(@Param("name") String name);

    @Query("SELECT i FROM StockItem i WHERE i.tenantId = :tenantId AND (i.status IS NULL OR i.status != 'deleted') ORDER BY i.name ASC")
    @EntityGraph("stockItemWithAll")
    List<StockItem> findAllForTenant(@Param("tenantId") String tenantId);

    @Query("SELECT COUNT(i) FROM StockItem i WHERE i.tenantId = :tenantId AND (i.status IS NULL OR i.status != 'deleted')")
    long countForTenant(@Param("tenantId") String tenantId);

    @Query("SELECT i FROM StockItem i WHERE i.tenantId = :tenantId AND (i.status IS NULL OR i.status != 'deleted') ORDER BY i.name ASC")
    List<StockItem> findPageForTenant(@Param("tenantId") String tenantId,
            Pageable pageable);

    @Query("SELECT i FROM StockItem i WHERE i.tenantId = :tenantId AND LOWER(i.status) = :status ORDER BY i.name ASC")
    List<StockItem> findByStatusForTenant(@Param("status") String status,
            @Param("tenantId") String tenantId);

    @Query("SELECT i FROM StockItem i WHERE i.tenantId = :tenantId AND LOWER(i.status) = :status ORDER BY i.name ASC")
    List<StockItem> findPageByStatusForTenant(@Param("status") String status,
            @Param("tenantId") String tenantId, Pageable pageable);

    @Query("SELECT i FROM StockItem i INNER JOIN i.stockCategory c WHERE i.tenantId = :tenantId AND LOWER(c.name) LIKE :categoryName AND (i.status IS NULL OR i.status != 'deleted') ORDER BY i.name ASC")
    List<StockItem> findAllForCategoryName(
            @Param("categoryName") String categoryName,
            @Param("tenantId") String tenantId);

    @Query(value = "SELECT DISTINCT(cf.name) FROM OL_STOCK_ITEM o INNER JOIN OL_STOCK_ITEM_CUSTOM cf on o.id = cf.stock_item_id WHERE o.tenant_id = :tenantId ", nativeQuery = true)
    List<String> findCustomFieldNames(@Param("tenantId") String tenantId);

    @Query(value = "UPDATE OL_STOCK_ITEM i SET stock_cat_id = :categoryId WHERE id = :itemId", nativeQuery = true)
    @Modifying(clearAutomatically = true)
    public void setStockCategory(@Param("itemId") Long itemId, @Param("categoryId") Long categoryId);

    @Override
    @Query("UPDATE #{#entityName} x set x.status = 'deleted', lastUpdated = CURRENT_TIMESTAMP where x.id = :id")
    @Modifying(clearAutomatically = true)
    public void deleteById(@Param("id") Long id);
}
