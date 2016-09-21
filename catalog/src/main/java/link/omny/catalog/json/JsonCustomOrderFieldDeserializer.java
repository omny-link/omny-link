package link.omny.catalog.json;

import java.util.List;
import java.util.Map.Entry;

import link.omny.catalog.model.CustomOrderField;
import link.omny.custmgmt.json.JsonCustomFieldDeserializer;
import link.omny.custmgmt.model.CustomField;

import com.fasterxml.jackson.databind.JsonNode;

public class JsonCustomOrderFieldDeserializer extends
        JsonCustomFieldDeserializer<List<CustomOrderField>> {

    protected CustomField newInstance(Entry<String, JsonNode> entry) {
        return new CustomOrderField(entry.getKey(), entry.getValue()
                .asText());
    }

}
