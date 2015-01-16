package link.omny.custmgmt.repositories;

import link.omny.custmgmt.model.CustomField;

import org.springframework.data.repository.CrudRepository;

public interface CustomFieldRepository extends
        CrudRepository<CustomField, Long> {

    // List<Note> findByContact(@Param("lastName") String lastName);

}
