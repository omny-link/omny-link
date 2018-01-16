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
