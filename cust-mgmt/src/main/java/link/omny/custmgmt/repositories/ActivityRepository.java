package link.omny.custmgmt.repositories;

import link.omny.custmgmt.model.Activity;
import link.omny.custmgmt.model.Contact;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(path = "/activities")
public interface ActivityRepository extends CrudRepository<Activity, Long> {

    // @Query("SELECT a FROM Activity a INNER JOIN a.contact c WHERE a.occurred < :time AND a.type = :type AND a.content = :content AND c.tenantId = :tenantId ORDER BY a.occurred DESC")
    // public List<Activity> findByTimeTypeAndContent(@Param("time") String
    // time,
    // @Param("type") String type, @Param("content") String content,
    // @Param("tenantId") String tenantId);

    @Query("UPDATE #{#entityName} x set x.contact = :newContact where x.contact = :oldContact")
    @Modifying(clearAutomatically = true)
    public void updateContact(@Param("oldContact") Contact oldContact,
            @Param("newContact") Contact newContact);
}
