package link.omny.custmgmt.json;

import java.io.IOException;
import java.util.List;

import link.omny.custmgmt.model.CustomField;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

/**
 */
public class JsonCustomFieldSerializer extends
		JsonSerializer<List<CustomField>> {
    @Override
	public void serialize(List<CustomField> fields, JsonGenerator jgen,
			SerializerProvider serializerProvider) throws IOException,
			JsonProcessingException {
		// SimpleDateFormat formatter = new SimpleDateFormat("yyyy-dd-MM");
		// String format = formatter.format(date);
		// jsonGenerator.writeString(format);
		jgen.writeStartObject();
		// jgen.writeStringField("content", fields.toString());
		// jgen.writeStringField("type", fields.getClass().getName());

		// ... and here are the custom fields; masquerading as standard fields
		for (CustomField field : fields) {
			jgen.writeStringField(field.getName(), field.getValue());
		}

		jgen.writeEndObject();
    }
}
