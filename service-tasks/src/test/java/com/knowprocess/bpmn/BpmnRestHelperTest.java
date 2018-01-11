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
