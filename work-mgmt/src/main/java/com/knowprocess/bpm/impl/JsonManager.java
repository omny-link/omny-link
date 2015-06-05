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
