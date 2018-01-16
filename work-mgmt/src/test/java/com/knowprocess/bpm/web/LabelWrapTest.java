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

import static org.junit.Assert.assertEquals;

import org.junit.BeforeClass;
import org.junit.Test;

public class LabelWrapTest {

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @Test
    public void testCollapsedSubProcess1() {
        String text = "Collapsed Sub-Process 1 Multi-Instances";
        float height = 79.0f;
        float width = 97.0f;
        float x = 1084.0f;
        float y = 114.0f;
        float labelHeight = 51.204604687499994f;
        float labelWidth = 86.18773333333334f;
        float labelX = 1089.3333333333333f;
        float labelY = 127.77544311079544f;

        int rowsOfText = LabelUtil.rowCount(text, labelHeight, labelWidth);
        assertEquals(3, rowsOfText);
        String[] rows = LabelUtil.rows(text, labelHeight, labelWidth);
        for (String row : rows) {
            System.out.println("  " + row);
        }
        assertEquals("Collapsed", rows[0]);
        assertEquals("Sub-Process 1", rows[1]);
        assertEquals("Multi-Instances", rows[2]);
    }

    @Test
    public void testCallGlobalUserTask() {
        String text = "Call Activity calling a Global User Task";
        float labelHeight = 51.204604687499994f;
        float labelWidth = 86.60892388451433f;

        int rowsOfText = LabelUtil.rowCount(text, labelHeight, labelWidth);
        assertEquals(3, rowsOfText);
        String[] rows = LabelUtil.rows(text, labelHeight, labelWidth);
        for (String row : rows) {
            System.out.println("  " + row);
        }
        assertEquals("Call Activity", rows[0]);
        assertEquals("calling a", rows[1]);
        assertEquals("Global User Task", rows[2]);
    }
}
