package com.knowprocess.bpm.web;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.knowprocess.bpm.model.ProcessDefinition;

public class Md5HashUtilsTest {

    @Test
    public void testIsIdentical() {
        assertTrue(Md5HashUtils.isIdentical("foo", "foo"));
        assertTrue(!Md5HashUtils.isIdentical("foo", "bar"));
    }

    @Test
    public void testIsBpmnIdentical() {
        String bpmn = ProcessDefinition
                .readFromClasspath("/processes/NoOp.bpmn");
        assertTrue(Md5HashUtils.isIdentical(bpmn, bpmn));
        assertTrue(!Md5HashUtils.isIdentical("foo", bpmn));
    }


}
