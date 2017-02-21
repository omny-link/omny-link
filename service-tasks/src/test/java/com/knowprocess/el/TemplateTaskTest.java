package com.knowprocess.el;

import static org.junit.Assert.assertTrue;

import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

import org.junit.Test;

public class TemplateTaskTest {

    String template ="Hi ${contact}\\n\\n"
            + "Thank you for contacting Omny Link, we'll get back to you shortly.\\n\\n"
            + "Best,\\n${owner.firstName.string}\\n"
            + "tel:${owner.phoneNumbers[0].string}";

    @Test
    public void test() throws Exception {
        TemplateTask task = new TemplateTask();
        
        Map<String,Object> params = new HashMap<String, Object>();
        params.put("contact", "Jill");
        params.put("owner", getAccountManager());
        String result = task.evaluateTemplate(template, params);
        assertTrue(result.contains("Hi Jill"));
        assertTrue(result.contains("Best,\\nJack"));
        assertTrue(result.contains("tel:011-111-1111"));
    }
    
    private JsonObject getAccountManager() {
        String personJSONData = 
                "{"
                + "  \"firstName\": \"Jack\", "
                + "  \"phoneNumbers\": [\"011-111-1111\", \"11-111-1111\"] "
                + "}";
             
        JsonReader reader = Json.createReader(new StringReader(personJSONData));
        JsonObject personObject = reader.readObject();
         
        reader.close();
        return personObject;
    }
}
