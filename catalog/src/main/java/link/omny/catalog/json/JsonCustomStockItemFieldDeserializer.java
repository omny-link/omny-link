package link.omny.catalog.json;

import java.util.List;
import java.util.Map.Entry;

import link.omny.catalog.model.CustomStockCategoryField;
import link.omny.custmgmt.json.JsonCustomFieldDeserializer;
import link.omny.custmgmt.model.CustomContactField;
import link.omny.custmgmt.model.CustomField;

import com.fasterxml.jackson.databind.JsonNode;

public class JsonCustomStockItemFieldDeserializer extends
        JsonCustomFieldDeserializer<List<CustomContactField>> {

    protected CustomField newInstance(Entry<String, JsonNode> entry) {
        return new CustomStockCategoryField(entry.getKey(), entry.getValue()
                .asText());
    }

}
