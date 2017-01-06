package link.omny.custmgmt.repositories;

import java.util.List;

import link.omny.custmgmt.model.CustomContactField;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(exported = false)
public interface CustomContactFieldRepository extends
        CrudRepository<CustomContactField, Long> {

    @Query("SELECT a FROM CustomContactField a WHERE a.contact.id = :contactId")
    List<CustomContactField> findByContactId(@Param("contactId") Long contactId);
}
