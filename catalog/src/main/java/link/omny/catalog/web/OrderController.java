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

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.io.IOException;
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
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import link.omny.catalog.CatalogException;
import link.omny.catalog.CatalogObjectNotFoundException;
import link.omny.catalog.model.CustomFeedbackField;
import link.omny.catalog.model.CustomOrderField;
import link.omny.catalog.model.CustomOrderItemField;
import link.omny.catalog.model.Feedback;
import link.omny.catalog.model.Order;
import link.omny.catalog.model.OrderItem;
import link.omny.catalog.repositories.FeedbackRepository;
import link.omny.catalog.repositories.OrderItemRepository;
import link.omny.catalog.repositories.OrderRepository;
import link.omny.catalog.repositories.StockItemRepository;
import link.omny.catalog.views.OrderViews;
import link.omny.supportservices.exceptions.BusinessEntityNotFoundException;
import link.omny.supportservices.internal.NullAwareBeanUtils;
import link.omny.supportservices.model.Document;
import link.omny.supportservices.model.Note;
import link.omny.supportservices.web.NumberSequenceController;
import lombok.Data;

/**
 * REST web service for accessing stock items.
 *
 * @author Tim Stephenson
 */
@RestController
@RequestMapping(value = "/{tenantId}/orders")
@Api(tags = "Order API")
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

    @Autowired
    private StockItemController stockItemSvc;

    /**
     * @return a complete order including its items and feedback.
     */
    @GetMapping(value = "/{id}")
    @JsonView(OrderViews.Detailed.class)
    @Transactional
    @ApiOperation(value = "Return the specified order.")
    public @ResponseBody EntityModel<Order> findEntityById(
            @PathVariable("tenantId") String tenantId,
            @PathVariable("id") Long orderId) {
        LOGGER.info("Read order {} for tenant {}", orderId, tenantId);

        Order order = findById(tenantId, orderId);
        LOGGER.info(
                "Found order with {} order items, {} notes, {} docs and {} feedback",
                order.getOrderItems().size(),
                order.getNotes().size(),
                order.getDocuments().size(),
                order.getFeedback() == null ? "WITHOUT" : "WITH");

        order.setChildOrders(orderRepo.findByParentOrderForTenant(tenantId, order.getId()));
        return addLinks(tenantId, order);
    }

    /**
     * @return the specified orders including all items and feedback.
     */
    @GetMapping(value = "/findByIdArray/{ids}")
    @Transactional
    @ApiOperation("Retrieve the specified orders.")
    public @ResponseBody List<EntityModel<Order>> readOrders(
            @PathVariable("tenantId") String tenantId,
            @PathVariable("ids") String orderIds) {
        LOGGER.info("Read orders {} for tenant {}", orderIds, tenantId);
        List<Order> orders = orderRepo.findByIds(tenantId, parseOrderIds(orderIds));
        for (Order order : orders) {
            LOGGER.info("Found order with {} order items",
                    order.getOrderItems().size());

            order.setChildOrders(orderRepo.findByParentOrderForTenant(tenantId, order.getId()));
        }
        return addLinks(tenantId, orders);
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
    @GetMapping(value = "/{id}/feedback")
    @ApiOperation("Retrieve feedback for the specified order.")
    public @ResponseBody Feedback readFeedback(
            @PathVariable("tenantId") String tenantId,
            @PathVariable("id") Long orderId) {
        LOGGER.info(
                "Read feedback for order {} of tenant {}", orderId, tenantId);

        Feedback feedback = feedbackRepo.findByOrder(tenantId, orderId);

        if (feedback == null) {
            LOGGER.info("No feedback for order {}", orderId);
            throw new CatalogObjectNotFoundException(String.format(
                    "Unable to find object of type %1$d for order %2$s",
                    Feedback.class, orderId), Feedback.class, orderId);
        } else {
            LOGGER.info("Found feedback for order {}: {}", orderId, feedback);
            return feedback;
        }
    }

    /**
     * @return orders for the specified tenant.
     */
    @GetMapping(value = "/")
    @JsonView(value = OrderViews.Summary.class)
    @ApiOperation(value = "Retrieves the orders for a specific tenant.")
    public @ResponseBody List<EntityModel<Order>> listEntitiesForTenant(
            @PathVariable("tenantId") String tenantId,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "limit", required = false) Integer limit) {
        return addLinks(tenantId, listForTenant(tenantId, page, limit));
    }

    protected List<Order> listForTenant(String tenantId, Integer page,
            Integer limit) {
        LOGGER.info("List orders for tenant {}", tenantId);

        List<Order> list;
        if (limit == null) {
            list = orderRepo.findAllForTenant(tenantId);
        } else {
            Pageable pageable = PageRequest.of(page == null ? 0 : page, limit);
            list = orderRepo.findPageForTenant(tenantId, pageable);
        }
        LOGGER.info("Found {} orders", list.size());
        return list;
    }

    /**
     * @return orders for that tenant.
     */
    @GetMapping(value = "/", produces = "text/csv")
    @JsonView(value = OrderViews.Summary.class)
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
    @GetMapping(value = "/findByContact/{contactId}")
    @JsonView(value = OrderViews.Summary.class)
    @ApiOperation(value = "Retrieve orders for a contact")
    public @ResponseBody List<EntityModel<Order>> listForContact(
            @PathVariable("tenantId") String tenantId,
            @PathVariable("contactId") Long contactId,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "limit", required = false) Integer limit) {
        return listForContacts(tenantId, new Long[] { contactId }, page, limit);
    }

    /**
     * @return orders owned by the specified contact(s).
     */
    @GetMapping(value = "/findByContacts/{contactIds}")
    @JsonView(value = OrderViews.Summary.class)
    @ApiOperation(value = "Retrieve orders for a number of contacts")
    public @ResponseBody List<EntityModel<Order>> listForContacts(
            @PathVariable("tenantId") String tenantId,
            @PathVariable("contactIds") Long[] contactIds,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "limit", required = false) Integer limit) {
        LOGGER.info(
                "List orders for contacts {} & tenant {}",
                Arrays.asList(contactIds), tenantId);

        List<Order> list;
        if (limit == null) {
            list = orderRepo.findAllForContacts(tenantId, contactIds);
        } else {
            Pageable pageable = PageRequest.of(page == null ? 0 : page, limit);
            list = orderRepo
                    .findPageForContacts(tenantId, contactIds, pageable);
        }
        LOGGER.info("Found {} orders", list.size());
        return addLinks(tenantId, list);
    }

    protected Order findById(final String tenantId, final Long id) {
        return orderRepo.findById(id)
                .orElseThrow(() -> new BusinessEntityNotFoundException(
                        Order.class, id));
    }

    /**
     * @return orders of the specified type.
     */
    @GetMapping(value = "/findByType/{type}")
    @JsonView(value = OrderViews.Summary.class)
    @ApiOperation(value = "Find orders of the specified type.")
    public @ResponseBody List<EntityModel<Order>> findByType(
            @PathVariable("tenantId") String tenantId,
            @PathVariable("type") String type,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "limit", required = false) Integer limit) {
        List<Order> list;
        if (limit == null) {
            list = orderRepo.findByTypeForTenant(tenantId, type);
        } else {
            Pageable pageable = PageRequest.of(page == null ? 0 : page, limit);
            list = orderRepo
                    .findPageByTypeForTenant(tenantId, type, pageable);
        }
        LOGGER.info("Found {} orders", list.size());
        return addLinks(tenantId, list);
    }

    /**
     * @return Funnel report based on stage for the specified tenant.
     */
    @GetMapping(value = "/funnel")
    @ApiOperation("Retrieve orders by stage.")
    public @ResponseBody FunnelReport reportTenantByAccount(
            @PathVariable("tenantId") String tenantId) {
        LOGGER.info("List account funnel for tenant {}", tenantId);

        FunnelReport rpt = new FunnelReport();
        List<Object[]> list = orderRepo
                .findAllForTenantGroupByStage(tenantId);
        LOGGER.debug("Found {} stages", list.size());

        for (Object[] objects : list) {
            rpt.addStage((String) objects[0], (Number) objects[1]);
        }

        return rpt;
    }

    /**
     * @return the created order.
     */
    @ResponseStatus(value = HttpStatus.CREATED)
    @PostMapping(value = "/")
    @ApiOperation(value = "Create a new order.")
    public @ResponseBody ResponseEntity<EntityModel<Order>> create(
            @PathVariable("tenantId") String tenantId,
            @RequestBody Order order) {
        order.setTenantId(tenantId);

        fixUpOrderItems(tenantId, order);
        for (CustomOrderField field : order.getCustomFields()) {
            field.setOrder(order);
        }
//        if (order.getStockItemId() != null) {
//            LOGGER.debug("Looking up order's stock item for {}...",
//                    order.getStockItemId());
//            order.setStockItem(
//                    stockItemSvc.findById(tenantId, order.getStockItemId()));
//            LOGGER.debug("... found {} ({})", order.getStockItem().getName(),
//                    order.getStockItem().getId());
//        }
        if (order.getOrderItems() != null && order.getOrderItems().size() > 0) {
            for (OrderItem item : order.getOrderItems()) {
                if (item.getStockItem() != null && item.getStockItem().getId() != null) {
                    item.setStockItem(
                            stockItemSvc.findById(tenantId, item.getStockItem().getId()));
                }
            }
        }
        if (order.getParent() != null && order.getParent().getId() != null) {
            order.setParent(findById(tenantId, order.getParent().getId()));
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
        EntityModel<Order> entityModel = addLinks(tenantId, orderRepo.save(order));
        HttpHeaders headers = headersWithLocation(entityModel.getLink("self").get().toUri());
        return new ResponseEntity<EntityModel<Order>>(entityModel, headers, HttpStatus.CREATED);
    }

    protected HttpHeaders headersWithLocation(URI uri) {
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(uri);
        return headers;
    }

    protected ResponseEntity<Object> getCreatedResponseEntity(String path, Map<String, String> vars) {
        URI location = MvcUriComponentsBuilder.fromController(getClass()).path(path).buildAndExpand(vars).toUri();
        HttpHeaders headers = headersWithLocation(location);
        return new ResponseEntity<Object>(headers, HttpStatus.CREATED);
    }

    /**
     * Update an existing order.
     */
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    @PutMapping(value = "/{id}", consumes = { "application/json" })
    @ApiOperation(value = "Update an existing order")
    @Transactional
    public @ResponseBody void update(@PathVariable("tenantId") String tenantId,
            @PathVariable("id") Long orderId,
            @RequestBody Order updatedOrder) {
        Order order = findById(tenantId, orderId);

        NullAwareBeanUtils.copyNonNullProperties(updatedOrder, order,
                "id", "documents", "notes", "stockItem");
        mergeStockItem(updatedOrder, order);

        fixUpOrderItems(tenantId, order);
        orderRepo.save(order);
    }

    private void mergeStockItem(Order updatedOrder, Order order) {
//        if (updatedOrder.getStockItem() == null) {
//            order.setStockItem(null);
//        } else if (!updatedOrder.getStockItem().equals(order.getStockItem())) {
//            order.setStockItem(
//                    stockItemSvc.findById(order.getTenantId(),
//                            updatedOrder.getStockItem().getId()));
//        }
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
                        stockItemSvc.findById(tenantId, item.getStockItem().getId()));
                LOGGER.debug("... found {} ({})", item.getStockItem().getName(),
                        item.getStockItem().getId());
            } else if (item.getCustomFieldValue("stockItemId") != null) { // HACK!
                LOGGER.debug("Looking up stock item for custom field {}...",
                        item.getCustomFieldValue("stockItemId"));
                item.setStockItem(stockItemSvc.findById(
                        tenantId, Long.parseLong((String) item.getCustomFieldValue("stockItemId"))));
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
    @PostMapping(value = "/{id}/order-items", consumes = { "application/json" })
    @Transactional
    @ApiOperation(value = "Add an item to the specified order.")
    public @ResponseBody ResponseEntity<Object> addOrderItem(
            @PathVariable("tenantId") String tenantId,
            @PathVariable("id") Long orderId, @RequestBody OrderItem newItem) {
        Order order = findById(tenantId, orderId);

        for (CustomOrderItemField coif : newItem.getCustomFields()) {
            coif.setOrderItem(newItem);
        }
        if (newItem.getStockItem() != null && newItem.getStockItem().getId() != null) {
            newItem.setStockItem(
                    stockItemSvc.findById(tenantId, newItem.getStockItem().getId()));
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
    @PostMapping(value = "/{orderId}/documents")
    @Transactional
    @ApiOperation(value = "Add a document to the specified order.")
    public @ResponseBody ResponseEntity<Document> addDocument(
            @PathVariable("tenantId") String tenantId,
            @PathVariable("orderId") Long orderId, @RequestBody Document doc) {
         Order order = findById(tenantId, orderId);
         order.getDocuments().add(doc);
         order.setLastUpdated(new Date());
         order = orderRepo.save(order);
         doc = order.getDocuments().stream()
                 .reduce((first, second) -> second).orElse(null);

         HttpHeaders headers = new HttpHeaders();
         URI uri = MvcUriComponentsBuilder.fromController(getClass())
                 .path("/{id}/documents/{docId}")
                 .buildAndExpand(tenantId, order.getId(), doc.getId())
                 .toUri();
         headers.setLocation(uri);

         return new ResponseEntity<Document>(doc, headers, HttpStatus.CREATED);
    }

    /**
     * Add a note to the specified order.
     * @return the created note.
     */
    @PostMapping(value = "/{orderId}/notes")
    @Transactional
    @ApiOperation(value = "Add a note to the specified order.")
    public @ResponseBody ResponseEntity<Note> addNote(
            @PathVariable("tenantId") String tenantId,
            @PathVariable("orderId") Long orderId, @RequestBody Note note) {
        Order order = findById(tenantId, orderId);
        order.getNotes().add(note);
        order.setLastUpdated(new Date());
        order = orderRepo.save(order);
        note = order.getNotes().stream()
                .reduce((first, second) -> second).orElse(null);


        HttpHeaders headers = new HttpHeaders();
        URI uri = MvcUriComponentsBuilder.fromController(getClass())
                .path("/{id}/notes/{noteId}")
                .buildAndExpand(tenantId, order.getId(), note.getId())
                .toUri();
        headers.setLocation(uri);

        return new ResponseEntity<Note>(note, headers, HttpStatus.CREATED);
    }

    /**
     * Update an existing order with new feedback.
     * @return created feedback object.
     */
    @PostMapping(value = "/{id}/feedback", consumes = { "application/json" })
    @ResponseStatus(value = HttpStatus.CREATED)
    @Transactional
    @ApiOperation(value = "Add feedback to the specified order.")
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

        Order order = findById(tenantId, orderId);
        feedback.setOrder(order);
        order.setFeedback(feedback);
//        order.setLastUpdated(new Date());
        order = orderRepo.save(order);

        HashMap<String, String> vars = new HashMap<String, String>();
        vars.put("tenantId", tenantId);
        vars.put("id", orderId.toString());
        vars.put("feedbackId", feedback.getId().toString());

        return getCreatedResponseEntity("/{id}/feedback/{feedbackId}", vars);
    }

    /**
     * Update existing order feedback.
     * @return
     */
    @PutMapping(value = "/{id}/feedback/{feedbackId}", consumes = { "application/json" })
    @ApiOperation("Update feedback for the specified order.")
    public @ResponseBody ResponseEntity<Object> updateFeedback(
            @PathVariable("tenantId") String tenantId,
            @PathVariable("id") Long orderId,
            @PathVariable("feedbackId") Long feedbackId,
            @RequestBody Feedback feedback) {
        Feedback existingFeedback = feedbackRepo.findByOrder(tenantId, orderId);
        if (existingFeedback == null) {
            throw new BusinessEntityNotFoundException(Feedback.class, feedbackId);
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
     * Update an existing order item.
     */
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    @PutMapping(value = "/{id}/order-items/{itemId}", consumes = { "application/json" })
    @ApiOperation(value = "Update an existing order item")
    public @ResponseBody void updateOrderItem(
            @PathVariable("tenantId") String tenantId,
            @PathVariable("id") Long orderId,
            @PathVariable("itemId") Long orderItemId,
            @RequestBody OrderItem updatedOrderItem) {
        OrderItem item = orderItemRepo.findById(orderItemId)
                .orElseThrow(() -> new BusinessEntityNotFoundException(
                        OrderItem.class, orderItemId));

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
    @PostMapping(value = "/{orderId}/stage/{stage}")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    @ApiOperation("Set the order's stage.")
    public @ResponseBody void setStage(
            @PathVariable("tenantId") String tenantId,
            @PathVariable("orderId") Long orderId,
            @PathVariable("stage") String stage,
            @RequestParam(required = false, defaultValue = "false") boolean orderPlaced) {
        LOGGER.info("Setting order {} to stage {}", orderId, stage);
        Order order = findById(tenantId, orderId);

        String oldStage = order.getStage();
        if (oldStage == null || !oldStage.equals(stage) || orderPlaced) {
            order.setStage(stage);
            order.setDate(new Date());
            Order savedOrder = orderRepo.save(order);
            if (!savedOrder.getStage().equalsIgnoreCase(stage)) {
                throw new RuntimeException("Unable to save new stage");
            }
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
    @ApiOperation(value = "Deletes the specified order.")
    public @ResponseBody void delete(@PathVariable("tenantId") String tenantId,
            @PathVariable("orderId") Long orderId) {
        orderRepo.deleteById(orderId);
    }

    /**
     * Delete the specified order item.
     */
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    @RequestMapping(value = "/{id}/order-items/{itemId}", method = RequestMethod.DELETE)
    @Transactional
    @ApiOperation(value = "Deletes the specified order item.")
    public @ResponseBody void deleteItem(
            @PathVariable("tenantId") String tenantId,
            @PathVariable("id") Long orderId,
            @PathVariable("itemId") Long orderItemId) {

        // cascade does not appear to work on delete
        orderRepo.deleteItemCustomField(orderItemId);

        orderRepo.deleteItem(orderId, orderItemId);
    }

    protected List<EntityModel<Order>> addLinks(final String tenantId, final List<Order> list) {
        ArrayList<EntityModel<Order>> entities = new ArrayList<EntityModel<Order>>();
        for (Order order : list) {
            entities.add(addLinks(tenantId, order));
        }
        return entities;
    }

    protected EntityModel<Order> addLinks(final String tenantId, final Order order) {
        return EntityModel.of(order, linkTo(methodOn(OrderController.class)
                .findEntityById(tenantId, order.getId()))
                .withSelfRel());
    }

    @Data
    public static class FunnelReport {
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
