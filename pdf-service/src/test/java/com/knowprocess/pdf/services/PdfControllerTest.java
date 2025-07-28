/*******************************************************************************
 * Copyright 2015-2025 Tim Stephenson and contributors
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.UnsupportedEncodingException;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;

import com.knowprocess.pdf.PdfTestApplication;
import com.knowprocess.pdf.web.PdfController;

/**
 * @author Tim Stephenson
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = PdfTestApplication.class)
@WebAppConfiguration
public class PdfControllerTest {

    private static String htmlIn;

    @Autowired
    private PdfController svc;

    @BeforeAll
    public static void setUpClass() {
        Html2PdfService svc = new Html2PdfService();
        htmlIn = svc.getClasspathResource("/docs/testHtml2Pdf.html");        
    }
    
    @Test
    public void testHtml2Pdf() throws UnsupportedEncodingException {
        ResponseEntity<byte[]> entity = svc.transformToPdf("test.pdf", htmlIn);
        assertEquals(HttpStatus.OK, entity.getStatusCode());
        byte[] pdf = entity.getBody();
        assertNotNull(pdf);
    }

}
