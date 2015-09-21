package link.omny.custmgmt.repositories;

import link.omny.custmgmt.model.Note;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(path = "/notes")
public interface NoteRepository extends CrudRepository<Note, Long> {

}
