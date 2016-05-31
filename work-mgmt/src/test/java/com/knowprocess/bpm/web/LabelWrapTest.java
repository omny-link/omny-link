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
