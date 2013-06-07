package com.knowprocess.xslt;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;

public class ClasspathResourceResolver implements URIResolver {

    @Override
    public Source resolve(String href, String base) throws TransformerException {
        System.out.println("Attempt to resolve " + base + href);
        Source s = null;
        try {
            s = new StreamSource(getClass().getResourceAsStream(href));
        } catch (Exception e) {
            // TODO
            e.printStackTrace();
        }
        return s;
    }

}
