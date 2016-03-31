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
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import com.knowprocess.bpm.Application;
import com.knowprocess.bpm.model.ProcessModel;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
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
    public void testLifecycleOfIncompleteModel() {
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
        svc.deleteFromJson(model2.getId());
    }

    private String getResourceAsString(String resourceName) {
        InputStream is = null;
        try {
            is = getClass().getResourceAsStream(resourceName);
            return new Scanner(is).useDelimiter("\\A").next();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            try {
                is.close();
            } catch (Exception e) {
            }
        }
    }

}
