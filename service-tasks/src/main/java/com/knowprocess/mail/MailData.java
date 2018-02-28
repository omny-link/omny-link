/*******************************************************************************
 *Copyright 2011-2018 Tim Stephenson and contributors
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

    public MailData(String template, String subject, String email) {
        this();
        properties.setProperty("template", template);
        properties.setProperty("subject", subject);
        properties.setProperty("contact.email", email);
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

    public static MailData fromJson(String json) {
        LOGGER.debug(String.format("fromJson(%1$s)", json));
        MailData md = new MailData();
        md.json = json;
        md.parse(json);
        return md;
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

    public String toJson() {
        String s = String
                .format("{\"template\":\"%1$s\",\"subject\":\"%2$s\", \"contact\":{\"email\":\"%3$s\",\"firstName\":\"%4$s\"}}",
                get("template"), get("subject"),
                get("contact.email"), get("contact.firstName"));
        return s;
    }

    @Override
    public String toString() {
        if (obj == null) {
            String s = String
                    .format("Mail Data:\n  template: %1$s,\n  subject:%2$s,\n  contact email:\n  %3$s,\n  first name: %4$s",
                            get("template"), get("subject"),
                            get("contact.email"), get("contact.firstName"));
            return s;
        } else {
            return obj.toString();
        }
    }

}
