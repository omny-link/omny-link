// WARNING: DO NOT EDIT THIS FILE. THIS FILE IS MANAGED BY SPRING ROO.
// You may push code into the target .java compilation unit if you wish to edit any member(s).

package org.activiti.spring.rest.model;

import java.util.Iterator;
import java.util.List;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import org.activiti.spring.rest.model.ProcessDefinition;
import org.activiti.spring.rest.model.ProcessDefinitionDataOnDemand;
import org.activiti.spring.rest.model.ProcessDefinitionIntegrationTest;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

privileged aspect ProcessDefinitionIntegrationTest_Roo_IntegrationTest {
    
    declare @type: ProcessDefinitionIntegrationTest: @RunWith(SpringJUnit4ClassRunner.class);
    
    declare @type: ProcessDefinitionIntegrationTest: @ContextConfiguration(locations = "classpath*:/META-INF/spring/applicationContext*.xml");
    
    declare @type: ProcessDefinitionIntegrationTest: @Transactional;
    
    @Autowired
    ProcessDefinitionDataOnDemand ProcessDefinitionIntegrationTest.dod;
    
    @Test
    public void ProcessDefinitionIntegrationTest.testCountProcessDefinitions() {
        Assert.assertNotNull("Data on demand for 'ProcessDefinition' failed to initialize correctly", dod.getRandomProcessDefinition());
        long count = ProcessDefinition.countProcessDefinitions();
        Assert.assertTrue("Counter for 'ProcessDefinition' incorrectly reported there were no entries", count > 0);
    }
    
    @Test
    public void ProcessDefinitionIntegrationTest.testFindProcessDefinition() {
        ProcessDefinition obj = dod.getRandomProcessDefinition();
        Assert.assertNotNull("Data on demand for 'ProcessDefinition' failed to initialize correctly", obj);
        String id = obj.getId();
        Assert.assertNotNull("Data on demand for 'ProcessDefinition' failed to provide an identifier", id);
        obj = ProcessDefinition.findProcessDefinition(id);
        Assert.assertNotNull("Find method for 'ProcessDefinition' illegally returned null for id '" + id + "'", obj);
        Assert.assertEquals("Find method for 'ProcessDefinition' returned the incorrect identifier", id, obj.getId());
    }
    
    @Test
    public void ProcessDefinitionIntegrationTest.testFindAllProcessDefinitions() {
        Assert.assertNotNull("Data on demand for 'ProcessDefinition' failed to initialize correctly", dod.getRandomProcessDefinition());
        long count = ProcessDefinition.countProcessDefinitions();
        Assert.assertTrue("Too expensive to perform a find all test for 'ProcessDefinition', as there are " + count + " entries; set the findAllMaximum to exceed this value or set findAll=false on the integration test annotation to disable the test", count < 250);
        List<ProcessDefinition> result = ProcessDefinition.findAllProcessDefinitions();
        Assert.assertNotNull("Find all method for 'ProcessDefinition' illegally returned null", result);
        Assert.assertTrue("Find all method for 'ProcessDefinition' failed to return any data", result.size() > 0);
    }
    
    @Test
    public void ProcessDefinitionIntegrationTest.testFindProcessDefinitionEntries() {
        Assert.assertNotNull("Data on demand for 'ProcessDefinition' failed to initialize correctly", dod.getRandomProcessDefinition());
        long count = ProcessDefinition.countProcessDefinitions();
        if (count > 20) count = 20;
        int firstResult = 0;
        int maxResults = (int) count;
        List<ProcessDefinition> result = ProcessDefinition.findProcessDefinitionEntries(firstResult, maxResults);
        Assert.assertNotNull("Find entries method for 'ProcessDefinition' illegally returned null", result);
        Assert.assertEquals("Find entries method for 'ProcessDefinition' returned an incorrect number of entries", count, result.size());
    }
    
    @Test
    public void ProcessDefinitionIntegrationTest.testFlush() {
        ProcessDefinition obj = dod.getRandomProcessDefinition();
        Assert.assertNotNull("Data on demand for 'ProcessDefinition' failed to initialize correctly", obj);
        String id = obj.getId();
        Assert.assertNotNull("Data on demand for 'ProcessDefinition' failed to provide an identifier", id);
        obj = ProcessDefinition.findProcessDefinition(id);
        Assert.assertNotNull("Find method for 'ProcessDefinition' illegally returned null for id '" + id + "'", obj);
        boolean modified =  dod.modifyProcessDefinition(obj);
        Integer currentVersion = obj.getVersion_();
        obj.flush();
        Assert.assertTrue("Version for 'ProcessDefinition' failed to increment on flush directive", (currentVersion != null && obj.getVersion_() > currentVersion) || !modified);
    }
    
    @Test
    public void ProcessDefinitionIntegrationTest.testMergeUpdate() {
        ProcessDefinition obj = dod.getRandomProcessDefinition();
        Assert.assertNotNull("Data on demand for 'ProcessDefinition' failed to initialize correctly", obj);
        String id = obj.getId();
        Assert.assertNotNull("Data on demand for 'ProcessDefinition' failed to provide an identifier", id);
        obj = ProcessDefinition.findProcessDefinition(id);
        boolean modified =  dod.modifyProcessDefinition(obj);
        Integer currentVersion = obj.getVersion_();
        ProcessDefinition merged = obj.merge();
        obj.flush();
        Assert.assertEquals("Identifier of merged object not the same as identifier of original object", merged.getId(), id);
        Assert.assertTrue("Version for 'ProcessDefinition' failed to increment on merge and flush directive", (currentVersion != null && obj.getVersion_() > currentVersion) || !modified);
    }
    
    @Test
    public void ProcessDefinitionIntegrationTest.testPersist() {
        Assert.assertNotNull("Data on demand for 'ProcessDefinition' failed to initialize correctly", dod.getRandomProcessDefinition());
        ProcessDefinition obj = dod.getNewTransientProcessDefinition(Integer.MAX_VALUE);
        Assert.assertNotNull("Data on demand for 'ProcessDefinition' failed to provide a new transient entity", obj);
        Assert.assertNull("Expected 'ProcessDefinition' identifier to be null", obj.getId());
        try {
            obj.persist();
        } catch (final ConstraintViolationException e) {
            final StringBuilder msg = new StringBuilder();
            for (Iterator<ConstraintViolation<?>> iter = e.getConstraintViolations().iterator(); iter.hasNext();) {
                final ConstraintViolation<?> cv = iter.next();
                msg.append("[").append(cv.getRootBean().getClass().getName()).append(".").append(cv.getPropertyPath()).append(": ").append(cv.getMessage()).append(" (invalid value = ").append(cv.getInvalidValue()).append(")").append("]");
            }
            throw new IllegalStateException(msg.toString(), e);
        }
        obj.flush();
        Assert.assertNotNull("Expected 'ProcessDefinition' identifier to no longer be null", obj.getId());
    }
    
    @Test
    public void ProcessDefinitionIntegrationTest.testRemove() {
        ProcessDefinition obj = dod.getRandomProcessDefinition();
        Assert.assertNotNull("Data on demand for 'ProcessDefinition' failed to initialize correctly", obj);
        String id = obj.getId();
        Assert.assertNotNull("Data on demand for 'ProcessDefinition' failed to provide an identifier", id);
        obj = ProcessDefinition.findProcessDefinition(id);
        obj.remove();
        obj.flush();
        Assert.assertNull("Failed to remove 'ProcessDefinition' with identifier '" + id + "'", ProcessDefinition.findProcessDefinition(id));
    }
    
}
