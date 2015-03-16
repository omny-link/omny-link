package link.omny.custmgmt.model;

import java.util.Date;
import java.util.List;

import org.springframework.data.rest.core.config.Projection;

@Projection(name = "complete", types = { Account.class, Contact.class,
        Document.class, Note.class })
public interface ContactAndAccountProjection {

    Long getId();

    String getFirstName();

    String getLastName();

    String getTitle();

    String getEmail();

    String getPhone1();

    String getPhone2();

    String getAddress1();

    String getAddress2();

    String getCountyOrCity();

    String getPostCode();

    String getEnquiryType();

    String getStage();

    String getOwner();

    String getSource();

    String getMedium();

    Date getFirstContact();

    Date getLastUpdated();

    String getTenantId();

    Account getAccount();

    List<CustomContactField> getCustomFields();

    List<Note> getNotes();

    List<Document> getDocuments();
}
