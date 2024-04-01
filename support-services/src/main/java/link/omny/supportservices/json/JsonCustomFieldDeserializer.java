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
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import link.omny.supportservices.model.CustomField;

/**
 */
public class JsonCustomFieldDeserializer<T> extends
        JsonDeserializer<Set<? extends CustomField>> {

    @Override
    public Set<? extends CustomField> deserialize(JsonParser jp,
            DeserializationContext ctxt) throws IOException,
            JsonProcessingException {
        Set<CustomField> set = new HashSet<CustomField>();
        ObjectCodec oc = jp.getCodec();
        JsonNode node = oc.readTree(jp);

        for (Iterator<Entry<String, JsonNode>> it = node.fields(); it.hasNext();) {
            Entry<String, JsonNode> entry = it.next();
            // if (!FIELDS.contains(entry.getKey())) {
            set.add((CustomField) newInstance(entry));
            // }
        }
        return set;
    }

    protected CustomField newInstance(Entry<String, JsonNode> entry) {
        return new CustomField(entry.getKey(), entry.getValue().asText());
    }
}
