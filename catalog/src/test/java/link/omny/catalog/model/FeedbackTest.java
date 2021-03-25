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
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Collections;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

public class FeedbackTest {

    private static ObjectMapper objectMapper;

    @BeforeAll
    public static void setUp() {
        objectMapper = new ObjectMapper();
    }

    @Test
    public void testCustomFieldEquals() {
        CustomFeedbackField field1 = new CustomFeedbackField("field", "foo");
        field1.setId(1l);
        CustomFeedbackField field2 = new CustomFeedbackField("field", "foo");
        field1.setId(2l);

        assertEquals(field1, field2);
    }

    @Test
    public void testMergeCustomFields() {
        Feedback feedback = new Feedback();
        feedback.setId(1l);
        feedback.setDescription("Great service!");
        feedback.setType("customer");
        CustomFeedbackField field1 = new CustomFeedbackField("name", "foo");
        field1.setId(1l);
        feedback.addCustomField(field1);

        CustomFeedbackField field2 = new CustomFeedbackField("name", "bar");
        assertNull(field2.getId());
        feedback.setCustomFields(Collections.singleton(field2));

        // ensure setting custom field has resulted in merge
        assertEquals(1, feedback.getCustomFields().size());
        assertEquals(field1.getId(), feedback.getCustomFields().iterator().next().getId());
        assertEquals(field2.getValue(), feedback.getCustomFields().iterator().next().getValue());

        StringWriter out = new StringWriter();
        try {
            objectMapper.writeValue(out, feedback);
            Feedback feedback2 = objectMapper.readValue(out.toString().getBytes(), Feedback.class);
            // Cannot use equals because serialisation converts customFields:List to object
            assertEquals(feedback.getId(), feedback2.getId());
            assertEquals(feedback.getDescription(), feedback2.getDescription());
            assertEquals(feedback.getType(), feedback2.getType());
            assertEquals(feedback.getCustomFields().size(), feedback2.getCustomFields().size());
            assertEquals("bar", feedback2.getCustomFieldValue("name"));
        } catch (IOException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    @Test
    public void testPayload() throws IOException {
        String json = "{\"customFields\":{\"coachComments\":\"Coach says went well\"}}";
        Feedback feedback = objectMapper.readValue(json .getBytes(), Feedback.class);
        assertEquals(1, feedback.getCustomFields().size());
        assertEquals("Coach says went well", feedback.getCustomFieldValue("coachComments"));
    }
}
