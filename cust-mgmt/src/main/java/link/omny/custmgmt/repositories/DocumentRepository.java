package link.omny.custmgmt.repositories;

import link.omny.custmgmt.model.Document;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(path = "/documents")
public interface DocumentRepository extends CrudRepository<Document, Long> {

}
