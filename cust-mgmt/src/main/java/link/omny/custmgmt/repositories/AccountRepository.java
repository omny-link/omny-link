package link.omny.custmgmt.repositories;

import java.util.List;

import link.omny.custmgmt.model.Account;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(path = "/accounts")
public interface AccountRepository extends CrudRepository<Account, Long> {

    List<Account> findByName(@Param("name") String name);

}
