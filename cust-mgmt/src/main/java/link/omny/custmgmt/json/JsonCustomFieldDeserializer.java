package link.omny.custmgmt.json;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import link.omny.custmgmt.model.CustomField;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

/**
 */
public abstract class JsonCustomFieldDeserializer<T> extends
        JsonDeserializer<List<? extends CustomField>> {

    @Override
    public List<? extends CustomField> deserialize(JsonParser jp,
            DeserializationContext ctxt) throws IOException,
            JsonProcessingException {
        List<CustomField> list = new ArrayList<CustomField>();
        ObjectCodec oc = jp.getCodec();
        JsonNode node = oc.readTree(jp);

        for (Iterator<Entry<String, JsonNode>> it = node.fields(); it.hasNext();) {
            Entry<String, JsonNode> entry = it.next();
            // if (!FIELDS.contains(entry.getKey())) {
            list.add((CustomField) newInstance(entry));
            // }
        }
        return list;
    }

    protected abstract Object newInstance(
            Entry<String, JsonNode> entry);
}
