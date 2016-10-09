package link.omny.catalog.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Collections;

import org.junit.Test;

public class StockItemTest {

    @Test
    public void testCustomFieldEquals() {
        CustomStockItemField field1 = new CustomStockItemField("field", "foo");
        field1.setId(1l);
        CustomStockItemField field2 = new CustomStockItemField("field", "foo");
        field1.setId(2l);

        assertEquals(field1, field2);
    }

    @Test
    public void testMergeCustomFields() {
        StockItem stockItem = new StockItem("Widget1", "Widget");
        CustomStockItemField field1 = new CustomStockItemField("field1", "foo");
        field1.setId(1l);
        stockItem.addCustomField(field1);

        CustomStockItemField field2 = new CustomStockItemField("field1", "foo");
        assertNull(field2.getId());
        
        stockItem.setCustomFields(Collections.singletonList(field2));
        
        assertEquals(1, stockItem.getCustomFields().size());
        assertEquals(field1.getId(), stockItem.getCustomFields().get(0).getId());
    }

    @Test
    public void testDefaultDescriptionByType() {
        StockItem stockItem = new StockItem("Widget1", "Widget");
        assertNull(stockItem.getDescription());
        stockItem.setTenantId("omny");
        assertEquals("Default description of Widget",
                stockItem.getDescription());

        stockItem = new StockItem("Widget2", "Whatchamacallit");
        assertNull(stockItem.getDescription());
        stockItem.setTenantId("omny");
        assertEquals("Default description of Whatchamacallit",
                stockItem.getDescription());
    }

}
