/*******************************************************************************
 * Copyright 2015-2022 Tim Stephenson and contributors
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
package link.omny.supportservices.json;

import java.io.IOException;
import java.util.Set;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import link.omny.supportservices.model.CustomField;

public class JsonCustomFieldSerializer extends
        JsonSerializer<Set<CustomField>> {

    @Override
    public void serialize(Set<CustomField> fields, JsonGenerator jgen,
            SerializerProvider serializerProvider) throws IOException,
            JsonProcessingException {
        // SimpleDateFormat formatter = new SimpleDateFormat("yyyy-dd-MM");
        // String format = formatter.format(date);
        // jsonGenerator.writeString(format);
        jgen.writeStartObject();
        // jgen.writeStringField("content", fields.toString());
        // jgen.writeStringField("type", fields.getClass().getName());
        //
        // ... and here are the custom fields; masquerading as standard fields
        for (CustomField field : fields) {
            // TODO, this ends up being ALWAYS, not sure how
//            System.out.println("XXXX"
//                    + serializerProvider.getConfig()
//                    .getSerializationInclusion());
            if (field.getValue() != null) {
                jgen.writeStringField(field.getName(), field.getValue());
            } 
        }

        jgen.writeEndObject();
    }
}
