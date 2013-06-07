package com.knowprocess.xslt;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.activiti.engine.impl.el.FixedValue;

public class TransformTask implements JavaDelegate {


    private String xsltResource;
    private FixedValue xsltField;
    private FixedValue outputField;

    public void setXsltResource(String xsltResource) {
        this.xsltResource = xsltResource;
    }

    public String transform(String xml) {
        TransformerFactory factory = TransformerFactory.newInstance();
        factory.setURIResolver(new ClasspathResourceResolver());
        InputStream xsltStream = null;
        StringWriter out = new StringWriter();
        try {
            xsltStream = getClass().getResourceAsStream(xsltResource);
            System.out.println("xsl: " + xsltStream);
            Source xsltSource = new StreamSource(xsltStream);
            Result outputTarget = new StreamResult(out);
            Source xmlSource = new StreamSource(new StringReader(xml.trim()));
            Transformer t = factory.newTransformer(xsltSource);
            t.transform(xmlSource, outputTarget);
        } catch (TransformerConfigurationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (TransformerException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            try {
                xsltStream.close();
            } catch (IOException e) {
                ;
            }
        }
        System.out.println("transform result: " + out.getBuffer().toString());
        return out.getBuffer().toString();
    }

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        String resource = (String) execution.getVariable("resource");
        setXsltResource(xsltField.getExpressionText());
        String outputVarName = outputField == null ? "resource" : outputField
                .getExpressionText();
        execution.setVariable(outputVarName,
                transform(resource));
    }

}
