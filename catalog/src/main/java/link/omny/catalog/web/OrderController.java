/*******************************************************************************
 *Copyright 2015-2018 Tim Stephenson and contributors
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
package link.omny.catalog.web;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import link.omny.catalog.CatalogException;
import link.omny.catalog.CatalogObjectNotFoundException;
import link.omny.catalog.json.JsonCustomFeedbackFieldDeserializer;
import link.omny.catalog.json.JsonCustomOrderFieldDeserializer;
import link.omny.catalog.json.JsonCustomOrderItemFieldDeserializer;
import link.omny.catalog.model.CustomFeedbackField;
import link.omny.catalog.model.CustomOrderField;
import link.omny.catalog.model.CustomOrderItemField;
import link.omny.catalog.model.Feedback;
import link.omny.catalog.model.Order;
import link.omny.catalog.model.OrderItem;
import link.omny.catalog.model.api.OrderWithSubEntities;
import link.omny.catalog.model.api.ShortOrder;
import link.omny.catalog.model.api.ShortStockItem;
import link.omny.catalog.repositories.FeedbackRepository;
import link.omny.catalog.repositories.OrderItemRepository;
import link.omny.catalog.repositories.OrderRepository;
import link.omny.catalog.repositories.StockItemRepository;
import link.omny.catalog.views.OrderViews;
import link.omny.custmgmt.internal.NullAwareBeanUtils;
import link.omny.custmgmt.json.JsonCustomFieldSerializer;
import link.omny.custmgmt.model.Document;
import link.omny.custmgmt.model.Note;
import link.omny.supportservices.exceptions.BusinessEntityNotFoundException;
import link.omny.supportservices.web.NumberSequenceController;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * REST web service for accessing stock items.
 *
 * @author Tim Stephenson
 */
@Controller
@RequestMapping(value = "/{tenantId}/orders")
public class OrderController {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(OrderController.class);

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    private FeedbackRepository feedbackRepo;

    @Autowired
    private OrderRepository orderRepo;

    @Autowired
    private OrderItemRepository orderItemRepo;

    @Autowired
    private NumberSequenceController seqSvc;

    @Autowired
    private StockItemRepository stockItemRepo;
    /**
     * @return a complete order including its items and feedback.
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    @JsonView(OrderViews.Detailed.class)
    @Transactional
    public @ResponseBody Order readOrder(
            @PathVariable("tenantId") String tenantId,
            @PathVariable("id") Long orderId) {
        LOGGER.info(String.format("Read order %1$s for tenant %2$s", orderId,
                tenantId));

        Order order = orderRepo.findOne(orderId);
        LOGGER.info(String.format(
                "Found order with %1$d order items and %2$s inc. feedback",
                order.getOrderItems().size(),
                order.getFeedback() == null ? "DOES NOT" : "DOES"));

        order.setChildOrders(orderRepo.findByParentOrderForTenant(tenantId, order.getId()));
        order.setFeedback(feedbackRepo.findByOrder(tenantId, orderId));
        addLinks(tenantId, order);
        return order;
    }

    /**
     * @return the specified orders including all items and feedback.
     */
    @RequestMapping(value = "/findByIdArray/{ids}", method = RequestMethod.GET)
    @JsonView(OrderViews.Detailed.class)
    @Transactional
    public @ResponseBody List<Order> readOrders(
            @PathVariable("tenantId") String tenantId,
            @PathVariable("ids") String orderIds) {
        LOGGER.info(String.format("Read orders %1$s for tenant %2$s", orderIds,
                tenantId));
        List<Order> orders = orderRepo.findByIds(tenantId, parseOrderIds(orderIds));
        for (Order order : orders) {
            LOGGER.info(String.format(
                    "Found order with %1$d order items and %2$s inc. feedback",
                    order.getOrderItems().size(),
                    order.getFeedback() == null ? "DOES NOT" : "DOES"));

            order.setChildOrders(orderRepo.findByParentOrderForTenant(tenantId, order.getId()));
            order.setFeedback(feedbackRepo.findByOrder(tenantId, order.getId()));
            addLinks(tenantId, order);
        }
        return orders;
    }

    protected Long[] parseOrderIds(String orderIds) {
        Long[] ids;
        try {
            JsonNode tree = objectMapper.readTree(orderIds);
            if (tree.isArray()) {
                ids = new Long[tree.size()];
                int idx = 0;
                for (JsonNode node : tree) {
                    ids[idx++] = node.asLong();
                }
            } else {
                ids = new Long[1];
                ids[0] = tree.asLong();
            }
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
            throw new CatalogException(String.format("Unable to parse order ids from %1$s", orderIds));
        }
        return ids;
    }

    /**
     * @return feedback for the specified order.
     */
    @RequestMapping(value = "/{id}/feedback", method = RequestMethod.GET)
    public @ResponseBody FeedbackResource readFeedback(
            @PathVariable("tenantId") String tenantId,
            @PathVariable("id") Long orderId) {
        LOGGER.info(String.format(
                "Read feedback for order %1$s of tenant %2$s", orderId,
                tenantId));

        Feedback feedback = feedbackRepo.findByOrder(tenantId, orderId);

        if (feedback == null) {
            LOGGER.info(String.format("No feedback for order %1$d", orderId));
            throw new CatalogObjectNotFoundException(String.format(
                    "Unable to find object of type %1$s for order %2$s",
                    Feedback.class, orderId), Feedback.class, orderId);
        } else {
            LOGGER.info(String.format("Found feedback for order %1$d: %2$s",
                    orderId, feedback));
            return wrap(feedback);
        }
    }

    /**
     * @return orders for the specified tenant.
     */
    @RequestMapping(value = "/", method = RequestMethod.GET)
    @JsonView(OrderViews.Summary.class)
    public @ResponseBody List<Order> listForTenant(
            @PathVariable("tenantId") String tenantId,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "limit", required = false) Integer limit) {
        LOGGER.info(String.format("List orders for tenant %1$s",
                tenantId));

        List<Order> list;
        if (limit == null) {
            list = orderRepo.findAllForTenant(tenantId);
        } else {
            Pageable pageable = new PageRequest(page == null ? 0 : page, limit);
            list = orderRepo.findPageForTenant(tenantId, pageable);
        }
        LOGGER.info(String.format("Found %1$s orders", list.size()));

        return list;
    }

    /**
     * @return orders for that tenant.
     */
    @RequestMapping(value = "/", method = RequestMethod.GET, produces = "text/csv")
    public @ResponseBody ResponseEntity<String> listForTenantAsCsv(
            @PathVariable("tenantId") String tenantId,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "limit", required = false) Integer limit) {
        StringBuilder sb = new StringBuilder().append(
                "id,name,ref,type,description,date,dueDate,stage,price,"
                + "tax,invoiceRef,owner,contactId,stockItemId,tenantId,"
                + "created,lastUpdated,notes,documents,");
        List<String> customFieldNames = orderRepo.findCustomFieldNames(tenantId);
        LOGGER.info("Found {} custom field names while exporting orders for {}: {}",
                customFieldNames.size(), tenantId, customFieldNames);
        for (String fieldName : customFieldNames) {
            sb.append(fieldName).append(",");
        }
        sb.append("\r\n");

        for (Order order : listForTenant(tenantId, page, limit)) {
            order.setCustomHeadings(customFieldNames);
            sb.append(order.toCsv()).append("\r\n");
        }
        LOGGER.info("Exporting CSV orders for {} generated {} bytes",
                tenantId, sb.length());

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentLength(sb.length());
        return new ResponseEntity<String>(
                sb.toString(), httpHeaders, HttpStatus.OK);
    }

    /**
     * @return orders owned by the specified contact.
     */
    @RequestMapping(value = "/findByContact/{contactId}", method = RequestMethod.GET)
    @JsonView(OrderViews.Detailed.class)
    public @ResponseBody List<Order> listForContact(
            @PathVariable("tenantId") String tenantId,
            @PathVariable("contactId") Long contactId,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "limit", required = false) Integer limit) {
        return listForContacts(tenantId, new Long[] { contactId }, page, limit);
    }

    /**
     * @return orders owned by the specified contact(s).
     */
    @RequestMapping(value = "/findByContacts/{contactIds}", method = RequestMethod.GET)
    @JsonView(OrderViews.Detailed.class)
    public @ResponseBody List<Order> listForContacts(
            @PathVariable("tenantId") String tenantId,
            @PathVariable("contactIds") Long[] contactIds,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "limit", required = false) Integer limit) {
        LOGGER.info(String.format(
                "List orders for contacts %1$s & tenant %2$s",
                Arrays.asList(contactIds), tenantId));

        List<Order> list;
        if (limit == null) {
            list = orderRepo.findAllForContacts(tenantId, contactIds);
        } else {
            Pageable pageable = new PageRequest(page == null ? 0 : page, limit);
            list = orderRepo
                    .findPageForContacts(tenantId, contactIds, pageable);
        }
        LOGGER.info(String.format("Found %1$s orders", list.size()));

        return list;
    }

    /**
     * @return orders of the specified type.
     */
    @RequestMapping(value = "/findByType/{type}", method = RequestMethod.GET)
    @JsonView(OrderViews.Detailed.class)
    public @ResponseBody List<Order> findByType(
            @PathVariable("tenantId") String tenantId,
            @PathVariable("type") String type,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "limit", required = false) Integer limit) {
        List<Order> list;
        if (limit == null) {
            list = orderRepo.findByTypeForTenant(tenantId, type);
        } else {
            Pageable pageable = new PageRequest(page == null ? 0 : page, limit);
            list = orderRepo
                    .findPageByTypeForTenant(tenantId, type, pageable);
        }
        LOGGER.info(String.format("Found %1$s orders", list.size()));

        return list;
    }

    /**
     * @return Funnel report based on stage for the specified tenant.
     */
    @RequestMapping(value = "/funnel", method = RequestMethod.GET)
    public @ResponseBody FunnelReport reportTenantByAccount(
            @PathVariable("tenantId") String tenantId) {
        LOGGER.info(String.format("List account funnel for tenant %1$s", tenantId));

        FunnelReport rpt = new FunnelReport();
        List<Object[]> list = orderRepo
                .findAllForTenantGroupByStage(tenantId);
        LOGGER.debug(String.format("Found %1$s stages", list.size()));

        for (Object[] objects : list) {
            rpt.addStage((String) objects[0], (Number) objects[1]);
        }

        return rpt;
    }

    /**
     * @return the created order.
     */
    @ResponseStatus(value = HttpStatus.CREATED)
    @RequestMapping(value = "/", method = RequestMethod.POST)
    public @ResponseBody ResponseEntity<?> create(
            @PathVariable("tenantId") String tenantId,
            @RequestBody Order order) {
        order.setTenantId(tenantId);

        fixUpOrderItems(tenantId, order);
        for (CustomOrderField field : order.getCustomFields()) {
            field.setOrder(order);
        }
        if (order.getStockItem() != null && order.getStockItem().getId() != null) {
            LOGGER.debug("Looking up order's stock item for {}...",
                    order.getStockItem().getId());
            order.setStockItem(
                    stockItemRepo.findOne(order.getStockItem().getId()));
            LOGGER.debug("... found {} ({})", order.getStockItem().getName(),
                    order.getStockItem().getId());
        }
        if (order.getOrderItems() != null && order.getOrderItems().size() > 0) {
            for (OrderItem item : order.getOrderItems()) {
                if (item.getStockItem() != null && item.getStockItem().getId() != null) {
                    item.setStockItem(
                            stockItemRepo.findOne(item.getStockItem().getId()));
                }
            }
        }
        if (order.getParent() != null && order.getParent().getId() != null) {
            order.setParent(
                    orderRepo.findOne(order.getParent().getId()));
        }
        if ("-1".equals(String.valueOf(order.getRef()))) {
            Long ref;
            switch (order.getType()) {
            case "po":
                ref = seqSvc.getNext("Purchase Order", tenantId).getLastUsed();
                break;
            default:
                ref = seqSvc.getNext("Order", tenantId).getLastUsed();
            }
            order.setRef(ref);
        }
        orderRepo.save(order);

        HashMap<String, String> vars = new HashMap<String, String>();
        vars.put("tenantId", tenantId);
        vars.put("id", order.getId().toString());

        return getCreatedResponseEntity("/{id}", vars);
    }

    protected ResponseEntity<Object> getCreatedResponseEntity(String path, Map<String, String> vars) {
        URI location = MvcUriComponentsBuilder.fromController(getClass()).path(path).buildAndExpand(vars).toUri();
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(location);
        return new ResponseEntity<Object>(headers, HttpStatus.CREATED);
    }

    /**
     * Update an existing order.
     */
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    @RequestMapping(value = "/{id}", method = RequestMethod.PUT, consumes = { "application/json" })
    @Transactional
    public @ResponseBody void update(@PathVariable("tenantId") String tenantId,
            @PathVariable("id") Long orderId,
            @RequestBody Order updatedOrder) {
        Order order = orderRepo.findOne(orderId);

        NullAwareBeanUtils.copyNonNullProperties(updatedOrder, order, "id", "stockItem");
        mergeStockItem(updatedOrder, order);

        fixUpOrderItems(tenantId, order);
        orderRepo.save(order);
    }

    private void mergeStockItem(Order updatedOrder, Order order) {
        if (updatedOrder.getStockItem() == null) {
            order.setStockItem(null);
        } else if (!updatedOrder.getStockItem().equals(order.getStockItem())) {
            order.setStockItem(
                    stockItemRepo.findOne(updatedOrder.getStockItem().getId()));
        }
    }

    private void fixUpOrderItems(String tenantId, Order order) {
        LOGGER.debug("fixUpOrderItems for order {} ({}), {} items found",
                order.getName(), order.getId(),
                (order.getOrderItems() == null ? 0 : order.getOrderItems().size()));
        for (OrderItem item : order.getOrderItems()) {
            item.setTenantId(tenantId);
            item.setOrder(order);
            if (item.getStockItem() != null && item.getStockItem().getId() != null) {
                LOGGER.debug("Looking up stock item for {}...",
                        item.getStockItem().getId());
                item.setStockItem(
                        stockItemRepo.findOne(item.getStockItem().getId()));
                LOGGER.debug("... found {} ({})", item.getStockItem().getName(),
                        item.getStockItem().getId());
            } else if (item.getCustomFieldValue("stockItemId") != null) { // HACK!
                LOGGER.debug("Looking up stock item for custom field {}...",
                        item.getCustomFieldValue("stockItemId"));
                item.setStockItem(stockItemRepo.findOne(
                        Long.parseLong((String) item.getCustomFieldValue("stockItemId"))));
                LOGGER.debug("... found {} ({})", item.getStockItem().getName(),
                        item.getStockItem().getId());
            }
            for (CustomOrderItemField field : item.getCustomFields()) {
                field.setOrderItem(item);
            }
        }
    }

    /**
     * Update an existing order with a new order item.
     * @return
     */
    @ResponseStatus(value = HttpStatus.CREATED)
    @RequestMapping(value = "/{id}/order-items", method = RequestMethod.POST, consumes = { "application/json" })
    @Transactional
    public @ResponseBody ResponseEntity<Object> addOrderItem(
            @PathVariable("tenantId") String tenantId,
            @PathVariable("id") Long orderId, @RequestBody OrderItem newItem) {
        Order order = orderRepo.findOne(orderId);

        for (CustomOrderItemField coif : newItem.getCustomFields()) {
            coif.setOrderItem(newItem);
        }
        if (newItem.getStockItem() != null && newItem.getStockItem().getId() != null) {
            newItem.setStockItem(
                    stockItemRepo.findOne(newItem.getStockItem().getId()));
        }

        order.addOrderItem(newItem);
        order.setLastUpdated(new Date());
        order = orderRepo.save(order);
        HashMap<String, String> vars = new HashMap<String, String>();
        vars.put("tenantId", tenantId);
        vars.put("id", order.getId().toString());
        List<OrderItem> items = order.getOrderItems();
        items.sort((o1,o2) -> o1.getId().compareTo(o2.getId()));
        vars.put("itemId", items.get(0).getId().toString());

        return getCreatedResponseEntity("/{id}/order-items/{itemId}", vars);
    }

    /**
     * Add a document to the specified order.
     */
    @RequestMapping(value = "/{orderId}/documents", method = RequestMethod.POST)
    @Transactional
    public @ResponseBody ResponseEntity<Document> addDocument(
            @PathVariable("tenantId") String tenantId,
            @PathVariable("orderId") Long orderId, @RequestBody Document doc) {
         Order order = orderRepo.findOne(orderId);
         order.getDocuments().add(doc);
         order.setLastUpdated(new Date());
         orderRepo.save(order);
         doc = order.getDocuments().get(order.getDocuments().size()-1);

         HttpHeaders headers = new HttpHeaders();
         URI uri = MvcUriComponentsBuilder.fromController(getClass())
                 .path("/{id}/documents/{docId}")
                 .buildAndExpand(tenantId, order.getId(), doc.getId())
                 .toUri();
         headers.setLocation(uri);

         return new ResponseEntity<Document>(doc, headers, HttpStatus.CREATED);
    }

    /**
     * Add a document to the specified order.
     *
     * <p>This is just a convenience method, see {@link #addDocument(String, Long, Document)}
     * @return The document created.
     */
    @RequestMapping(value = "/{orderId}/documents", method = RequestMethod.POST, consumes = "application/x-www-form-urlencoded")
    public @ResponseBody Document addDocument(
            @PathVariable("tenantId") String tenantId,
            @PathVariable("orderId") Long orderId,
            @RequestParam("author") String author,
            @RequestParam("name") String name,
            @RequestParam("url") String url) {

        return addDocument(tenantId, orderId, new Document(author, name, url)).getBody();
    }

    /**
     * Add a note to the specified order.
     * @return the created note.
     */
    @RequestMapping(value = "/{orderId}/notes", method = RequestMethod.POST)
    @Transactional
    public @ResponseBody ResponseEntity<Note> addNote(
            @PathVariable("tenantId") String tenantId,
            @PathVariable("orderId") Long orderId, @RequestBody Note note) {
        Order order = orderRepo.findOne(orderId);
        order.getNotes().add(note);
        order.setLastUpdated(new Date());
        orderRepo.save(order);
        note = order.getNotes().get(order.getNotes().size()-1);

        HttpHeaders headers = new HttpHeaders();
        URI uri = MvcUriComponentsBuilder.fromController(getClass())
                .path("/{id}/notes/{noteId}")
                .buildAndExpand(tenantId, order.getId(), note.getId())
                .toUri();
        headers.setLocation(uri);

        return new ResponseEntity<Note>(note, headers, HttpStatus.CREATED);
    }

    /**
     * Add a note to the specified order from its parts.
     *
     * <p>This is just a convenience method, see {@link #addNote(String, Long, Note)}
     *
     * @return The note created.
     */
    @RequestMapping(value = "/{orderId}/notes", method = RequestMethod.POST, consumes = "application/x-www-form-urlencoded")
    public @ResponseBody Note addNote(
            @PathVariable("tenantId") String tenantId,
            @PathVariable("orderId") Long orderId,
            @RequestParam("author") String author,
            @RequestParam("favorite") boolean favorite,
            @RequestParam("content") String content) {
        return addNote(tenantId, orderId, new Note(author, content, favorite)).getBody();
    }

    /**
     * Update an existing order with new feedback.
     * @return
     */
    @ResponseStatus(value = HttpStatus.CREATED)
    @RequestMapping(value = "/{id}/feedback", method = RequestMethod.POST, consumes = { "application/json" })
    @Transactional
    public @ResponseBody ResponseEntity<Object> addFeedback(
            @PathVariable("tenantId") String tenantId,
            @PathVariable("id") Long orderId, @RequestBody Feedback feedback) {
        LOGGER.debug("Creating feedback on order {} for {}",
                orderId, tenantId);
        feedback.setTenantId(tenantId);
        for (CustomFeedbackField cf : feedback.getCustomFields()) {
            cf.setFeedback(feedback);
        }
        feedback = feedbackRepo.save(feedback);

        Order order = orderRepo.findOne(orderId);
        feedback.setOrder(order);
        order.setFeedback(feedback);
        order.setLastUpdated(new Date());
        orderRepo.save(order);

        HashMap<String, String> vars = new HashMap<String, String>();
        vars.put("tenantId", tenantId);
        vars.put("id", orderId.toString());
        vars.put("feedbackId", feedback.getId().toString());

        return getCreatedResponseEntity("/{id}/feedback/{feedbackId}",vars);
    }

    /**
     * Update existing order feedback.
     * @return
     */
    @RequestMapping(value = "/{id}/feedback/{feedbackId}", method = RequestMethod.PUT, consumes = { "application/json" })
    public @ResponseBody ResponseEntity<Object> updateFeedback(
            @PathVariable("tenantId") String tenantId,
            @PathVariable("id") Long orderId,
            @PathVariable("feedbackId") Long feedbackId,
            @RequestBody Feedback feedback) {
        Feedback existingFeedback = feedbackRepo.findByOrder(tenantId, orderId);
        if (existingFeedback == null) {
            throw new BusinessEntityNotFoundException("feedback", String.valueOf(feedbackId));
        } else {
            LOGGER.debug("Updating feedback {} of order {} for {}",
                    existingFeedback.getId(), orderId, tenantId);
            NullAwareBeanUtils
                    .copyNonNullProperties(feedback, existingFeedback, "order");
            feedback = feedbackRepo.save(existingFeedback);
        }

        HashMap<String, String> vars = new HashMap<String, String>();
        vars.put("tenantId", tenantId);
        vars.put("id", orderId.toString());
        vars.put("feedbackId", feedback.getId().toString());

        return getCreatedResponseEntity("/{id}/feedback/{feedbackId}",vars);
    }

    /**
     * Update an existing order.
     */
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    @RequestMapping(value = "/{id}/order-items/{itemId}", method = RequestMethod.PUT, consumes = { "application/json" })
    public @ResponseBody void updateOrderItem(
            @PathVariable("tenantId") String tenantId,
            @PathVariable("id") Long orderId,
            @PathVariable("itemId") Long orderItemId,
            @RequestBody OrderItem updatedOrderItem) {
        OrderItem item = orderItemRepo.findOne(orderItemId);

        NullAwareBeanUtils.copyNonNullProperties(updatedOrderItem, item, "id", "stockItem");
        item.setTenantId(tenantId);
        mergeStockItem(updatedOrderItem, item);

        orderItemRepo.save(item);
    }

    private void mergeStockItem(OrderItem updatedOrderItem, OrderItem item) {
        if (updatedOrderItem.getStockItem() == null) {
            item.setStockItem(null);
        } else if (!updatedOrderItem.getStockItem().equals(item.getStockItem())) {
            item.setStockItem(
                    stockItemRepo.findByName(updatedOrderItem.getStockItem().getName()));
        }
    }

    /**
     * Change the stage the order has reached.
     *
     * @param tenantId
     * @param orderId
     * @param stage
     */
    @RequestMapping(value = "/{orderId}/stage/{stage}", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public @ResponseBody void setStage(
            @PathVariable("tenantId") String tenantId,
            @PathVariable("orderId") Long orderId,
            @PathVariable("stage") String stage,
            @RequestParam(required = false, defaultValue = "false") boolean orderPlaced) {
        LOGGER.info(String.format("Setting order %1$s to stage %2$s",
                orderId, stage));
        Order order = orderRepo.findOne(orderId);

        String oldStage = order.getStage();
        if (!oldStage.equals(stage) || orderPlaced) {
            order.setStage(stage);
            order.setDate(new Date());
            orderRepo.save(order);
        } else {
            LOGGER.warn("Skipping update of order {} from stage {} to {}",
                    orderId, oldStage, stage);
        }
    }

    /**
     * Delete an existing order.
     */
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    @RequestMapping(value = "/{orderId}", method = RequestMethod.DELETE)
    @Transactional
    public @ResponseBody void delete(@PathVariable("tenantId") String tenantId,
            @PathVariable("orderId") Long orderId) {
        orderRepo.delete(orderId);
    }

    /**
     * Delete the specified order item.
     */
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    @RequestMapping(value = "/{id}/order-items/{itemId}", method = RequestMethod.DELETE)
    @Transactional
    public @ResponseBody void deleteItem(
            @PathVariable("tenantId") String tenantId,
            @PathVariable("id") Long orderId,
            @PathVariable("itemId") Long orderItemId) {

        // cascade does not appear to work on delete
        orderRepo.deleteItemCustomField(orderItemId);

        orderRepo.deleteItem(orderId, orderItemId);
    }

    private FeedbackResource wrap(Feedback feedback) {
        FeedbackResource resource = new FeedbackResource();

        NullAwareBeanUtils
                .copyNonNullProperties(feedback, resource, "customFields");
        resource.setFeedbackId(feedback.getId());
        resource.setOrderId(feedback.getOrder().getId());

        ArrayList<CustomFeedbackField> fields = new ArrayList<CustomFeedbackField>();
        List<Long> fieldsSeen = new ArrayList<Long>();
        for (CustomFeedbackField field : feedback.getCustomFields()) {
            if (field.getFeedback().getId().equals(feedback.getId())
                    && !fieldsSeen.contains(field.getId())) {
                fields.add(field);
                fieldsSeen.add(field.getId());
            } else {
                LOGGER.debug(String.format(
                        "Removing duplicate field due to inner join: %1$s",
                        feedback));
            }
        }
        resource.setCustomFields(fields);

        return resource;
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class OrderResource extends ShortOrderResource implements
            OrderWithSubEntities {
        private static final long serialVersionUID = -5578143108772043622L;
        private List<OrderItem> orderItems;
        private Feedback feedback;
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class ShortOrderResource extends ResourceSupport implements
            ShortOrder {
        private static final long serialVersionUID = 385801786736131068L;
        private String selfRef;
        private Long localId;
        private String name;
        private String description;
        private Date date;
        private String dueDate;
        @JsonDeserialize(using = JsonCustomOrderFieldDeserializer.class)
        @JsonSerialize(using = JsonCustomFieldSerializer.class)
        private List<CustomOrderField> customFields;
        private Long contactId;
        private String stage;
        private BigDecimal price;
        private String invoiceRef;
        private ShortStockItem stockItem;
        private Date created;
        private Date lastUpdated;
        private String tenantId;

        @Override
        public void addCustomField(CustomOrderField customField) {
            if (customFields == null) {
                customFields = new ArrayList<CustomOrderField>();
            }
            customFields.add(customField);
        }
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class FeedbackResource extends ResourceSupport {
        private Long feedbackId;
        private Long orderId;
        private String selfRef;
        private String description;
        private BigDecimal price;
        @JsonDeserialize(using = JsonCustomFeedbackFieldDeserializer.class)
        @JsonSerialize(using = JsonCustomFieldSerializer.class)
        private List<CustomFeedbackField> customFields;
        private Date created;
        private Date lastUpdated;
        private String tenantId;
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class ShortOrderItemResource extends ResourceSupport {
        private Long orderItemId;
        private Long orderId;
        private String selfRef;
        private String name;
        private String description;
        private String status;
        private BigDecimal price;
        private Long stockItemId;
        private String stockItemName;
        @JsonDeserialize(using = JsonCustomOrderItemFieldDeserializer.class)
        @JsonSerialize(using = JsonCustomFieldSerializer.class)
        private List<CustomOrderItemField> customFields;
        private Date created;
        private Date lastUpdated;
        private String tenantId;
    }

    private Order addLinks(String tenantId, Order order) {
        List<Link> links = new ArrayList<Link>();
        links.add(new Link(String.format("/%1$s/orders/%2$s",
                tenantId, order.getId())));
        order.setLinks(links);
        return order;
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class FunnelReport extends ResourceSupport {
        @JsonProperty
        private Map<String, Number> stages;

        public Map<String, Number> getStages() {
            if (stages == null) {
                stages = new HashMap<String, Number>();
            }
            return stages;
        }

        public void addStage(String stage, Number count) {
            getStages().put(
                    (stage == null || stage.length() == 0) ? "N/A" : stage,
                    count == null ? 0 : count);
        }
    }
}
