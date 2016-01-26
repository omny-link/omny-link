package link.omny.custmgmt.json;

import java.util.List;
import java.util.Map.Entry;

import link.omny.custmgmt.model.CustomAccountField;
import link.omny.custmgmt.model.CustomField;

import com.fasterxml.jackson.databind.JsonNode;

public class JsonCustomAccountFieldDeserializer extends
        JsonCustomFieldDeserializer<List<CustomAccountField>> {

    protected CustomField newInstance(Entry<String, JsonNode> entry) {
        return new CustomAccountField(entry.getKey(), entry.getValue().asText());
    }

}
