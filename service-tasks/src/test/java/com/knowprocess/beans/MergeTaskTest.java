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
package com.knowprocess.beans;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import org.junit.Test;

import com.knowprocess.core.model.ProcessMetaData;

public class MergeTaskTest {

    @Test
    public void testMerge() {
        ProcessMetaData srcBean = new ProcessMetaData();
        ProcessMetaData tBean = new ProcessMetaData();
        assertNotNull(srcBean.getStart());
        MergeTask svc = new MergeTask();
        try {
            svc.merge(srcBean, tBean);
            assertNotNull(tBean.getStart());
            assertEquals(srcBean.getStart(), tBean.getStart());
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

}
