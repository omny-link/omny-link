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
