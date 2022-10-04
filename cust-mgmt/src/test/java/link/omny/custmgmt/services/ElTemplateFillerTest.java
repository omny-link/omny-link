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

    // NOTE: intValue to convert long to int per
    // https://stackoverflow.com/questions/28839814/unable-to-find-unambiguous-method-class-com-fasterxml-jackson-databind-node-arr
    // (needed with flowable embedded vsn of EL)
    private static final String CRLF = System.getProperty("line.separator");
    String template = String.format("Hi ${contact}%1$s"
            + "Thank you for contacting Omny Link at "
            + "${dateFormatter.toString(now,'dd-MMM-yyyy HH:mm')}, "
            + "we'll get back to you shortly.%2$s%3$s"
            + "Best,${owner.findPath('firstName').textValue()}%4$s"
            + "tel:${owner.findPath('phoneNumbers').get( (0).intValue() ).textValue()}", CRLF, CRLF, CRLF, CRLF);

    private static ObjectMapper objectMapper;

    @BeforeAll
    public static void beforeAll() {
        objectMapper = new ObjectMapper();
    }

    @Test
    public void testEval() throws Exception {
        ElTemplateFiller svc = new ElTemplateFiller();

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("contact", "Jill");
        params.put("owner", getAccountManager());
        String result = svc.evaluateTemplate(template, params).replaceAll(CRLF, "");
        System.out.println("Result was: "+result);
        // replace ${contact}
        assertTrue(result.contains("Hi Jill"));
        // replace with formatted date time
        assertTrue(result.matches(".*at \\d{2}-[a-zA-Z]{3}-\\d{4} \\d{2}:\\d{2}.*"));
        // replace with owner.firstName
        assertTrue(result.contains("Best,Jack"));
        // replace with owner's first phone number
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
