/*******************************************************************************
 *Copyright 2015-2018 Tim Stephenson and contributors
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Collections;

import org.junit.jupiter.api.Test;

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
        
        stockCategory.setCustomFields(Collections.singleton(field2));
        
        assertEquals(1, stockCategory.getCustomFields().size());
        assertEquals(field1.getId(), stockCategory.getCustomFields().iterator().next()
                .getId());
    }

}
