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
	private static final TransformerFactory factory = TransformerFactory
			.newInstance();
	public static final String ERROR_KEY = "ERROR";
	public static final String IGNORED_KEY = "IGNORED";
	public static final String PASS_KEY = "PASS";
    private FixedValue xsltParamsField;
    private FixedValue xsltField;
    private FixedValue outputField;
	private Transformer[] transformers;

	public TransformTask() {
		super();
		factory.setURIResolver(new ClasspathResourceResolver());
	}

	public void setXsltResource(String xsltResource)
			throws TransformerConfigurationException {
		String[] resources = xsltResource.split(",");
		transformers = new Transformer[resources.length];
		for (int i = 0 ; i < resources.length ; i++) {
			InputStream is = null;
			try {
				is = getClass().getResourceAsStream(resources[i]);
				transformers[i] = factory.newTransformer(new StreamSource(is));
			} finally {
				try {
					is.close();
				} catch (Exception e) {
					;
				}
			}
		}
    }

    public String transform(String xml) {
        return transform(xml, new HashMap<String, String>());
    }

    public String transform(String xml, Map<String, String> params) {
        try {
			for (Transformer t : transformers) {
				xml = transformOnce(t, xml, params);
			}
        } catch (TransformerConfigurationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (TransformerException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.out.println("transform result: " + xml);
        return xml;
    }

	private String transformOnce(Transformer t, String xml,
			Map<String, String> params) throws TransformerException {
    	StringWriter out = new StringWriter();
    	Result outputTarget = new StreamResult(out);
        Source xmlSource = new StreamSource(new StringReader(xml.trim()));

		for (Entry<String, String> entry : params.entrySet()) {
			t.setParameter(entry.getKey(), entry.getValue());
		}
		t.transform(xmlSource, outputTarget);
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
