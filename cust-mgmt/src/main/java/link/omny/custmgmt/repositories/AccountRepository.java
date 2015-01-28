package link.omny.custmgmt.repositories;

import java.util.List;

import link.omny.custmgmt.model.Account;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface AccountRepository extends
		CrudRepository<Account, Long> {

	List<Account> findByName(@Param("name") String ame);

    // //
    // @Query("select m from AppModel m where m.namespace = ?1 and m.name = ?2")
    // Customer findByName(@Param("namespace") String namespace,
    // @Param("name") String name);
}
