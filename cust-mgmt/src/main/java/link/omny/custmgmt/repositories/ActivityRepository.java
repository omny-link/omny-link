package link.omny.custmgmt.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import link.omny.custmgmt.model.Activity;

@RepositoryRestResource(path = "/activities")
public interface ActivityRepository extends CrudRepository<Activity, Long> {

//    @Query("SELECT a FROM Activity a JOIN a.contact c WHERE c.account.id = :accountId ORDER BY a.lastUpdated ASC")
//    List<Activity> findByAccountId(@Param("accountId") Long accountId);
//
//    @Query("SELECT a FROM Activity a WHERE a.contact.id = :contactId ORDER BY a.lastUpdated ASC")
//    List<Activity> findByContactId(@Param("contactId") Long contactId);

//    @Query("UPDATE #{#entityName} x set x.contact = :newContact where x.contact = :oldContact")
//    @Modifying(clearAutomatically = true)
//    public void updateContact(@Param("oldContact") Contact oldContact,
//            @Param("newContact") Contact newContact);

}
