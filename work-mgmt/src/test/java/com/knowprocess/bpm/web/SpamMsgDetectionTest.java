package com.knowprocess.bpm.web;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class SpamMsgDetectionTest {

    protected MessageController svc = new MessageController();

    @Test
    public void isEmptyJsonTest() {
        String json = "{ \"id\": \"\" }";
        assertTrue(svc.isEmptyJson(json));
    }

    @Test
    public void isNotEmptyJsonTest() {
        String json = "{ \"id\": \"abc123\" }";
        assertTrue(!svc.isEmptyJson(json));
    }

    @Test
    public void isEmptyJsonArrayTest() {
        String json = "[]";
        assertTrue(svc.isEmptyJson(json));
    }

    @Test
    public void isNotEmptyJsonArrayTest() {
        String json = "[{ \"id\": \"abc123\" }]";
        assertTrue(!svc.isEmptyJson(json));
    }
}
