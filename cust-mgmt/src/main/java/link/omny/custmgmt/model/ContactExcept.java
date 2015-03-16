package link.omny.custmgmt.model;

import java.util.Date;

import org.springframework.data.rest.core.config.Projection;

@Projection(name = "excerpt", types = { Account.class, Contact.class })
public interface ContactExcept {

    Long getId();

    String getFirstName();

    String getLastName();

    String getTitle();

    String getEmail();

    String getPhone1();

    String getStage();

    String getOwner();

    Date getFirstContact();

    Date getLastUpdated();

    String getTenantId();

    Account getAccount();
}
