package com.knowprocess.deployment;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import com.knowprocess.deployment.ValidationError.ValidationLevel;
import com.knowprocess.xslt.TransformTask;

/**
 * Validates suitability of BPMN process definition for execution in engine.
 * 
 * @author tim.stephenson@knowprocess.com
 */
public class Validator implements JavaDelegate {

    public List<ValidationError> validate(String bpmnDefinition) {
        // InputStream is = null ;
        InputStream bpsimStream = null;
        // InputStream bpmnStream = null;
        // InputStream bpmndiStream = null;
        // InputStream dcStream = null;
        // InputStream diStream = null;
        // InputStream semanticStream = null;

        ReportingErrorHandler errHandler = new ReportingErrorHandler();
        try {
            // is = Validator.class
            // .getResourceAsStream(bpmnDefinition);
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setValidating(false);
            factory.setNamespaceAware(true);

            SchemaFactory schemaFactory = SchemaFactory
                    .newInstance("http://www.w3.org/2001/XMLSchema");
            // schemaFactory.setResourceResolver(new ResourceResolver());

            bpsimStream = getClass().getResourceAsStream("/xsd/bpsim-1.0.xsd");
            // bpmnStream = getClass().getResourceAsStream("/xsd/BPMN20.xsd");
            // bpmnStream = getClass().getResourceAsStream(
            // "/xsd/BPMN20-all-in-one.xsd");
            // bpmndiStream = getClass().getResourceAsStream("/xsd/BPMNDI.xsd");
            // dcStream = getClass().getResourceAsStream("/xsd/DC.xsd");
            // diStream = getClass().getResourceAsStream("/xsd/DI.xsd");
            // semanticStream = getClass()
            // .getResourceAsStream("/xsd/Semantic.xsd");
            factory.setSchema(schemaFactory.newSchema(new Source[] {
                    new StreamSource(new InputStreamReader(bpsimStream)),
                    // new StreamSource(new InputStreamReader(bpmnStream)),
            // new StreamSource(new InputStreamReader(bpmndiStream)),
            // new StreamSource(new InputStreamReader(diStream)),
            // new StreamSource(new InputStreamReader(dcStream)),
            // new StreamSource(new InputStreamReader(semanticStream))
                    }));

            // TODO convert this to DOM parser in order to feed through chain of
            // validators of different kinds?
            SAXParser parser = factory.newSAXParser();
            XMLReader reader = parser.getXMLReader();

            reader.setErrorHandler(errHandler);
            reader.parse(new InputSource(new StringReader(bpmnDefinition)));
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
        }

        return errHandler.getErrors();
    }

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        String resource = (String) execution
              .getVariable("resource"); 
        // TODO issues with imported schemas
//        List<ValidationError> errors = validate(resource);
        TransformTask transform = new TransformTask();
        transform.setXsltResource("/xslt/ActivitiSupportRules.xsl");
        List<ValidationError> errors = clean(transform.transform(resource
                .trim()));
        
        execution.setVariable("errors", errors);
    }

    private List<ValidationError> clean(String transform) {
        List<ValidationError> errors = new ArrayList<ValidationError>();
        String[] messages = transform.split("\\n", 0);
        System.out.println("messages found: " + messages.length);
        // List<String> ignored = new ArrayList<String>();
        // List<String> passed = new ArrayList<String>();
        // List<String> errors = new ArrayList<String>();
        for (String string : messages) {
            String msg = string.trim();
            if (msg.length() > 0 && msg.toUpperCase().startsWith("ERROR")) {
                errors.add(new ValidationError(ValidationLevel.ERROR, msg));
            } else if (msg.length() > 0 && msg.toUpperCase().startsWith("PASS")) {
                errors.add(new ValidationError(ValidationLevel.INFO, msg));
            } else if (msg.length() > 0
                    && msg.toUpperCase().startsWith("IGNORED")) {
                errors.add(new ValidationError(ValidationLevel.DEBUG, msg));
            }
        }
        return errors;
    }
}
