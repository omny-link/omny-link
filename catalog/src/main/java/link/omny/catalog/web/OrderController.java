package link.omny.catalog.web;

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
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
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

import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

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
    private FeedbackRepository feedbackRepo;

    @Autowired
    private OrderRepository orderRepo;

    @Autowired
    private OrderItemRepository orderItemRepo;
    
    @Autowired
    private StockItemRepository stockItemRepo;

    /**
     * Return the specified order and its items and feedback.
     * 
     * @return a complete order.
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
        
        order.setFeedback(feedbackRepo.findByOrder(tenantId, orderId));

        return order;
//        CompleteOrder orderResource = wrap(order);
//        return orderResource;
    }

    /**
     * Return feedback for the specified order.
     *
     * @return orders for that tenant.
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
     * Return just the orders for a specific tenant.
     * 
     * @return orders for that tenant.
     */
    @RequestMapping(value = "/", method = RequestMethod.GET)
//    @JsonView(OrderViews.OrderSummary.class)
    public @ResponseBody List<ShortOrder> listForTenant(
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
        
        return wrapShort(list);
//        return list;
    }

    /**
     * Return just the orders for a specific contact.
     * 
     * @return orders for that contact.
     */
    @RequestMapping(value = "/findByContact/{contactId}", method = RequestMethod.GET)
    public @ResponseBody List<OrderWithSubEntities> listForContact(
            @PathVariable("tenantId") String tenantId,
            @PathVariable("contactId") Long contactId,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "limit", required = false) Integer limit) {
        LOGGER.info(String.format("List orders for contact %1$s & tenant %2$s",
                contactId, tenantId));

        List<Order> list;
        if (limit == null) {
            list = orderRepo.findAllForContact(tenantId, contactId);
        } else {
            Pageable pageable = new PageRequest(page == null ? 0 : page, limit);
            list = orderRepo.findPageForContact(tenantId, contactId, pageable);
        }
        LOGGER.info(String.format("Found %1$s orders", list.size()));

        return wrap(list);
    }

    /**
     * Return just the orders for a specific contact or contacts.
     * 
     * @return orders.
     */
    @RequestMapping(value = "/findByContacts/{contactIds}", method = RequestMethod.GET)
//    @JsonView(OrderViews.Detailed.class)
    //[2017-02-09 17:03:08.735] boot - 22032  WARN [http-nio-8082-exec-1] --- DefaultHandlerExceptionResolver: Failed to write HTTP message: org.springframework.http.converter.HttpMessageNotWritableException: Could not write content: Can not override serializer (through reference chain: java.util.ArrayList[0]); nested exception is com.fasterxml.jackson.databind.JsonMappingException: Can not override serializer (through reference chain: java.util.ArrayList[0])
    public @ResponseBody List<OrderWithSubEntities> listForContacts(
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

        return wrap(list);
    }

    /**
     * Create a new order.
     * 
     * @return
     */
    @ResponseStatus(value = HttpStatus.CREATED)
    @RequestMapping(value = "/", method = RequestMethod.POST)
    public @ResponseBody ResponseEntity<?> create(
            @PathVariable("tenantId") String tenantId,
            @RequestBody Order order) {
        order.setTenantId(tenantId);

        fixUpOrderItems(tenantId, order);
        if (order.getOrderItems() != null && order.getOrderItems().size() > 0) {
            order.setStockItem(null);
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
        for (OrderItem item : order.getOrderItems()) {
            item.setTenantId(tenantId);
            item.setOrder(order);
            // HACK!
            if (item.getCustomFieldValue("stockItemId") != null) {
                item.setStockItem(stockItemRepo.findOne(
                        Long.parseLong((String) item.getCustomFieldValue("stockItemId"))));
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

        newItem.setTenantId(tenantId);
        newItem.setOrder(order);
        if (newItem.getStockItem() != null && newItem.getStockItem().getId() != null) {
            newItem.setStockItem(
                    stockItemRepo.findOne(newItem.getStockItem().getId()));
        }

        newItem = orderItemRepo.save(newItem);
        HashMap<String, String> vars = new HashMap<String, String>();
        vars.put("tenantId", tenantId);
        vars.put("id", order.getId().toString());
        vars.put("itemId", newItem.getId().toString());
        
        return getCreatedResponseEntity("/{id}/order-items/{itemId}", vars);
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
        Feedback existingFeedback = feedbackRepo.findByOrder(tenantId, orderId);
        if (existingFeedback == null) {
            Order order = orderRepo.findOne(orderId);
            feedback.setTenantId(tenantId);
            feedback.setOrder(order);
            feedback = feedbackRepo.save(feedback);

            order.setFeedback(feedback);
            orderRepo.save(order);
        } else {
            NullAwareBeanUtils
                    .copyNonNullProperties(feedback, existingFeedback);
            feedbackRepo.save(existingFeedback);
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
    @Transactional
    public @ResponseBody void updateOrderItem(
            @PathVariable("tenantId") String tenantId,
            @PathVariable("id") Long orderId,
            @PathVariable("itemId") Long orderItemId,
            @RequestBody OrderItem updatedOrderItem) {
        OrderItem item = orderItemRepo.findOne(orderItemId);

        NullAwareBeanUtils.copyNonNullProperties(updatedOrderItem, item, "id");
        item.setTenantId(tenantId);
        mergeStockItem(updatedOrderItem, item);

        orderItemRepo.save(item);
    }

    private void mergeStockItem(OrderItem updatedOrderItem, OrderItem item) {
        if (updatedOrderItem.getStockItem() == null) {
            item.setStockItem(null);
        } else if (!updatedOrderItem.getStockItem().equals(item.getStockItem())) {
            item.setStockItem(
                    stockItemRepo.findOne(updatedOrderItem.getStockItem().getId()));
        }
    }

    /**
     * Change the stage the order has reached.
     */
    @RequestMapping(value = "/{orderId}/stage", method = RequestMethod.POST, consumes = "application/json")
    public @ResponseBody void setStage(
            @PathVariable("tenantId") String tenantId,
            @PathVariable("orderId") Long orderId,
            @RequestBody String stage) {
        LOGGER.info(String.format("Setting order %1$s to stage %2$s",
                orderId, stage));

        Order order = orderRepo.findOne(orderId);
        order.setStage(stage);
        order.setDate(new Date());
        orderRepo.save(order);
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

    private List<ShortOrder> wrapShort(List<Order> list) {
        List<ShortOrder> resources = new ArrayList<ShortOrder>(
                list.size());
        for (Order order : list) {
            resources.add(wrapShort(order));
        }
        return resources;
    }

    private ShortOrder wrapShort(Order order) {
        ShortOrderResource resource = new ShortOrderResource();

        BeanUtils.copyProperties(order, resource, "feedback", "orderItems", "stockItem");
        resource.setContactId(order.getContactId());
        resource.setOrderId(order.getId());
        if (order.getStockItem()!=null) {
            StockCategoryController.ShortStockItemResource stockItem
                    = new StockCategoryController.ShortStockItemResource();
            BeanUtils.copyProperties(order.getStockItem(), stockItem);
            stockItem.setPrice(order.getStockItem().getPrice() == null
                    ? null : order.getStockItem().getPrice().toPlainString());
            resource.setStockItem(stockItem);
        }

        Link detail = linkTo(OrderRepository.class,
                order.getId()).withSelfRel();
        resource.add(detail);
        resource.setSelfRef(detail.getHref());
        return resource;
    }

    private List<OrderWithSubEntities> wrap(List<Order> list) {
        List<OrderWithSubEntities> resources = new ArrayList<OrderWithSubEntities>(
                list.size());
        for (Order order : list) {
            resources.add(wrap(order));
        }
        return resources;
    }
    
    private OrderResource wrap(Order order) {
        OrderResource orderResource = new OrderResource();

        // Have to handle sub-entities specially due to serialisation problems
        BeanUtils.copyProperties(order, orderResource, "stockItem");
        orderResource.setContactId(order.getContactId());
        orderResource.setOrderId(order.getId());

        if (order.getStockItem()!=null) {
            StockCategoryController.ShortStockItemResource stockItem
                    = new StockCategoryController.ShortStockItemResource();
            BeanUtils.copyProperties(order.getStockItem(), stockItem);
            stockItem.setPrice(order.getStockItem().getPrice() == null
                    ? null : order.getStockItem().getPrice().toPlainString());
            orderResource.setStockItem(stockItem);
        }
        ArrayList<OrderItem> orderItems = new ArrayList<OrderItem>();
        for (OrderItem orderItem : order.getOrderItems()) {
            OrderItem orderItemResource
                    = new OrderItem();
            // if include stock item JSON serialisation exception occurs
            BeanUtils.copyProperties(orderItem, orderItemResource, "stockItem");
            orderItems.add(orderItemResource);
        }
        orderResource.setOrderItems(orderItems);

        orderResource.setFeedback(feedbackRepo.findByOrder(order.getTenantId(),
                order.getId()));

        Link detail = linkTo(OrderRepository.class, order.getId())
                .withSelfRel();
        orderResource.add(detail);
        orderResource.setSelfRef(detail.getHref());

        return orderResource;
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

    private Link linkTo(
            @SuppressWarnings("rawtypes") Class<? extends CrudRepository> clazz,
            Long id) {
        return new Link(clazz.getAnnotation(RepositoryRestResource.class)
                .path() + "/" + id);
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
        private Long orderId;
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
}
