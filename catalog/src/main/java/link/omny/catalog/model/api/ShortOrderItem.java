/*******************************************************************************
 *Copyright 2018 Tim Stephenson and contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
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
