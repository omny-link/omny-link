/*******************************************************************************
 * Copyright 2011-2022 Tim Stephenson and contributors
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
package link.omny.custmgmt.services;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ElTemplateFillerTest {

    String template = "Hi ${contact}\\\\n\\\\n"
            + "Thank you for contacting Omny Link, we'll get back to you shortly.\\\\n\\\\n"
            + "Best,\\\\n${owner.findPath('firstName').textValue()}\\\\n"
            + "tel:${owner.findPath('phoneNumbers').get(0).textValue()}";

    private static ObjectMapper objectMapper;

    @BeforeAll
    public static void beforeAll() {
        objectMapper = new ObjectMapper();
    }

    @Test
    public void test() throws Exception {
        ElTemplateFiller svc = new ElTemplateFiller();

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("contact", "Jill");
        params.put("owner", getAccountManager());
        String result = svc.evaluateTemplate(template, params);
        System.err.println("Result was: "+result);
        assertTrue(result.contains("Hi Jill"));
        assertTrue(result.contains("Best,\\nJack"));
        assertTrue(result.contains("tel:011-111-1111"));
    }

    private JsonNode getAccountManager()
            throws JsonMappingException, JsonProcessingException {
        String personJsonData = "{"
                + "  \"firstName\": \"Jack\", "
                + "  \"phoneNumbers\": [\"011-111-1111\", \"11-111-1111\"] "
                + "}";

        JsonNode node = objectMapper.readTree(personJsonData);
        return node;
    }
}
