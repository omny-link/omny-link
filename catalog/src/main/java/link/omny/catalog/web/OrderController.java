package link.omny.catalog.web;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import link.omny.catalog.json.JsonCustomOrderFieldDeserializer;
import link.omny.catalog.json.JsonCustomOrderItemFieldDeserializer;
import link.omny.catalog.model.CustomOrderField;
import link.omny.catalog.model.CustomOrderItemField;
import link.omny.catalog.model.Order;
import link.omny.catalog.model.OrderItem;
import link.omny.catalog.repositories.OrderRepository;
import link.omny.custmgmt.json.JsonCustomFieldSerializer;
import lombok.Data;
import lombok.EqualsAndHashCode;

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
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

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
    private OrderRepository orderRepo;

    /**
     * Return just the orders for a specific tenant.
     * 
     * @return orders for that tenant.
     */
    @RequestMapping(value = "/", method = RequestMethod.GET)
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

        return wrap(list);
    }

    /**
     * Return just the orders for a specific contact.
     * 
     * @return orders for that contact.
     */
    @RequestMapping(value = "/{contactId}", method = RequestMethod.GET)
    public @ResponseBody List<ShortOrder> listForContact(
            @PathVariable("tenantId") String tenantId,
            @PathVariable("contactId") Long contactId,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "limit", required = false) Integer limit) {
        LOGGER.info(String.format("List orders for contact %1$s & tenant %2$s",
                contactId, tenantId));

        List<Order> list;
        if (limit == null) {
            list = orderRepo.findAllForContact(contactId);
        } else {
            Pageable pageable = new PageRequest(page == null ? 0 : page, limit);
            list = orderRepo.findPageForContact(contactId, pageable);
        }
        LOGGER.info(String.format("Found %1$s orders", list.size()));

        return wrap(list);
    }

    /**
     * Create a new order.
     * 
     * @return
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @ResponseStatus(value = HttpStatus.CREATED)
    @RequestMapping(value = "/", method = RequestMethod.POST)
    public @ResponseBody ResponseEntity<?> create(
            @PathVariable("tenantId") String tenantId,
            @RequestBody Order order) {
        order.setTenantId(tenantId);

        for (OrderItem item : order.getOrderItems()) {
            item.setTenantId(tenantId);
            item.setOrder(order);
        }

        orderRepo.save(order);

        UriComponentsBuilder builder = MvcUriComponentsBuilder
                .fromController(getClass());
        HashMap<String, String> vars = new HashMap<String, String>();
        vars.put("tenantId", tenantId);
        vars.put("id", order.getId().toString());

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(builder.path("/{id}").buildAndExpand(vars).toUri());
        return new ResponseEntity(headers, HttpStatus.CREATED);
    }

    /**
     * Update an existing order.
     */
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    @RequestMapping(value = "/{id}", method = RequestMethod.PUT, consumes = { "application/json" })
    public @ResponseBody void update(@PathVariable("tenantId") String tenantId,
            @PathVariable("id") Long orderId,
            @RequestBody Order updatedOrder) {
        Order order = orderRepo.findOne(orderId);

        BeanUtils.copyProperties(updatedOrder, order, "id",
                "item");
        order.setTenantId(tenantId);
        orderRepo.save(order);
    }

    /**
     * Delete an existing stockCategory.
     */
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE, consumes = { "application/json" })
    public @ResponseBody void delete(@PathVariable("tenantId") String tenantId,
            @PathVariable("id") Long orderId) {
        orderRepo.delete(orderId);
    }

    private List<ShortOrder> wrap(List<Order> list) {
        List<ShortOrder> resources = new ArrayList<ShortOrder>(
                list.size());
        for (Order orders : list) {
            resources.add(wrap(orders));
        }
        return resources;
    }

    private ShortOrder wrap(Order order) {
        ShortOrder resource = new ShortOrder();

        BeanUtils.copyProperties(order, resource);


        ArrayList<ShortOrderItem> items = new ArrayList<ShortOrderItem>();
        for (OrderItem item : order.getOrderItems()) {
            items.add(wrap(item));
        }
        resource.setOrderItems(items);

        Link detail = linkTo(OrderRepository.class,
                order.getId()).withSelfRel();
        resource.add(detail);
        resource.setSelfRef(detail.getHref());
        return resource;
    }

    private ShortOrderItem wrap(OrderItem item) {
        ShortOrderItem resource = new ShortOrderItem();

        BeanUtils.copyProperties(item, resource);
        resource.setOrderItemId(item.getId());

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
    public static class ShortOrder extends ResourceSupport {
        private String selfRef;
        private String name;
        private String description;
        private Date date;
        private Date dueDate;
        @JsonDeserialize(using = JsonCustomOrderFieldDeserializer.class)
        @JsonSerialize(using = JsonCustomFieldSerializer.class)
        private List<CustomOrderField> customFields;
        private List<ShortOrderItem> orderItems;
        private String status;
        private BigDecimal price;
        private Date created;
        private Date lastUpdated;
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class ShortOrderItem extends ResourceSupport {
        private Long orderItemId;
        private String selfRef;
        private String name;
        private String description;
        private String status;
        private BigDecimal price;
        private Order order;
        @JsonDeserialize(using = JsonCustomOrderItemFieldDeserializer.class)
        @JsonSerialize(using = JsonCustomFieldSerializer.class)
        private List<CustomOrderItemField> customFields;
        private Date created;
        private Date lastUpdated;
        private String tenantId;
    }
}
