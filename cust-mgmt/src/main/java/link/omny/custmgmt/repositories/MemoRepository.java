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

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import link.omny.custmgmt.model.Memo;

@RepositoryRestResource(path = "/memos")
public interface MemoRepository extends CrudRepository<Memo, Long> {

    @Override
    @Query("SELECT m FROM Memo m WHERE (m.status IS NULL OR m.status != 'deleted') AND m.id = :id")
    Memo findOne(@Param("id") Long id);

    @Query("SELECT m FROM Memo m WHERE m.tenantId = :tenantId AND (status IS NULL OR status != 'deleted') ORDER BY m.lastUpdated DESC")
    List<Memo> findAllForTenant(@Param("tenantId") String tenantId);

    @Query("SELECT m FROM Memo m WHERE m.tenantId = :tenantId AND (status IS NULL OR status != 'deleted') ORDER BY m.lastUpdated DESC")
    List<Memo> findPageForTenant(@Param("tenantId") String tenantId,
            Pageable pageable);

    @Query("SELECT m FROM Memo m WHERE m.tenantId = :tenantId AND LOWER(m.status) = :status ORDER BY m.name ASC")
    List<Memo> findByStatusForTenant(@Param("status") String status,
            @Param("tenantId") String tenantId);

    @Query("SELECT m FROM Memo m WHERE m.tenantId = :tenantId AND LOWER(m.status) = :status ORDER BY m.name ASC")
    List<Memo> findPageByStatusForTenant(@Param("status") String status,
            @Param("tenantId") String tenantId, Pageable pageable);

    @Query("SELECT m FROM Memo m WHERE m.tenantId = :tenantId AND m.name = :name AND (status IS NULL OR status != 'deleted')")
    Memo findByName(@Param("name") String name,
            @Param("tenantId") String tenantId);
    
    @Override
    @Query("UPDATE #{#entityName} x set x.status = 'deleted' where x.id = :id")
    @Modifying(clearAutomatically = true)
    void delete(@Param("id") Long id);
}
