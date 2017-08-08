package com.knowprocess.bpmn;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class BpmnRestHelperTest {

    @Test
    public void testTenantUri() {
        String tenantUri = new BpmnRestHelper().tenantUri("knowprocess", "http://api.knowprocess.com/contacts/123");
        assertEquals("http://api.knowprocess.com/knowprocess/contacts/123", tenantUri);
    }

    @Test
    public void testUriToDbId() {
        Long id = new BpmnRestHelper().uriToLocalId("http://api.knowprocess.com/contacts/123");
        assertEquals(new Long("123"), id);
    }
}
