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

import tools.jackson.core.JsonParser;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.JsonNode;

import link.omny.supportservices.model.CustomField;

/**
 */
public class JsonCustomFieldDeserializer<T> extends
        ValueDeserializer<Set<? extends CustomField>> {

    @Override
    public Set<? extends CustomField> deserialize(JsonParser jp,
            DeserializationContext ctxt) {
        Set<CustomField> set = new HashSet<CustomField>();
        JsonNode node = jp.readValueAsTree();

        for (Entry<String, JsonNode> entry : node.properties()) {
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
