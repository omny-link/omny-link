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
package com.knowprocess.bpmn;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.knowprocess.xslt.TransformTask;

public class BpmnHelper {

	public void splitCollaboration(InputStream collab, File outputDir) {
		try {
			TransformTask svc = new TransformTask();
			svc.setXsltResources("/xslt/ExecutableTweaker.xsl");

			DocumentBuilderFactory domFactory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder domBuilder = domFactory.newDocumentBuilder();
			Document collabDom = domBuilder.parse(collab);

			XPathFactory factory = XPathFactory.newInstance();
			XPath xPath = factory.newXPath();

			Node collabNode = ((NodeList) xPath.evaluate("//collaboration",
					collabDom, XPathConstants.NODESET)).item(0);
			if (collabNode == null) {
				collabNode = (Node) xPath.evaluate("//semantic:collaboration",
						collabDom, XPathConstants.NODE);
			}
			NodeList childNodes = collabNode.getChildNodes();
			Map<String, String> params = new HashMap<String, String>();

			for (int i = 0; i < childNodes.getLength(); i++) {
				Node child = childNodes.item(i);
				writeProcess(outputDir, svc, collabDom, params, child);
			}
		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformerConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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

	}

	private void writeProcess(File outputDir, TransformTask svc,
			Document collabDom, Map<String, String> params, Node child) {
		if (child.getNodeType() == Node.ELEMENT_NODE
				&& child.getNodeName().equals("participant")) {
			System.out.println("Found process participant named: "
					+ child.getAttributes().getNamedItem("name"));
			params.clear();
			params.put("processParticipantToExecute", child.getAttributes()
					.getNamedItem("name").getNodeValue());
			String xml = svc.transform(
					new DOMSource(collabDom.getDocumentElement()), params);

			PrintWriter out = null;
			try {
				File outputFile = new File(outputDir, child.getAttributes()
						.getNamedItem("name").getNodeValue().replace(' ', '_')
						+ ".bpmn");
				System.out.println("Writing to " + outputFile.getName());
				out = new PrintWriter(outputFile);
				out.println(xml);
				System.out.println("... ok");
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.err.println("... unsuccessful: " + e.getMessage());
			} finally {
				out.close();
			}
		}
	}
}
