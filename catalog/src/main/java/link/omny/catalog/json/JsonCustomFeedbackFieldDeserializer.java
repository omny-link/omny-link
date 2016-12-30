package link.omny.catalog.json;

import java.util.List;
import java.util.Map.Entry;

import link.omny.catalog.model.CustomFeedbackField;
import link.omny.custmgmt.json.JsonCustomFieldDeserializer;
import link.omny.custmgmt.model.CustomField;

import com.fasterxml.jackson.databind.JsonNode;

public class JsonCustomFeedbackFieldDeserializer extends
        JsonCustomFieldDeserializer<List<CustomFeedbackField>> {

    protected CustomField newInstance(Entry<String, JsonNode> entry) {
        return new CustomFeedbackField(entry.getKey(), entry.getValue()
                .asText());
    }

}
