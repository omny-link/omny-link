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
package link.omny.catalog.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.hateoas.EntityModel;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;

import link.omny.catalog.CatalogTestApplication;
import link.omny.catalog.model.CustomFeedbackField;
import link.omny.catalog.model.CustomOrderField;
import link.omny.catalog.model.CustomOrderItemField;
import link.omny.catalog.model.Feedback;
import link.omny.catalog.model.Order;
import link.omny.catalog.model.OrderItem;

/**
 * @author Tim Stephenson
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = CatalogTestApplication.class)
@WebAppConfiguration
public class OrderContollerTest {
    private static final String CUST_FIELD_COLOUR = "colour";

    private static final CustomOrderField CUSTOM_FIELD_2 = new CustomOrderField("field2", "bar");

    private static final CustomOrderField CUSTOM_FIELD_1 = new CustomOrderField("field1", "foo");

    private static final String FEEDBACK = "5 stars";

    private static final String FEEDBACK_CUSTOM_KEY = "timeliness";

    private static final String FEEDBACK_CUSTOM_VALUE = "Good";

    private static final BigDecimal PRICE = new BigDecimal("9.99");

    private static final BigDecimal PRICE_INCREASED = PRICE
            .multiply(new BigDecimal(1.15));

    private static final String INVOICE_REF = "INV123";

    private static final String TENANT_ID = "omny";

    private static final Long CONTACT_ID = 1l;

    @Autowired
    private OrderController svc;

    @Test
    public void testParseSingleOrderId() {
        Long[] ids = svc.parseOrderIds("12");
        assertEquals(1, ids.length);
        assertEquals(12l, ids[0].longValue());
    }

    @Test
    public void testParseOrderIds() {
        Long[] ids = svc.parseOrderIds("[12,15]");
        assertEquals(2, ids.length);
        assertEquals(12l, ids[0].longValue());
        assertEquals(15l, ids[1].longValue());
    }

    @Test
    public void testSimpleOrderLifecycle() {
        Order order = createOrder();
        assertNotNull(order);
        assertNotNull(order.getId());
        assertEquals(PRICE, order.getPrice());
        assertEquals(CONTACT_ID, order.getContactId());
        assertEquals(2, order.getPrice().scale());

        Order order2 = retrieveOrder(order.getId());
        assertNotNull(order2);
        assertEquals(2, order2.getCustomFields().size());

        order.setInvoiceRef(INVOICE_REF);
        Order order3 = updateOrder(order.getId(), order);
        assertNotNull(order3);
        assertEquals(INVOICE_REF, order3.getInvoiceRef());

        Feedback feedback = createFeedback();
        addFeedback(order.getId(), feedback);
        Feedback feedback2 = retrieveFeedback(order.getId());
        assertNotNull(feedback2);
        // h2 database does not do created time default
        // assertNotNull(orderIncFeedback.getFeedback().getCreated());
        assertEquals(FEEDBACK, feedback2.getDescription());
        
        feedback.addCustomField(new CustomFeedbackField(FEEDBACK_CUSTOM_KEY,
                FEEDBACK_CUSTOM_VALUE));
        addFeedback(order.getId(), feedback);
        Feedback feedback3 = retrieveFeedback(order.getId());
        assertNotNull(feedback3);
        assertEquals(1, feedback3.getCustomFields().size());
        assertEquals(FEEDBACK_CUSTOM_KEY, feedback3.getCustomFields().iterator().next()
                .getName());
        assertEquals(FEEDBACK_CUSTOM_VALUE, feedback3.getCustomFields().iterator().next()
                .getValue());
        
        // check equivalence of readOrder and readOrders API
        List<EntityModel<Order>> orders = svc.readOrders(TENANT_ID, order.getId().toString());
        Order order4 = orders.get(0).getContent();
        assertEquals(order.getId(), order4.getId());
        assertEquals(order.getName(), order4.getName());
        assertEquals(order.getType(), order4.getType());
        assertEquals(order.getOrderItems().size(), order4.getOrderItems().size());

        deleteOrder(order.getId());

        Order order5 = retrieveOrder(order.getId());
        assertEquals("deleted", order5.getStage());
    }

    @Test
    @Disabled
    public void testOrderWithItemsLifecycle() {
        Order order = createOrderAndItems();
        assertNotNull(order);
        assertNotNull(order.getId());
        assertEquals(2, order.getOrderItems().size());
        assertTrue(order.getOrderItems().stream()
                .filter(x -> (x.getCustomFieldValue(CUST_FIELD_COLOUR).equals("Avocado")
                        && x.getPrice().equals(PRICE)))
                .findFirst().isPresent());

        Order order2 = retrieveOrder(order.getId());
        assertNotNull(order2);

        // Update price
        assertEquals(2, order2.getOrderItems().size());
        OrderItem orderItem2 = order2.getOrderItems().iterator().next();
        assertEquals(PRICE, orderItem2.getPrice());
        orderItem2.setPrice(PRICE_INCREASED);
        Order order2b = updateOrder(order2.getId(), order2);
        assertNotNull(order2b);
        OrderItem orderItem2b = order2b.getOrderItems().iterator().next();
        assertEquals(PRICE_INCREASED.doubleValue(), orderItem2b.getPrice()
                .doubleValue(), 0.01);

        // Update custom field
        order2b.getOrderItems().iterator().next().addCustomField(
                        new CustomOrderItemField(CUST_FIELD_COLOUR, "Absinthe"));
        Order order2c = updateOrder(order2b.getId(), order2b);
        assertNotNull(order2c);
        OrderItem orderItem2c = order2c.getOrderItems().iterator().next();
        assertEquals(1, orderItem2c.getCustomFields().size());
//        assertEquals("Absinthe", orderItem2c.getCustomFields().iterator().next().getValue());

        order.setInvoiceRef(INVOICE_REF);
        Order order3 = updateOrder(order.getId(), order);
        assertNotNull(order3);
        assertEquals(INVOICE_REF, order3.getInvoiceRef());

        Feedback feedback = createFeedback();
        addFeedback(order.getId(), feedback);
        addFeedback(order.getId(), feedback);

        deleteOrder(order.getId());

        Order order4 = retrieveOrder(order.getId());
        assertEquals("deleted", order4.getStage());
    }

    private Order createOrder() {
        Order order = getSimpleOrder();
        svc.create(TENANT_ID, order);

        return order;
    }

    private Order getSimpleOrder() {
        Order order = new Order("1 type A widget");
        order.setPrice(PRICE);
        order.setContactId(CONTACT_ID);

        order.addCustomField(CUSTOM_FIELD_1);
        order.addCustomField(CUSTOM_FIELD_2);

        return order;
    }

    private Order createOrderAndItems() {
        Order order = getOrderAndItems();
        svc.create(TENANT_ID, order);

        return order;
    }

    private Order getOrderAndItems() {
        Order order = new Order("Basket");

        OrderItem orderItem1 = new OrderItem("Widget");
        orderItem1.setPrice(PRICE);
        orderItem1.addCustomField(new CustomOrderItemField(CUST_FIELD_COLOUR,
                "Avocado"));
        order.addOrderItem(orderItem1);
        
        OrderItem orderItem2 = new OrderItem("Widget");
        orderItem2.setPrice(PRICE);
        orderItem2
                .addCustomField(new CustomOrderItemField(CUST_FIELD_COLOUR, "Blue"));
        order.addOrderItem(orderItem2);

        return order;
    }

    private Order retrieveOrder() {
        List<Order> allOrders = svc.listForTenant(TENANT_ID, null, null);
        assertEquals(1, allOrders.size());
        return allOrders.get(0);
    }

    private Order retrieveOrder(Long orderId) {
        return svc.findEntityById(TENANT_ID, orderId).getContent();
    }

    private Feedback retrieveFeedback(Long orderId) {
        return svc.readFeedback(TENANT_ID, orderId);
    }

    private Order updateOrder(Long orderId, Order updatedOrder) {
        svc.update(TENANT_ID, orderId, updatedOrder);
        Order retrievedOrder = retrieveOrder();
        return retrievedOrder;
    }

    private Feedback addFeedback(Long orderId, Feedback feedback) {
        svc.addFeedback(TENANT_ID, orderId, feedback);
        return feedback;
    }

    private Feedback createFeedback() {
        Feedback feedback = new Feedback();
        feedback.setDescription(FEEDBACK);
        return feedback;
    }

    private void deleteOrder(Long orderId) {
        svc.delete(TENANT_ID, orderId);
    }
}
