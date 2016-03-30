package link.omny.catalog.repositories;

import link.omny.catalog.model.MediaResource;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(path = "/media")
public interface MediaResourceRepository extends
        CrudRepository<MediaResource, Long> {

}
