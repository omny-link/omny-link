package link.omny.catalog.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Collections;

import org.junit.Test;

public class StockCategoryTest {

    @Test
    public void testMergeCustomFields() {
        StockCategory stockCategory = new StockCategory();
        CustomStockCategoryField field1 = new CustomStockCategoryField(
                "field1", "foo");
        field1.setId(1l);
        stockCategory.addCustomField(field1);

        CustomStockCategoryField field2 = new CustomStockCategoryField(
                "field1", "foo");
        assertNull(field2.getId());
        
        stockCategory.setCustomFields(Collections.singletonList(field2));
        
        assertEquals(1, stockCategory.getCustomFields().size());
        assertEquals(field1.getId(), stockCategory.getCustomFields().get(0)
                .getId());
    }

}
