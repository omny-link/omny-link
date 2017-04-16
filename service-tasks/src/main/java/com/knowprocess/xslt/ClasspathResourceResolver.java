package com.knowprocess.xslt;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClasspathResourceResolver implements URIResolver {
    protected static final Logger LOGGER = LoggerFactory
            .getLogger(ClasspathResourceResolver.class);

    @Override
    public Source resolve(String href, String base) throws TransformerException {
        LOGGER.info("Attempt to resolve {} from classpath", href);
        Source s = null;
        try {
            s = new StreamSource(getClass().getResourceAsStream(href));
        } catch (Exception e) {
            LOGGER.error("Unable to resolve resource {} from classpath", href);
        }
        return s;
    }

}
