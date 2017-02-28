package com.knowprocess.el;

import static org.junit.Assert.assertEquals;

import java.util.Date;

import org.junit.BeforeClass;
import org.junit.Test;

public class DateFormatterTest {

    private static DateFormatter dateFormatter;

    @BeforeClass
    public static void setUpClass() {
        dateFormatter = new DateFormatter();
    }
    
    @Test
    public void testNullDate() {
        assertEquals("dd-mm-yyyy", dateFormatter.toString((Date) null, "dd-MM-yyyy"));
        assertEquals("dd/mm/yyyy", dateFormatter.toString((Date) null, "dd/MM/yyyy"));
        assertEquals("mm/dd/yyyy", dateFormatter.toString((Date) null, "MM/dd/yyyy"));
    }

    @Test
    public void testReformatDate() {
        assertEquals("31-12-2016", dateFormatter.toString("2016-12-31", "dd-MM-yyyy"));
    }
    
}
