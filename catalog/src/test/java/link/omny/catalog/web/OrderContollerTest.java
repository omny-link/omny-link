package link.omny.catalog.web;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.math.BigDecimal;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import link.omny.catalog.TestApplication;
import link.omny.catalog.model.CustomFeedbackField;
import link.omny.catalog.model.CustomOrderField;
import link.omny.catalog.model.CustomOrderItemField;
import link.omny.catalog.model.Feedback;
import link.omny.catalog.model.Order;
import link.omny.catalog.model.OrderItem;
import link.omny.catalog.model.api.OrderWithSubEntities;
import link.omny.catalog.model.api.ShortOrder;
import link.omny.catalog.web.OrderController.FeedbackResource;

/**
 * @author Tim Stephenson
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = TestApplication.class)
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
    public void testSimpleOrderLifecycle() {
        Order order = createOrder();
        assertNotNull(order);
        assertNotNull(order.getId());
        assertEquals(PRICE, order.getPrice());
        assertEquals(CONTACT_ID, order.getContactId());
        assertEquals(2, order.getPrice().scale());

        OrderWithSubEntities order2 = retrieveOrder(order.getId());
        assertNotNull(order2);
        assertEquals(2, order2.getCustomFields().size());

        order.setInvoiceRef(INVOICE_REF);
        ShortOrder order3 = updateOrder(order.getId(), order);
        assertNotNull(order3);
        assertEquals(INVOICE_REF, order3.getInvoiceRef());

        Feedback feedback = createFeedback();
        addFeedback(order.getId(), feedback);
        FeedbackResource feedback2 = retrieveFeedback(order.getId());
        assertNotNull(feedback2);
        // h2 database does not do created time default
        // assertNotNull(orderIncFeedback.getFeedback().getCreated());
        assertEquals(FEEDBACK, feedback2.getDescription());
        
        feedback.addCustomField(new CustomFeedbackField(FEEDBACK_CUSTOM_KEY,
                FEEDBACK_CUSTOM_VALUE));
        addFeedback(order.getId(), feedback);
        FeedbackResource feedback3 = retrieveFeedback(order.getId());
        assertNotNull(feedback3);
        assertEquals(1, feedback3.getCustomFields().size());
        assertEquals(FEEDBACK_CUSTOM_KEY, feedback3.getCustomFields().get(0)
                .getName());
        assertEquals(FEEDBACK_CUSTOM_VALUE, feedback3.getCustomFields().get(0)
                .getValue());

        deleteOrder(order.getId());

        ShortOrder order4 = retrieveOrder(order.getId());
        assertEquals("deleted", order4.getStage());
    }

    @Test
    public void testOrderWithItemsLifecycle() {
        Order order = createOrderAndItems();
        assertNotNull(order);
        assertNotNull(order.getId());
        assertEquals(2, order.getOrderItems().size());
        OrderItem orderItem = order.getOrderItems().get(0);
        assertEquals(PRICE, orderItem.getPrice());
        assertEquals("Avocado", orderItem.getCustomFieldValue(CUST_FIELD_COLOUR));

        OrderWithSubEntities order2 = retrieveOrder(order.getId());
        assertNotNull(order2);

        // Update price
        assertEquals(2, order2.getOrderItems().size());
        OrderItem orderItem2 = order2.getOrderItems().get(0);
        assertEquals(PRICE, orderItem2.getPrice());
        order.getOrderItems().get(0).setPrice(PRICE_INCREASED);
        updateOrder(order.getId(), order);
        OrderWithSubEntities order2b = retrieveOrder(order.getId());
        assertNotNull(order2b);
        OrderItem orderItem2b = order2b.getOrderItems().get(0);
        assertEquals(PRICE_INCREASED.doubleValue(), orderItem2b.getPrice()
                .doubleValue(), 0.01);

        // Update custom field
        order.getOrderItems().get(0).setCustomField(
                        new CustomOrderItemField(CUST_FIELD_COLOUR, "Absinthe"));
        updateOrder(order.getId(), order);
        OrderWithSubEntities order2c = retrieveOrder(order.getId());
        assertNotNull(order2c);
        OrderItem orderItem2c = order2c.getOrderItems().get(0);
        assertEquals(1, orderItem2c.getCustomFields().size());
        // assertEquals("Absinthe", orderItem2c.getCustomFields());

        order.setInvoiceRef(INVOICE_REF);
        ShortOrder order3 = updateOrder(order.getId(), order);
        assertNotNull(order3);
        assertEquals(INVOICE_REF, order3.getInvoiceRef());

        Feedback feedback = createFeedback();
        addFeedback(order.getId(), feedback);
        addFeedback(order.getId(), feedback);

        deleteOrder(order.getId());

        ShortOrder order4 = retrieveOrder(order.getId());
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

    private ShortOrder retrieveOrder() {
        List<Order> allOrders = svc.listForTenant(TENANT_ID, null, null);
        assertEquals(1, allOrders.size());
        return allOrders.get(0);
    }

    private OrderWithSubEntities retrieveOrder(Long orderId) {
        return svc.readOrder(TENANT_ID, orderId);
    }

    private FeedbackResource retrieveFeedback(Long orderId) {
        return svc.readFeedback(TENANT_ID, orderId);
    }

    private ShortOrder updateOrder(Long orderId, Order updatedOrder) {
        svc.update(TENANT_ID, orderId, updatedOrder);
        ShortOrder retrievedOrder = retrieveOrder();
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
