/*******************************************************************************
 * Copyright 2015-2022 Tim Stephenson and contributors
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

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Set;

import link.omny.catalog.model.CustomOrderField;
import link.omny.catalog.model.Feedback;
import link.omny.catalog.model.OrderItem;

public interface OrderWithSubEntities extends ShortOrder, Serializable {

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
//
//    Date getCreated();
//
//    void setCreated(Date created);
//
//    Date getLastUpdated();
//
//    void setLastUpdated(Date lastUpdated);

    String getTenantId();

    void setTenantId(String tenantId);

    Feedback getFeedback();

    void setFeedback(Feedback feedback);

    Set<CustomOrderField> getCustomFields();

    void setCustomFields(Set<CustomOrderField> fields);

    void addCustomField(CustomOrderField customField);

    void setOrderItems(List<OrderItem> newItems);

    List<OrderItem> getOrderItems();

    // Order addOrderItem(OrderItem item);

}
