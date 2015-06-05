package com.knowprocess.bpm.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import javax.json.JsonObject;
import javax.json.stream.JsonParsingException;

import org.junit.BeforeClass;
import org.junit.Test;

public class JsonManagerTest {

    private static JsonManager jsonManager;

    @BeforeClass
    public static void setUpClass() {
        jsonManager = new JsonManager();
    }

    @Test
    public void testGoodInputVariable() {
        String json = "{\"firstName\":\"Franz\",\"lastName\":\"Kafka\"}";
        JsonObject jObj = jsonManager.toObject(json);
        assertNotNull(jObj);
        assertEquals("Franz", jObj.getString("firstName"));
        assertEquals("Kafka", jObj.getString("lastName"));
    }

    @Test
    public void testGoodNestedInputVariable() {
        String json = "{\"firstName\":\"George\",\"lastName\":\"Orwell\", "
                + "\"account\":{\"name\":\"Penguin\"}}";
        JsonObject jObj = jsonManager.toObject(json);
        assertNotNull(jObj);
        assertEquals("Penguin",
                ((JsonObject) jObj.get("account")).getString("name"));
    }

    @Test
    public void testBadInputVariable() {
        try {
            String nonJson = "George";
            JsonObject jObj = jsonManager.toObject(nonJson);
            fail();
        } catch (JsonParsingException e) {
            ; // expected
        }
    }

}
