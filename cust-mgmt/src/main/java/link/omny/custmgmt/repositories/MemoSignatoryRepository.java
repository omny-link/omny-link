package link.omny.custmgmt.repositories;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import link.omny.custmgmt.model.MemoSignatory;

@RepositoryRestResource(exported = false)
public interface MemoSignatoryRepository extends CrudRepository<MemoSignatory, Long> {

    @Query("DELETE #{#entityName} x where x.memo.id = :memoId")
    @Modifying(clearAutomatically = true)
    void deleteAllForMemo(@Param("memoId") Long memoId);

}
