package com.knowprocess.mail;

import java.io.Serializable;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonValue;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MailData implements Serializable {

    private static final long serialVersionUID = 2240381003869008173L;

    protected static final Logger LOGGER = LoggerFactory
            .getLogger(MailData.class);

    private Properties properties;

    private transient JsonObject obj;

    private String json;

    public MailData() {
        properties = new Properties();
    }

    /**
     * Synonym for <code>fromJson</code> to conform to Spring Roo naming
     * conventions.
     * 
     * @see fromJson
     * @param json
     * @return instance parsed from the json parameter.
     */
    public MailData fromJsonToMailData(String json) {
        return fromJson(json);
    }

    // TODO should be static
    public MailData fromJson(String json) {
        LOGGER.debug(String.format("fromJson(%1$s)", json));
        this.json = json;
        parse(json);
        return this;
    }

    public static List<MailData> fromJsonArray(String json) {
        JsonReader reader = Json.createReader(new StringReader(json));
        JsonArray array = reader.readArray();
        List<MailData> list = new ArrayList<MailData>();
        for (JsonValue jsonValue : array) {
            list.add(new MailData().fromJson(jsonValue.toString()));
        }
        return list;
    }

    public MailData fromFlattenedJson(String json) {
        JsonParser parser = Json.createParser(new StringReader(json));
        while (parser.hasNext()) {
            Event e = parser.next();
            if (e == Event.KEY_NAME) {
                // System.out.println(parser.getString());
                // System.out.println(parser.getLocation());
                int start = json.indexOf('"', (int) parser.getLocation()
                        .getStreamOffset() + 1);
                properties
                        .setProperty(
                                parser.getString(),
                                json.substring(start + 1,
                                        json.indexOf('"', start + 1)));
            }
        }
        return this;
    }

    private void parse(String json) {
        JsonReader reader = Json.createReader(new StringReader(json));
        obj = reader.readObject();
        System.out.println("obj" + obj);
        System.out.println("parsed: " + this);
    }

    public String get(String key) {
        LOGGER.info(String.format("get(%1$s)", key));
        if (properties.size() > 0) {
            return properties.getProperty(key, " - ");
        } else {
            return getFromJson(key);
        }
    }

    private String getFromJson(String key) {
        if (obj == null) {
            parse(json);
        }
        String[] keys = key.split("\\.");
        Object val = obj;
        for (int i = 0; i < keys.length; i++) {
            String s = keys[i];
            if (i + 1 < keys.length) {
                val = ((JsonObject) val).getJsonObject(s);
            } else {
                val = ((JsonObject) val).get(s);
            }
        }
        if (val == null) {
            val = "";
        } else {
            // For some reason the JSON Value retains the " wrappers
            val = val.toString();
            if (((String) val).startsWith("\"")) {
                val = ((String) val).substring(1, ((String) val).length() - 1);
            }
        }
        return (String) val;
    }

    @Override
    public String toString() {
        return "MailData: " + (obj == null ? "null." : obj.toString());
    }

}
