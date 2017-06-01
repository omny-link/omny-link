package link.omny.catalog.repositories;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import link.omny.catalog.model.MediaResource;

@RepositoryRestResource(path = "/media")
public interface MediaResourceRepository extends
        CrudRepository<MediaResource, Long> {

    List<MediaResource> findByStockItemId(@Param("stockItemId") Long stockItemId);

    List<MediaResource> findByStockCategoryId(@Param("stockCategoryId") Long stockCategoryId);

}
