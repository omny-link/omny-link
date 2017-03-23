package com.knowprocess.xslt;

import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Nonnull;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransformTask implements JavaDelegate {
	private static final TransformerFactory factory = TransformerFactory
			.newInstance();
    protected static final Logger LOGGER = LoggerFactory
            .getLogger(TransformTask.class);

	public static final String ERROR_KEY = "ERROR";
	public static final String IGNORED_KEY = "IGNORED";
	public static final String PASS_KEY = "PASS";
	private FixedValue xsltParamsField;
	private FixedValue xsltField;
	private FixedValue outputField;
	private Templates[] templates;

	public TransformTask() {
		super();
		factory.setURIResolver(new ClasspathResourceResolver());
	}

    /**
     * 
     * @param xsltResources
     *            Comma separated list of XSLT classpath resources.
     * @throws TransformerConfigurationException
     */
    public void setXsltResources(String xsltResources)
			throws TransformerConfigurationException {
        LOGGER.info(String.format("Setting up pre-processors %1$s",
                xsltResources));
        String[] resources = xsltResources.split(",");
		templates = new Templates[resources.length];
		for (int i = 0; i < resources.length; i++) {
			InputStream is = null;
			try {
				is = getClass().getResourceAsStream(resources[i]);
				templates[i] = factory.newTemplates(new StreamSource(is));
				assert (templates[i]!=null);
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

	public String transform(@Nonnull String xml, Map<String, String> params) {
		try {
			for (Templates t : templates) {
				Source xmlSource = new StreamSource(
						new StringReader(xml.trim()));
				xml = transformOnce(t.newTransformer(), xmlSource, params);
			}
		} catch (TransformerException e) {
		    throw new ActivitiException("Unable to perform XSLT transformation", e);
		}
		if (LOGGER.isDebugEnabled()) {
		    LOGGER.debug("transform result: " + xml);
		}
		return xml;
	}

	public String transform(Source xmlSource, Map<String, String> params) {
		String xml = null;
		try {
			for (Templates t : templates) {
				xml = transformOnce(t.newTransformer(), xmlSource, params);
				xmlSource = new StreamSource(new StringReader(xml.trim()));
			}
		} catch (TransformerException e) {
		    throw new ActivitiException("Unable to perform XSLT transformation", e);
		}
		if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("transform result: " + xml);
        }
		return xml;
	}

	private String transformOnce(Transformer t, Source xmlSource,
			Map<String, String> params) throws TransformerException {
		StringWriter out = new StringWriter();
		Result outputTarget = new StreamResult(out);

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
				: xsltParamsField.getExpressionText().split(",");

		for (String paramName : xsltParamNames) {
			Object param = execution.getVariable(paramName);
			if (param != null && param instanceof String) {
				params.put(paramName, (String) param);
			}
		}

		setXsltResources(xsltField.getExpressionText());
		String outputVarName = outputField == null ? "resource" : outputField
				.getExpressionText();
		String result = transform(resource, params);
		if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("transform result: " + result);
        }
		// TODO GARBAGE! Can't do this in every case !
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
		execution.setVariable(outputVarName, result.getBytes());
		execution.setVariable("errors", errors);
		execution.setVariable("messages", messages);
	}

	public Map<String, List<String>> parseResults(String result) {
		String[] messages = result.split("\\n", 0);
		LOGGER.info("messages found: %1$d", messages.length);
		Map<String, List<String>> map = new HashMap<String, List<String>>();
		List<String> ignored = new ArrayList<String>();
		List<String> passed = new ArrayList<String>();
		List<String> errors = new ArrayList<String>();
		map.put(ERROR_KEY, errors);
		map.put(IGNORED_KEY, ignored);
		map.put(PASS_KEY, passed);
		for (String string : messages) {
			String msg = string.trim();
			if (msg.length() > 0
					&& msg.toUpperCase().startsWith(TransformTask.ERROR_KEY)) {
				errors.add(msg);
			} else if (msg.length() > 0
					&& msg.toUpperCase().startsWith(TransformTask.PASS_KEY)) {
				passed.add(msg);
			} else if (msg.length() > 0
					&& msg.toUpperCase().startsWith(TransformTask.IGNORED_KEY)) {
				ignored.add(msg);
			}
		}
		LOGGER.info("Issues found: ERRORS: %1$d, IGNORED: %2$d, PASSED: %3$d", 
		        errors.size(), ignored.size(), passed.size());
		return map;
	}
}
