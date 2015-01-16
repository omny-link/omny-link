package link.omny.custmgmt.repositories;

import link.omny.custmgmt.model.Document;

import org.springframework.data.repository.CrudRepository;

//@RestResource(exported = false)
public interface DocumentRepository extends CrudRepository<Document, Long> {

    // List<Note> findByContact(@Param("lastName") String lastName);

}
