/*******************************************************************************
 *Copyright 2018 Tim Stephenson and contributors
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
package com.knowprocess.bpm.web;

import static org.junit.Assert.assertNotNull;

import java.io.InputStream;
import java.util.Date;
import java.util.Scanner;
import java.util.UUID;

import org.activiti.engine.ProcessEngine;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import com.knowprocess.bpm.Application;
import com.knowprocess.bpm.model.ProcessModel;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = Application.class)
@WebAppConfiguration
public class DeploymentControllerTest {

    private static final String RESOURCE_NAME = "loan-20160302.bpmn";

    @Autowired
    public ProcessEngine processEngine;

    @Autowired
    public DeploymentController svc;

    public void tearDown() {
    }

    @Test
    public void testLifecycleOfIncompleteModel() throws Exception {
        // CREATE
        ProcessModel model = new ProcessModel();
        model.setId(UUID.randomUUID().toString());
        model.setKey("testProcess");
        model.setName("test process");
        String bpmnString = getResourceAsString("/processes/" + RESOURCE_NAME);
        model.setBpmnString(bpmnString);
        model.setResourceName(RESOURCE_NAME);
        model.setCategory("test category");
        model.setCreated(new Date());
        model.setVersion(1);

        ProcessModel model2 = svc.createModel(model);
        assertNotNull(model2);
        assertNotNull(model2.getId());

        // RETRIEVE

        // UPDATE

        // DELETE
        svc.deleteFromJson(model2.getId(), false);
    }

    @SuppressWarnings("resource")
    private String getResourceAsString(String resourceName) throws Exception {
        try (InputStream is = getClass().getResourceAsStream(resourceName)) {
            return new Scanner(is).useDelimiter("\\A").next();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

}
