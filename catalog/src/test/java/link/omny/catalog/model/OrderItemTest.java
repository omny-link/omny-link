package link.omny.catalog.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Collections;

import org.junit.Test;

public class OrderItemTest {

    @Test
    public void testCustomFieldEquals() {
        CustomOrderItemField field1 = new CustomOrderItemField("field", "foo");
        field1.setId(1l);
        CustomOrderItemField field2 = new CustomOrderItemField("field", "foo");
        field1.setId(2l);

        assertEquals(field1, field2);
    }

    @Test
    public void testMergeCustomFields() {
        OrderItem orderItem = new OrderItem();
        CustomOrderItemField field1 = new CustomOrderItemField("field1", "foo");
        field1.setId(1l);
        orderItem.addCustomField(field1);
        orderItem.addCustomField(new CustomOrderItemField("colour", "Avocado"));

        CustomOrderItemField field2 = new CustomOrderItemField("field1", "bar");
        assertNull(field2.getId());
        orderItem.setCustomFields(Collections.singletonList(field2));

        orderItem.addCustomField(
                new CustomOrderItemField("colour", "Blue"));
        
        assertEquals(2, orderItem.getCustomFields().size());
        assertEquals("bar", orderItem.getCustomFieldValue("field1"));
        assertEquals("Blue", orderItem.getCustomFieldValue("colour"));
        assertEquals(field1.getId(), orderItem.getCustomFields().get(0).getId());
    }

}
