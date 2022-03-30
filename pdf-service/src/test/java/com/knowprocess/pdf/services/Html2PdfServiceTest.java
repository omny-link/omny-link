/*******************************************************************************
 *Copyright 2011-2022 Tim Stephenson and contributors
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
package com.knowprocess.pdf.services;


import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.FileOutputStream;

import org.junit.jupiter.api.Test;


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
        assertTrue(out.exists(), "No PDF file found");
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
        assertTrue(out.exists(), "No PDF file found");
    }

    @Test
    public void testHtml2BookingConfirmation() {
        Html2PdfService svc = new Html2PdfService();
        String var = svc.getClasspathResource("/docs/testBookingConfirmationv2.html");
        File out = new File("target", "testBookingConfirmationv2.pdf");
        try (FileOutputStream os = new FileOutputStream(out)) {
            svc.execute(var, os);
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        assertTrue(out.exists(), "No PDF file found");
    }
}
