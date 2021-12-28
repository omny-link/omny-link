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

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import link.omny.custmgmt.model.Contact;

@RepositoryRestResource(exported = false)
public interface ContactRepository extends CrudRepository<Contact, Long> {

    @Override
    @EntityGraph("contactWithAll")
    Optional<Contact> findById(Long id);

    @Query("SELECT c FROM Contact c LEFT JOIN c.account a WHERE c.tenantId = :tenantId "
            + "AND (c.stage IS NULL OR c.stage != 'deleted') ORDER BY c.id ASC")
    @EntityGraph("contactWithAccount")
    List<Contact> findAllForTenant(@Param("tenantId") String tenantId);

    @Query("SELECT COUNT(c) FROM Contact c LEFT JOIN c.account a WHERE c.tenantId = :tenantId AND (c.stage IS NULL OR c.stage != 'deleted')")
    long countForTenant(@Param("tenantId") String tenantId);

    @Query("SELECT COUNT(c) FROM Contact c LEFT JOIN c.account a WHERE c.tenantId = :tenantId AND (c.stage IS NULL OR c.stage != 'deleted') AND c.alerts IS NOT NULL")
    long countAlertsForTenant(@Param("tenantId") String tenantId);

    @Query("SELECT c FROM Contact c LEFT JOIN c.account a WHERE c.tenantId = :tenantId AND (c.stage IS NULL OR c.stage != 'deleted') AND (owner = :userId) ORDER BY c.lastUpdated DESC")
    @EntityGraph("contactWithAccount")
    List<Contact> findAllForTenantOwnedByUser(
            @Param("tenantId") String tenantId, @Param("userId") String userId);

    @Query("SELECT c FROM Contact c LEFT JOIN c.account a WHERE c.tenantId = :tenantId AND c.doNotEmail = false AND (c.stage IS NULL OR c.stage != 'deleted') ORDER BY c.lastUpdated DESC")
    @EntityGraph("contactWithAccount")
    List<Contact> findAllEmailableForTenant(@Param("tenantId") String tenantId);

    @Query("SELECT c FROM Contact c LEFT JOIN c.account a WHERE c.tenantId = :tenantId AND (c.stage IS NULL OR c.stage != 'deleted') ORDER BY c.lastUpdated DESC")
    @EntityGraph("contactWithAccount")
    List<Contact> findPageForTenant(@Param("tenantId") String tenantId,
            Pageable pageable);

    @Query("SELECT c FROM Contact c LEFT JOIN c.account a WHERE c.tenantId = :tenantId AND c.doNotEmail = false AND (stage IS NULL OR stage != 'deleted') ORDER BY c.lastUpdated DESC")
    @EntityGraph("contactWithAccount")
    List<Contact> findEmailablePageForTenant(@Param("tenantId") String tenantId,
            Pageable pageable);

    @Query("SELECT c FROM Contact c "
            + "WHERE c.firstName = :firstName AND c.lastName = :lastName "
            + "AND c.account.name = :accountName "
            + "AND c.tenantId = :tenantId")
    List<Contact> findByFirstNameLastNameAndAccountNameForTenant(
            @Param("tenantId") String tenantId,
            @Param("firstName") String firstName,
            @Param("lastName") String lastName,
            @Param("accountName") String accountName);

    @Query("SELECT c FROM Contact c WHERE c.email = :email AND (c.stage IS NULL OR c.stage != 'deleted') AND c.tenantId = :tenantId")
    List<Contact> findByEmail(@Param("email") String email,
            @Param("tenantId") String tenantId);

    @Query("SELECT c FROM Contact c WHERE c.emailHash = :emailHash AND c.tenantId = :tenantId")
    List<Contact>  findByEmailHash(@Param("emailHash") String emailHash, @Param("tenantId") String tenantId);

    @Query("SELECT c FROM Contact c WHERE c.tags LIKE :tag AND (c.stage IS NULL OR c.stage != 'deleted') AND c.tenantId = :tenantId")
    List<Contact> findByTag(@Param("tag") String tag,
            @Param("tenantId") String tenantId);

    @Query("SELECT c.stage, COUNT(c) FROM Contact c WHERE (c.stage IS NULL OR c.stage != 'deleted') AND c.tenantId = :tenantId GROUP BY c.stage")
    List<Object[]> findAllForTenantGroupByStage(
            @Param("tenantId") String tenantId);

    @Query("SELECT c FROM Contact c WHERE c.uuid = :uuid AND (c.stage IS NULL OR c.stage != 'deleted') AND c.tenantId = :tenantId")
    List<Contact> findByUuid(@Param("uuid") String uuid,
            @Param("tenantId") String tenantId);

    @Query("SELECT c FROM Contact c WHERE c.uuid = :uuid AND c.email IS NOT NULL AND (c.stage IS NULL OR c.stage != 'deleted') AND c.tenantId = :tenantId")
    List<Contact> findKnownByUuid(@Param("uuid") String uuid,
            @Param("tenantId") String tenantId);

    @Query("SELECT c FROM Contact c WHERE c.uuid = :uuid AND c.firstName IS NULL AND c.lastName IS NULL AND (c.stage IS NULL OR c.stage != 'deleted') AND c.tenantId = :tenantId")
    List<Contact> findAnonByUuid(@Param("uuid") String uuid,
            @Param("tenantId") String tenantId);

    @Query("SELECT c FROM Contact c INNER JOIN c.activities a WHERE a.occurred > :sinceDate AND (c.stage IS NULL OR c.stage != 'deleted') AND c.tenantId = :tenantId ORDER BY a.occurred DESC")
    List<Contact> findActiveForTenant(@Param("sinceDate") Date sinceDate,
            @Param("tenantId") String tenantId);

    @Query("SELECT c FROM Contact c INNER JOIN c.account a WHERE a.id = :accountId AND (c.stage IS NULL OR c.stage != 'deleted') AND c.tenantId = :tenantId ORDER BY c.lastName DESC")
    List<Contact> findByAccountId(@Param("accountId") Long accountId,
            @Param("tenantId") String tenantId);

    @Query("SELECT c FROM Contact c WHERE c.accountType = :accountType AND (c.stage IS NULL OR c.stage != 'deleted') AND c.tenantId = :tenantId ORDER BY c.lastName DESC")
    List<Contact> findByAccountType(@Param("accountType") String accountType,
            @Param("tenantId") String tenantId);

    @Query("SELECT c FROM Contact c INNER JOIN c.customFields cf WHERE cf.name = :key AND cf.value = :value AND (c.stage IS NULL OR c.stage != 'deleted') AND c.tenantId = :tenantId")
    List<Contact> findByCustomField(@Param("key") String key, @Param("value") String value, @Param("tenantId") String tenantId);

    @Query(value = "SELECT DISTINCT(cf.name) FROM OL_CONTACT c INNER JOIN OL_CONTACT_CUSTOM cf on c.id = cf.contact_id WHERE (c.stage IS NULL OR c.stage != 'deleted') AND c.tenant_id = :tenantId ", nativeQuery = true)
    List<String> findCustomFieldNames(@Param("tenantId") String tenantId);

    @Query(value = "UPDATE OL_CONTACT c SET account_id = :accountId WHERE c.id = :contactId", nativeQuery = true)
    @Modifying(clearAutomatically = true)
    void setAccount(@Param("contactId") Long contactId, @Param("accountId") Long accountId);

    @Query(value = "UPDATE Contact c SET c.stage = :stage WHERE (c.lastUpdated < :before OR c.lastUpdated IS NULL) AND c.stage NOT IN ('deleted','Cold') AND c.tenantId = :tenantId")
    @Modifying(clearAutomatically = true)
    int updateStage(@Param("stage") String stage, @Param("before") Date before, @Param("tenantId") String tenantId);

    @Override
    @Query("UPDATE #{#entityName} x SET x.stage = 'deleted' WHERE x.id = :id")
    @Modifying(clearAutomatically = true)
    void deleteById(@Param("id") Long id);

}
