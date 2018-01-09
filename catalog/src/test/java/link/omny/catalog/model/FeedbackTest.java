package link.omny.catalog.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Collections;

import org.junit.BeforeClass;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

public class FeedbackTest {

    private static ObjectMapper objectMapper;

    @BeforeClass
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
        CustomFeedbackField field1 = new CustomFeedbackField("field1", "foo");
        field1.setId(1l);
        feedback.addCustomField(field1);

        CustomFeedbackField field2 = new CustomFeedbackField("field1", "foo");
        assertNull(field2.getId());
        feedback.setCustomFields(Collections.singletonList(field2));

        assertEquals(1, feedback.getCustomFields().size());
        assertEquals(field1.getId(), feedback.getCustomFields().get(0).getId());

        StringWriter out = new StringWriter();
        try {
            objectMapper.writeValue(out, feedback);
            Feedback feedback2 = objectMapper.readValue(out.toString().getBytes(), Feedback.class);
            assertEquals(feedback, feedback2);
            assertEquals(1, feedback.getCustomFields().size());
            assertEquals(feedback.getCustomFields().size(), feedback2.getCustomFields().size());
            assertEquals("foo", feedback2.getCustomFieldValue("field1"));
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
