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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.tool.xml.Pipeline;
import com.itextpdf.tool.xml.XMLWorker;
import com.itextpdf.tool.xml.XMLWorkerHelper;
import com.itextpdf.tool.xml.exceptions.CssResolverException;
import com.itextpdf.tool.xml.html.Tags;
import com.itextpdf.tool.xml.parser.XMLParser;
import com.itextpdf.tool.xml.pipeline.css.CSSResolver;
import com.itextpdf.tool.xml.pipeline.css.CssResolverPipeline;
import com.itextpdf.tool.xml.pipeline.end.PdfWriterPipeline;
import com.itextpdf.tool.xml.pipeline.html.HtmlPipeline;
import com.itextpdf.tool.xml.pipeline.html.HtmlPipelineContext;
import com.itextpdf.tool.xml.pipeline.html.LinkProvider;

@Service
public class Html2PdfService {

    protected static final Logger LOGGER = LoggerFactory
            .getLogger(Html2PdfService.class);

    @Value("${kp.application.base-uri:https://api.knowprocess.com}")
    private String baseUrl;

    @Value("${kp.services.css:/css/base.css}")
    private final String cssResource;

    private String bootstrapCss;

    private String css;
    
    public Html2PdfService() {
        this.cssResource = "/css/base.css";
    }

    public Html2PdfService(String cssResource) {
        this.cssResource = cssResource;
    }

    public void execute(String htmlIn, OutputStream outputStream) {
        try {
            Document document = new Document();

            PdfWriter writer = PdfWriter.getInstance(document, outputStream);
            writer.setInitialLeading(12.5f);
            document.open();

            HtmlPipelineContext htmlContext = new HtmlPipelineContext(null);
            htmlContext.setTagFactory(Tags.getHtmlTagProcessorFactory());
            // htmlContext.setImageProvider(new AbstractImageProvider() {
            // public String getImageRootPath() {
            // return "images/";
            // }
            // });

            htmlContext.setLinkProvider(new LinkProvider() {
                public String getLinkRoot() {
                    return baseUrl;
                }
            });

            CSSResolver cssResolver = XMLWorkerHelper.getInstance()
                    .getDefaultCssResolver(false);
            try {
                cssResolver.addCss(getBootstrapCss(
                        "/META-INF/resources/webjars/bootstrap/3.4.1/css/bootstrap.min.css"),
                        true);
                cssResolver.addCss(getUserDefinedCss(), true);
            } catch (CssResolverException e) {
                LOGGER.warn("Cannot add CSS to PDF pipeline", e);
            }
            Pipeline<?> pipeline = new CssResolverPipeline(cssResolver,
                    new HtmlPipeline(htmlContext,
                            new PdfWriterPipeline(document, writer)));
            XMLWorker worker = new XMLWorker(pipeline, true);

            XMLParser p = new XMLParser(worker);
            htmlIn = htmlIn.replaceAll("<br>", "<br/>");
            StringBuilder sb = new StringBuilder().append("<html><head>")
                    .append("</head><body>")
                    .append(htmlIn)
                    .append("</body></html>")
                    .append(Character.LINE_SEPARATOR);
            p.parse(new StringReader(sb.toString()));

            document.close();
        } catch (NoClassDefFoundError e) {
            throw new IllegalStateException(
                    "PDF generation not currently enabled.");
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
            throw new IllegalStateException(e);
        } catch (DocumentException e) {
            LOGGER.error(e.getMessage(), e);
            throw new IllegalStateException("Probably a template problem.");
        }
    }

    private String getBootstrapCss(String resource) {
        if (bootstrapCss == null) {
            bootstrapCss = getClasspathResource(resource);
        }
        return bootstrapCss;
    }

    private String getUserDefinedCss() {
        if (css == null && cssResource != null) {
            css = getClasspathResource(cssResource);
        }
        return css;
    }

    @SuppressWarnings("resource")
    protected String getClasspathResource(String resource) {
        InputStream is = null;
        try {
            is = getClass().getResourceAsStream(resource);
            return new Scanner(is).useDelimiter("\\A").next();
        } catch (Exception e) {
            LOGGER.warn(String.format("Unable to read resource from %1$s",
                    resource));
        } finally {
            try {
                is.close();
            } catch (Exception e) {
                ;
            }
        }
        return "";
    }
}
