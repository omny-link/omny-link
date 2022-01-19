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
package link.omny.custmgmt.repositories;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import link.omny.custmgmt.model.Account;

@RepositoryRestResource(exported = false)
public interface AccountRepository extends JpaRepository<Account, Long>,
        JpaSpecificationExecutor<Account> {
    @Override
    @EntityGraph(value = "accountWithAll")
    Optional<Account> findById(Long id);

    @Query("SELECT a FROM Account a INNER JOIN a.customFields c WHERE (a.stage IS NULL OR a.stage != 'deleted') AND a.tenantId = :tenantId AND c.name='orgCode' AND c.value = :code ORDER BY a.lastUpdated DESC")
    Account findByCodeForTenant(@Param("code") String code, @Param("tenantId") String tenantId);

    @Query("SELECT a FROM Account a WHERE a.name = :name AND (a.stage IS NULL OR a.stage != 'deleted') AND a.tenantId = :tenantId ORDER BY a.lastUpdated DESC")
    Account findByNameForTenant(@Param("name") String name, @Param("tenantId") String tenantId);

    @Query("SELECT a FROM Account a INNER JOIN a.customFields c "
            + "WHERE (a.stage IS NULL OR a.stage != 'deleted') "
            + "AND a.tenantId = :tenantId AND c.name=:fieldName "
            + "AND c.value = :fieldValue ORDER BY a.lastUpdated DESC")
    List<Account> findByCustomFieldForTenant(@Param("fieldName") String fieldName,
            @Param("fieldValue") String fieldValue,
            @Param("tenantId") String tenantId);

    @Query("SELECT COUNT(a) FROM Account a WHERE a.tenantId = :tenantId AND (a.stage IS NULL OR a.stage != 'deleted')")
    long countForTenant(@Param("tenantId") String tenantId);

    @Query("SELECT a.stage, COUNT(a) FROM Account a WHERE (a.stage IS NULL OR a.stage != 'deleted') AND a.tenantId = :tenantId GROUP BY a.stage")
    List<Object[]> findAllForTenantGroupByStage(
            @Param("tenantId") String tenantId);

    @Query(value = "SELECT DISTINCT(cf.name) FROM OL_ACCOUNT a INNER JOIN OL_ACCOUNT_CUSTOM cf on a.id = cf.account_id WHERE (a.stage IS NULL OR a.stage != 'deleted') AND a.tenant_id = :tenantId ", nativeQuery = true)
    List<String> findCustomFieldNames(@Param("tenantId") String tenantId);

    @Query(value = "UPDATE Account a set a.stage = :stage WHERE (a.lastUpdated < :before OR a.lastUpdated IS NULL) AND a.stage != 'deleted'AND a.tenantId = :tenantId")
    @Modifying(clearAutomatically = true)
    int updateStage(@Param("stage") String stage, @Param("before") Date before, @Param("tenantId") String tenantId);

    @Override
    @Query("UPDATE #{#entityName} x set x.stage = 'deleted' where x.id = :id")
    @Modifying(clearAutomatically = true)
    public void deleteById(@Param("id") Long id);

}
