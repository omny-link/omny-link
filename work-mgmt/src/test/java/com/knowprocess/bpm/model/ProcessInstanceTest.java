package com.knowprocess.bpm.model;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ProcessInstanceTest {

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void test() throws JsonGenerationException, JsonMappingException,
            IOException {
        ProcessInstance pi = new ProcessInstance();
        pi.setBusinessKey("Tim Stephenson");
        Map<String, Object> vars = new HashMap<String, Object>();
        vars.put("contactId", "http://localhost/contacts/1");
        pi.setProcessVariables(vars);

        ObjectMapper om = new ObjectMapper();
        StringWriter sw = new StringWriter();
        om.writeValue(sw, pi);
        System.out.println(sw.toString());
    }

}
