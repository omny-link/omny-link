package link.omny.custmgmt.repositories;

import java.util.Date;
import java.util.List;

import link.omny.custmgmt.model.Contact;
import link.omny.custmgmt.model.ContactExcept;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(excerptProjection = ContactExcept.class, path = "/contacts")
public interface ContactRepository extends CrudRepository<Contact, Long> {

    @Query("SELECT c FROM Contact c LEFT JOIN c.account a WHERE c.tenantId = :tenantId AND (stage IS NULL OR stage != 'deleted') ORDER BY c.lastUpdated DESC")
    List<Contact> findAllForTenant(@Param("tenantId") String tenantId);

    @Query("SELECT c FROM Contact c LEFT JOIN c.account a WHERE c.tenantId = :tenantId AND (stage IS NULL OR stage != 'deleted') AND (owner = :userId) ORDER BY c.lastUpdated DESC")
    List<Contact> findAllForTenantOwnedByUser(
            @Param("tenantId") String tenantId, @Param("userId") String userId);

    @Query("SELECT c FROM Contact c LEFT JOIN c.account a WHERE c.tenantId = :tenantId AND c.doNotEmail = false AND (stage IS NULL OR stage != 'deleted') ORDER BY c.lastUpdated DESC")
    List<Contact> findAllEmailableForTenant(@Param("tenantId") String tenantId);

    @Query("SELECT c FROM Contact c LEFT JOIN c.account a WHERE c.tenantId = :tenantId AND (stage IS NULL OR stage != 'deleted') ORDER BY c.lastUpdated DESC")
    List<Contact> findPageForTenant(@Param("tenantId") String tenantId,
            Pageable pageable);

    @Query("SELECT c FROM Contact c LEFT JOIN c.account a WHERE c.tenantId = :tenantId AND c.doNotEmail = false AND (stage IS NULL OR stage != 'deleted') ORDER BY c.lastUpdated DESC")
    List<Contact> findEmailablePageForTenant(@Param("tenantId") String tenantId,
            Pageable pageable);

    List<Contact> findByLastName(@Param("lastName") String lastName);

    // This applies AND semantics
    List<Contact> findByFirstNameOrLastName(
            @Param("firstName") String firstName,
            @Param("lastName") String lastName);

    // This appears to match everything :-(
    List<Contact> findByFirstNameOrLastNameOrAccountName(
            @Param("firstName") String firstName,
            @Param("lastName") String lastName,
            @Param("account.name") String accountName);

    @Query("SELECT c FROM Contact c "
            + "WHERE c.firstName = :firstName AND c.lastName = :lastName ")
    // TODO ought to restrict this to single tenant
    List<Contact> findByFirstNameAndLastName(
            @Param("firstName") String firstName,
            @Param("lastName") String lastName);

    @Query("SELECT c FROM Contact c "
            + "WHERE c.firstName = :firstName AND c.lastName = :lastName "
            + "AND c.account.name = :accountName")
    // TODO ought to restrict this to single tenant
    List<Contact> findByFirstNameLastNameAndAccountName(
            @Param("firstName") String firstName,
            @Param("lastName") String lastName,
            @Param("accountName") String accountName);

    @Query("SELECT c FROM Contact c WHERE c.email = :email AND (stage IS NULL OR stage != 'deleted') AND c.tenantId = :tenantId")
    List<Contact> findByEmail(@Param("email") String email,
            @Param("tenantId") String tenantId);

    @Query("SELECT c FROM Contact c WHERE c.tags LIKE :tag AND (stage IS NULL OR stage != 'deleted') AND c.tenantId = :tenantId")
    List<Contact> findByTag(@Param("tag") String tag,
            @Param("tenantId") String tenantId);

    @Query("SELECT c.stage, COUNT(c) FROM Contact c WHERE (stage IS NULL OR stage != 'deleted') AND c.tenantId = :tenantId GROUP BY c.stage")
    List<Object[]> findAllForTenantGroupByStage(
            @Param("tenantId") String tenantId);

    @Query("SELECT c FROM Contact c WHERE c.uuid = :uuid AND (stage IS NULL OR stage != 'deleted') AND c.tenantId = :tenantId")
    List<Contact> findByUuid(@Param("uuid") String uuid,
            @Param("tenantId") String tenantId);

    @Query("SELECT c FROM Contact c WHERE c.uuid = :uuid AND c.email IS NOT NULL AND (stage IS NULL OR stage != 'deleted') AND c.tenantId = :tenantId")
    List<Contact> findKnownByUuid(@Param("uuid") String uuid,
            @Param("tenantId") String tenantId);

    @Query("SELECT c FROM Contact c WHERE c.uuid = :uuid AND c.firstName IS NULL AND c.lastName IS NULL AND (stage IS NULL OR stage != 'deleted') AND c.tenantId = :tenantId")
    List<Contact> findAnonByUuid(@Param("uuid") String uuid,
            @Param("tenantId") String tenantId);

    @Query("SELECT c FROM Contact c INNER JOIN c.activity a WHERE a.occurred > :sinceDate AND (stage IS NULL OR stage != 'deleted') AND c.tenantId = :tenantId ORDER BY a.occurred DESC")
    List<Contact> findActiveForTenant(@Param("sinceDate") Date sinceDate,
            @Param("tenantId") String tenantId);

    @Query(value = "UPDATE OL_CONTACT c set c.account_id = ?2 WHERE c.id = ?1", nativeQuery = true)
    @Modifying(clearAutomatically = true)
    public void setAccount(Long contactId, Long accountId);

    @Override
    @Query("UPDATE #{#entityName} x set x.stage = 'deleted' where x.id = ?1")
    @Modifying(clearAutomatically = true)
    public void delete(Long id);

}
