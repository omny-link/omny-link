/*******************************************************************************
 *Copyright 2018 Tim Stephenson and contributors
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
package com.knowprocess.opml.internal;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.knowprocess.opml.api.OpmlFeed;
import com.knowprocess.opml.api.OpmlInput;
import com.knowprocess.resource.internal.UrlResource;

/**
 * Service class for parsing an OPML feed into a model bean.
 * 
 * @author tstephen
 */
public class OpmlInputImpl extends DefaultHandler implements OpmlInput,
        JavaDelegate {

    private OpmlFeed root;

    /**
     * @see com.knowprocess.opml.api.OpmlInput#build(java.io.InputStream)
     */
    @Override
    public OpmlFeed build(InputStream is) {
        root = new OpmlFeed();
        SAXParserFactory parserFactory = SAXParserFactory.newInstance();
        try {
            SAXParser parser = parserFactory.newSAXParser();
            parser.parse(is, this);
        } catch (ParserConfigurationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SAXException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return root;
    }

    @Override
    public void characters(char[] ch, int start, int length)
            throws SAXException {
        // TODO Auto-generated method stub
        super.characters(ch, start, length);
    }

    @Override
    public void endElement(String uri, String localName, String qName)
            throws SAXException {
        // TODO Auto-generated method stub
        super.endElement(uri, localName, qName);
    }

    @Override
    public void startElement(String uri, String localName, String qName,
            Attributes attributes) throws SAXException {
        // TODO Auto-generated method stub
        super.startElement(uri, localName, qName, attributes);
        if ("outline".equals(qName)) {
            root.addChild(new OpmlFeed(attributes.getValue("title"), attributes
                    .getValue("text"), attributes.getValue("type"), attributes
                    .getValue("xmlUrl"), attributes.getValue("htmlUrl")));
        }
    }

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        String opmlUrl = (String) execution.getVariable("opmlUrl");
        InputStream is = null;
        try {
            is = new UrlResource().getResource(opmlUrl);
            execution.setVariable("opml", build(is));
            System.out.println("opml: " + execution.getVariable("opml"));
        } catch (Exception e) {
            // TODO
            e.printStackTrace();
        }
    }

}
