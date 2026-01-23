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
package link.omny.custmgmt.json;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;

public class CustomBooleanDeserializer extends ValueDeserializer<Boolean> {
    protected static final Logger LOGGER = LoggerFactory
            .getLogger(CustomBooleanDeserializer.class);
    protected static final Class<?> _valueClass = Boolean.class;

    @Override
    public Boolean deserialize(JsonParser jp, DeserializationContext ctxt)
            throws JacksonException {
        LOGGER.info("deserialize boolean from json");
        JsonToken t = jp.currentToken();
        if (t == JsonToken.VALUE_TRUE) {
            return Boolean.TRUE;
        }
        if (t == JsonToken.VALUE_FALSE) {
            return Boolean.FALSE;
        }
        if (t == JsonToken.VALUE_NULL) {
            return Boolean.FALSE;
        }
        if (t == JsonToken.VALUE_NUMBER_INT) {
            return (jp.getIntValue() != 0);
        }
        if (t == JsonToken.VALUE_STRING) {
            String text = jp.getText().trim();
            if ("true".equals(text)) {
                return Boolean.TRUE;
            }
            if ("false".equals(text) || text.length() == 0) {
                return Boolean.FALSE;
            }

            if ("N".equalsIgnoreCase(text) || text.length() == 0) {
                return Boolean.FALSE;
            }

            if ("Y".equalsIgnoreCase(text)) {
                return Boolean.TRUE;
            }
            // Now we're getting desperate
            if (text.toLowerCase().contains("agree")
                    || text.toLowerCase().contains("confirm")
                    || text.toLowerCase().contains("happy")
                    || text.toLowerCase().contains("yes")) {
                return Boolean.TRUE;
            } else {
                return Boolean.FALSE;
            }
        }
        ctxt.handleUnexpectedToken(_valueClass, jp);
        return Boolean.FALSE;
    }
}
