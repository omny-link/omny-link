package link.omny.custmgmt.repositories;

import java.util.List;

import link.omny.custmgmt.model.Contact;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface ContactRepository extends CrudRepository<Contact, Long> {

    // @Query
    // List<Contact> findAll();

    @Query("select c,a from Contact c JOIN c.account a where c.lastName = :lastName")
    List<Contact> findByLastName(@Param("lastName") String lastName);

    @Query("select c from Contact c where c.tenantId = :tenantId")
    List<Contact> findAllForTenant(@Param("tenantId") String tenantId);

    // //
    // @Query("select m from AppModel m where m.namespace = ?1 and m.name = ?2")
    // Customer findByName(@Param("namespace") String namespace,
    // @Param("name") String name);
}
