package com.knowprocess.mail;

import java.io.Serializable;
import java.io.StringReader;
import java.util.Properties;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

public class MailData implements Serializable {

	private static final long serialVersionUID = 2240381003869008173L;

	private Properties properties;

	private transient JsonObject obj;

	private String json;

	public MailData() {
		properties = new Properties();
	}

	public MailData fromJson(String json) {
		this.json = json;
		parse(json);
		return this;
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
