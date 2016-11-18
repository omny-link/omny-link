package link.omny.catalog.json;

import java.util.List;
import java.util.Map.Entry;

import link.omny.catalog.model.CustomOrderItemField;
import link.omny.custmgmt.json.JsonCustomFieldDeserializer;
import link.omny.custmgmt.model.CustomField;

import com.fasterxml.jackson.databind.JsonNode;

public class JsonCustomOrderItemFieldDeserializer extends
        JsonCustomFieldDeserializer<List<CustomOrderItemField>> {

    protected CustomField newInstance(Entry<String, JsonNode> entry) {
        return new CustomOrderItemField(entry.getKey(), entry.getValue()
                .asText());
    }

}
