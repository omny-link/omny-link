package link.omny.custmgmt.repositories;

import java.util.List;

import link.omny.custmgmt.model.Memo;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(path = "/memos")
public interface MemoRepository extends CrudRepository<Memo, Long> {

    @Query("SELECT m FROM Memo m WHERE m.tenantId = :tenantId AND (status IS NULL OR status != 'deleted') ORDER BY m.lastUpdated DESC")
    List<Memo> findAllForTenant(@Param("tenantId") String tenantId);

    @Query("SELECT m FROM Memo m WHERE m.tenantId = :tenantId AND (status IS NULL OR status != 'deleted') ORDER BY m.lastUpdated DESC")
    List<Memo> findPageForTenant(@Param("tenantId") String tenantId,
            Pageable pageable);

    @Override
    @Query("UPDATE #{#entityName} x set x.status = 'deleted' where x.id = ?1")
    @Modifying(clearAutomatically = true)
    void delete(Long id);

    @Query("SELECT m FROM Memo m WHERE m.tenantId = :tenantId AND m.name = :name")
    Memo findByName(@Param("name") String name,
            @Param("tenantId") String tenantId);
}
