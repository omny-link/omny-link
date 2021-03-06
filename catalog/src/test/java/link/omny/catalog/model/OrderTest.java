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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;

import org.junit.BeforeClass;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import link.omny.custmgmt.model.Note;

public class OrderTest {

    private static ObjectMapper objectMapper;

    @BeforeClass
    public static void setUp() {
        objectMapper = new ObjectMapper();
    }

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

        OrderItem orderItem1 = new OrderItem("Widget");
        orderItem1.setId(1l);
        orderItem1
                .addCustomField(new CustomOrderItemField("colour", "Avocado"));
        order.addOrderItem(orderItem1);

        CustomOrderField field2 = new CustomOrderField("field1", "foo");
        assertNull(field2.getId());
        order.setCustomFields(Collections.singletonList(field2));

        orderItem1.addCustomField(
                new CustomOrderItemField("colour", "Blue"));

        assertEquals(1, order.getCustomFields().size());
        assertEquals(1, order.getOrderItems().size());
        assertEquals(1, order.getOrderItems().get(0).getCustomFields().size());
        assertEquals(field1.getId(), order.getCustomFields().get(0).getId());
    }

    @Test
    public void testDeserializeOrderWithItems() {
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

    @Test
    public void testPayload() throws IOException {
        String json = "{\"feedback\":{\"customFields\":{\"coachComments\":\"Coach says went well\"}},\"orderLocalId\":\"2516\",\"ip\":\"80.234.225.146\",\"admin_email\":\"info@a-life.co.uk\",\"tenantId\":\"acme\"}";
        Order order = objectMapper.readValue(json .getBytes(), Order.class);
        assertEquals(1, order.getFeedback().getCustomFields().size());
        assertEquals("Coach says went well", order.getFeedback().getCustomFieldValue("coachComments"));
    }

    @Test
    public void testToCsv() throws IOException {
        Date now = new Date();
        Order order = new Order(1l, 1l, "My first order",
                "A description including\nseveral\nline breaks",
                "order", now, now, "confirmed",
                new BigDecimal("100"),new BigDecimal("20"));
        order.addNote(new Note(1l, "tim@knowprocess.com", now,
                "A single-line note", true, false));
        order.addNote(new Note(2l, "tim@knowprocess.com", now,
                "A note\nthat spans multiple lines", true, false));
        assertEquals(2,  order.getNotes().size());

        String csv = order.toCsv();
        assertTrue(csv.startsWith(
                "1,My first order,1,order,\"A description including\n"
                + "several\nline breaks\",,,New enquiry,100,20,,,,,null,"));
        assertTrue(csv.contains("tim@knowprocess.com: A single-line note"));
        assertTrue(csv.contains("tim@knowprocess.com: A note\n"
                + "that spans multiple lines;"));
    }
}
