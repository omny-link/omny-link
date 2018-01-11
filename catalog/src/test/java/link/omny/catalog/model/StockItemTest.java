/*******************************************************************************
 *Copyright 2018 Tim Stephenson and contributors
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
}
