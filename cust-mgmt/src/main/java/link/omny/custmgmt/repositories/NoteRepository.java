package link.omny.custmgmt.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import link.omny.custmgmt.model.Note;

@RepositoryRestResource(path = "/notes")
public interface NoteRepository extends CrudRepository<Note, Long> {

//    List<Note> findByContactId(@Param("contactId") Long contactId);

}
