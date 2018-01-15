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

    @Test
    public void testSortTypes() {
        StockCategory category = new StockCategory("Springfield")
                .addStockItem(new StockItem("Springfield 1", "Power Plant", "Published"))
                .addStockItem(new StockItem("Office 1", "Office", "Published"));

        assertEquals("Office,Power Plant", category.getTags());
    }

    @Test
    public void testOverrideTypes() {
        StockCategory category = new StockCategory("Springfield").addStockItem(
                new StockItem("Springfield 1", "Power Plant")).addStockItem(
                new StockItem("Office 1", "Office"));

        category.setTags("Office");
        assertEquals("Office", category.getTags());
    }

}
