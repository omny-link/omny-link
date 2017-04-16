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
