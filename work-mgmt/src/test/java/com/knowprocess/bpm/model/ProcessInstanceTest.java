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
