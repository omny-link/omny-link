package com.knowprocess.xslt;

import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.activiti.engine.impl.el.FixedValue;

public class TransformTask implements JavaDelegate {
    private static final int MAX_VAR_LENGTH = 4000;
    private String xsltResource;
    private FixedValue xsltParamsField;
    private FixedValue xsltField;
    private FixedValue outputField;

    public void setXsltResource(String xsltResource) {
        this.xsltResource = xsltResource;
    }

    public String transform(String xml) {
        return transform(xml, new HashMap<String, String>());
    }

    public String transform(String xml, Map<String, String> params) {
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
            for (Entry<String, String> entry : params.entrySet()) {
                t.setParameter(entry.getKey(), entry.getValue());
            }
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
            } catch (Exception e) {
                ;
            }
        }
        System.out.println("transform result: " + out.getBuffer().toString());
        return out.getBuffer().toString();
    }

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        Object tmp = execution.getVariable("resource");
        String resource = null;
        if (tmp instanceof String) {
            resource = (String) tmp;
        } else if (tmp instanceof byte[]) {
            resource = new String((byte[]) tmp, "UTF-8");
        } else {
            throw new ActivitiException("Unhandled type for var: "
                    + tmp.getClass());
        }

        Map<String, String> params = new HashMap<String, String>();
        String[] xsltParamNames = xsltParamsField == null ? new String[0]
                : xsltParamsField.getExpressionText().split(
                ",");

        for (String paramName : xsltParamNames) {
            Object param = execution.getVariable(paramName);
            if (param != null && param instanceof String) {
                params.put(paramName, (String) param);
            }
        }

        setXsltResource(xsltField.getExpressionText());
        String outputVarName = outputField == null ? "resource" : outputField
                .getExpressionText();
        String result = transform(resource, params);
        System.out.println("result: " + result);
        // TODO GARBAGE! Cant do this in every case !
        String[] msgs = result.split("\n");
        List<String> errors = new ArrayList<String>();
        List<String> messages = new ArrayList<String>();
        for (String msg : msgs) {
            if (msg.trim().startsWith("ERROR")) {
                errors.add(msg.trim());
            } else if (msg.trim().startsWith("INFO")) {
                messages.add(msg.trim());
            }
        }
        // if (result.length() > MAX_VAR_LENGTH) {
        // // we have a problem, cannot store in the standard Activiti DB
        //
        // String msg = "Resource is too large (" + result.length()
        // + " bytes) to store as a process variable: " + resource;
        // System.out.println(msg);
        // // throw new ActivitiException(msg);
        // }
        execution.setVariable(outputVarName, result.getBytes());
        execution.setVariable("errors", errors);
        execution.setVariable("messages", messages);
    }

}
