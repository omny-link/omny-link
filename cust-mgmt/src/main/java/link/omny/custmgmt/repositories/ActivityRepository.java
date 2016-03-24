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

    @Query("UPDATE #{#entityName} x set x.contact = :newContact where x.contact = :oldContact")
    @Modifying(clearAutomatically = true)
    public void updateContact(@Param("oldContact") Contact oldContact,
            @Param("newContact") Contact newContact);
}
