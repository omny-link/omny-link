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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Scanner;

import org.junit.BeforeClass;
import org.junit.Test;

import com.knowprocess.bpm.model.ProcessDefinition;

public class Svg2PngTest {

    private static ProcessDefinitionController svc;

    @BeforeClass
    public static void setUp() {
        svc = new ProcessDefinitionController();
    }
    
    @Test
    public void testSvgToPng() throws Exception {
        byte[] pngBytes = svc.svgToPng(readFromClasspath("/B.2.0.svg"));
        assertNotNull(pngBytes);
        FileOutputStream fos = null;
        try {
            File pngFile = new File("target", "B.2.0.png");
            fos = new FileOutputStream(pngFile);
            fos.write(pngBytes);
            assertTrue(pngFile.exists());
        } finally {
            fos.close();
        }
    }

    @SuppressWarnings("resource")
    public static String readFromClasspath(String resourceName) {
        InputStream is = null;
        try {
            is = ProcessDefinition.class.getResourceAsStream(resourceName);
            return new Scanner(is).useDelimiter("\\A").next();
        } catch (Exception e) {
            throw e;
        } finally { 
            try {
                is.close();
            } catch (Exception e) {
                ;
            }
        }
    }
    
}
