package link.omny.catalog.json;

import java.util.List;
import java.util.Map.Entry;

import link.omny.catalog.model.CustomStockItemField;
import link.omny.custmgmt.json.JsonCustomFieldDeserializer;
import link.omny.custmgmt.model.CustomField;

import com.fasterxml.jackson.databind.JsonNode;

public class JsonCustomOrderItemFieldDeserializer extends
        JsonCustomFieldDeserializer<List<CustomStockItemField>> {

    protected CustomField newInstance(Entry<String, JsonNode> entry) {
        return new CustomStockItemField(entry.getKey(), entry.getValue()
                .asText());
    }

}
