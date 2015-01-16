package link.omny.custmgmt.repositories;

import link.omny.custmgmt.model.Note;

import org.springframework.data.repository.CrudRepository;

//@RestResource(exported = false)
public interface NoteRepository extends CrudRepository<Note, Long> {

    // List<Note> findByContact(@Param("lastName") String lastName);

}
