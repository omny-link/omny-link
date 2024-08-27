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

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PdfController {

    protected static final Logger LOGGER = LoggerFactory
            .getLogger(PdfController.class);

    @Autowired
    protected Html2PdfService html2PdfService;

    @PostMapping(value = "/pdfs/{fileName}.pdf", consumes = { MediaType.TEXT_HTML_VALUE }, 
            produces = MediaType.APPLICATION_PDF_VALUE)
    public final ResponseEntity<byte[]> transformToPdf(
            @PathVariable("fileName") String fileName,
            @RequestBody String htmlIn) throws UnsupportedEncodingException {
        htmlIn = URLDecoder.decode(htmlIn, "UTF-8");
        LOGGER.info("transformToPdf to {}, received {}...", fileName,
                (htmlIn != null && htmlIn.length() > 50 ? htmlIn.substring(0, 50) : htmlIn));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        html2PdfService.execute(htmlIn, baos);
        
        return new ResponseEntity<byte[]>(baos.toByteArray(), HttpStatus.OK);
    }

}
