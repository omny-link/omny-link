package link.omny.catalog.model.api;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import link.omny.catalog.model.CustomOrderField;

public interface ShortOrder extends Serializable {

    String getName();

    void setName(String name);

    String getDescription();

    void setDescription(String description);

    Date getDate();

    void setDate(Date date);

    String getDueDate();

    void setDueDate(String dueDate);

    String getStage();

    void setStage(String stage);

    BigDecimal getPrice();

    void setPrice(BigDecimal price);

    String getInvoiceRef();

    void setInvoiceRef(String invoiceRef);

    Long getContactId();

    void setContactId(Long contactId);

    Date getCreated();

    void setCreated(Date created);

    Date getLastUpdated();

    void setLastUpdated(Date lastUpdated);

    String getTenantId();

    void setTenantId(String tenantId);

    List<CustomOrderField> getCustomFields();

    void setCustomFields(List<CustomOrderField> fields);

    void addCustomField(CustomOrderField customField);

}
