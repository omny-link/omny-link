package link.omny.catalog.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.io.InputStream;
import java.util.Collections;

import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

public class OrderTest {

    @Test
    public void testCustomFieldEquals() {
        CustomOrderField field1 = new CustomOrderField("field", "foo");
        field1.setId(1l);
        CustomOrderField field2 = new CustomOrderField("field", "foo");
        field1.setId(2l);

        assertEquals(field1, field2);
    }

    @Test
    public void testMergeCustomFields() {
        Order order = new Order();
        CustomOrderField field1 = new CustomOrderField("field1", "foo");
        field1.setId(1l);
        order.addCustomField(field1);

        CustomOrderField field2 = new CustomOrderField("field1", "foo");
        assertNull(field2.getId());
        
        order.setCustomFields(Collections.singletonList(field2));
        
        assertEquals(1, order.getCustomFields().size());
        assertEquals(field1.getId(), order.getCustomFields().get(0).getId());
    }

    @Test
    public void testDeserializeOrderWithItems() {
        ObjectMapper objectMapper = new ObjectMapper();
        InputStream is = null;
        try {
            is = getClass().getResourceAsStream(
                    "/testDeserializeOrderWithItems.json");
            assertNotNull("No test data file: testDeserializeOrderWithItems.json on classpath");
            Order order = objectMapper.readValue(is, Order.class);
            assertNotNull(order);
            assertNotNull(order.getOrderItems());
            assertEquals(2, order.getOrderItems().size());
            OrderItem item1 = order.getOrderItems().get(0);
            assertNotNull(item1.getStockItem());
            assertNotNull(item1.getStockItem().getId());
            assertNotNull(item1.getCustomFields());
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }

    }

}
