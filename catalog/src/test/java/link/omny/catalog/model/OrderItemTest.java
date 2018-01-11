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
