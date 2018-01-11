/*******************************************************************************
 *Copyright 2018 Tim Stephenson and contributors
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
package com.knowprocess.bpm.impl;

import java.io.StringReader;
import java.io.StringWriter;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsonManager {

    protected static final Logger LOGGER = LoggerFactory
            .getLogger(JsonManager.class);

    public JsonObject toObject(String json) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Parsing: " + json);
        }
        JsonReader jsonReader = Json.createReader(new StringReader(json));
        return jsonReader.readObject();
    }

    public boolean appearsToBeJson(Object value) {
        return value instanceof String
                && (value.toString().contains("{") || value.toString()
                        .contains("["));
    }

    public String stringify(JsonObject value) {
        StringWriter sw = new StringWriter();
        JsonWriter jsonWriter = Json.createWriter(sw);
        jsonWriter.writeObject(value);
        jsonWriter.close();
        return sw.toString();
    }
}
