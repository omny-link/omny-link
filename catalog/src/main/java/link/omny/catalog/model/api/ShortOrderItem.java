package link.omny.catalog.model.api;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import link.omny.catalog.model.CustomOrderItemField;

public interface ShortOrderItem {
//     Long getOrderItemId();
//     void setOrderItemId(Long orderItemId);
//     Long getOrderId();
//     void setOrderId(Long orderId);
     String getSelfRef();
     void setSelfRef(String selfRef);
     String getName();
     void setName(String name);
     String getDescription();
     void setDescription(String description);
     String getStatus();
     void setStatus(String status);
     BigDecimal getPrice();
     void setPrice(BigDecimal price);
     Long getStockItemId();
     void setStockItemId(Long stockItemId);
     String getStockItemName();
     void setStockItemName(String stockItemName);
     List<CustomOrderItemField> getCustomFields();
     void setCustomFields(List<CustomOrderItemField> customFields);
     Date getCreated();
     void setCreated(Date created);
     Date getLastUpdated();
     void setLastUpdated(Date lastUpdated);
     String getTenantId();
     void setTenantId(String tenantId);
}
