package link.omny.custmgmt.repositories;

import java.util.List;

import link.omny.custmgmt.model.Contact;
import link.omny.custmgmt.model.ContactExcept;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(excerptProjection = ContactExcept.class, path = "/contacts")
public interface ContactRepository extends CrudRepository<Contact, Long> {

    @Query("select c from Contact c LEFT JOIN c.account a WHERE c.tenantId = :tenantId")
    List<Contact> findAllForTenant(@Param("tenantId") String tenantId);

    List<Contact> findByLastName(@Param("lastName") String lastName);

    // This does not work
    List<Contact> findByFirstNameAndLastName(
            @Param("firstName") String firstName,
            @Param("lastName") String lastName);

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
            + "WHERE c.firstName = :firstName AND c.lastName = :lastName "
            + "AND c.account.name = :accountName")
    List<Contact> findByFirstNameLastNameAndAccountName(
            @Param("firstName") String firstName,
            @Param("lastName") String lastName,
            @Param("accountName") String accountName);

}
