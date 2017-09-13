package com.knowprocess.services.pdf;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileOutputStream;

import org.junit.Test;

public class Html2PdfServiceTest {

    @Test
    public void testHtml2PdfNoUserDefinedCss() {
        Html2PdfService svc = new Html2PdfService();
        String var = svc.getClasspathResource("/docs/testHtml2Pdf.html");
        File out = new File("target", "testHtml2PdfNoUserDefinedCss.pdf");
        try (FileOutputStream os = new FileOutputStream(out)) {
            svc.execute(var, os);
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        assertTrue("No PDF file found", out.exists());
    }

    @Test
    public void testHtml2PdfWithUserDefinedCss() {
        Html2PdfService svc = new Html2PdfService("/css/user.css");
        String var = svc.getClasspathResource("/docs/testHtml2Pdf.html");
        File out = new File("target", "testHtml2PdfWithUserDefinedCss.pdf");
        try (FileOutputStream os = new FileOutputStream(out)) {
            svc.execute(var, os);
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        assertTrue("No PDF file found", out.exists());
    }

}
