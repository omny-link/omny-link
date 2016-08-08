package link.omny.custmgmt.model;

import org.springframework.data.rest.core.config.Projection;

@Projection(name = "typeahead", types = { Account.class })
public interface AccountTypeaheadProjection {

    Long getId();

    String getName();

}
