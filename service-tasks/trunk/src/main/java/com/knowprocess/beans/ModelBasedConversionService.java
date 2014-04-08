package com.knowprocess.beans;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.support.ConfigurableConversionService;
import org.springframework.core.convert.support.GenericConversionService;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class ModelBasedConversionService extends GenericConversionService
        implements ConfigurableConversionService {

    protected static final Logger LOGGER = LoggerFactory
            .getLogger(ModelBasedConversionService.class);

    private XPathExpression attrMappingExpr;

    protected XPathFactory xFactory = XPathFactory.newInstance();

    protected XPath xPath = xFactory.newXPath();

    private XPathExpression entityExpr;

    private XPathExpression entityNameExpr;

    private XPathExpression trgtNameExpr;

    private XPathExpression mappingExpr;

    private Map<String, Class<?>> mappings;

    public ModelBasedConversionService() {
        super();
        mappings = new HashMap<String, Class<?>>();
    }

    public ModelBasedConversionService(String modelResource, String domain,
            String srcPkg, String trgtPkg) {
        this();
        init(modelResource, domain, srcPkg, trgtPkg);
    }

    protected void init(String modelResource, String domain, String srcPkg,
            String trgtPkg) {
        try {
            Document document = parseModel(modelResource, domain);

            NodeList entities = (NodeList) getEntityExpr().evaluate(document,
                    XPathConstants.NODESET);

            for (int i = 0; i < entities.getLength(); i++) {
                Node entity = entities.item(i);
                String srcName = getEntityNameExpr().evaluate(entity);
                String trgtName = getTrgtNameExpr(domain).evaluate(entity);
                if (trgtName.contains("»")) {
                    trgtName = trgtName.substring(trgtName.indexOf(".") + 1,
                            trgtName.indexOf('»'));
                    try {
                        Class<?> srcType = getClass().getClassLoader()
                                .loadClass(srcPkg + "." + srcName);
                        Class<?> trgtType = getClass().getClassLoader()
                                .loadClass(trgtPkg + "." + trgtName);

                        // init a converter with the JS script and add to this
                        // conversion service
                        LOGGER.info(String.format(
                                "Registering converter from %1$s to %2$s.",
                                srcName, trgtName));
                        JSConverter converter = new JSConverter(srcType,
                                trgtType, createScript(domain, srcPkg, srcName,
                                        trgtPkg, trgtName,
                                        (NodeList) getMappingExpr().evaluate(
                                                entity,
                                                XPathConstants.NODESET)));
                        addConverter(srcType, trgtType, converter);
                        mappings.put(srcType.getName(), trgtType);
                    } catch (ClassNotFoundException e) {
                        // if (LOGGER.isDebugEnabled()) {
                        // LOGGER.error(e.getMessage(), e);
                        // }
                        LOGGER.warn(String
                                .format("No class found for one or both of source %1$s or target %2$s defined in model %3$s",
                                        srcName, trgtName, modelResource));
                    }
                }
            }
        } catch (XPathExpressionException e) {
            String msg = String.format(
                    "Unable to create converters for %1$s from %2$s", domain,
                    modelResource);
            LOGGER.error(msg, e);
            throw new IllegalArgumentException(msg, e);
        }

    }

    private XPathExpression getAttrMappingExpr()
            throws XPathExpressionException {
        if (attrMappingExpr == null) {
            attrMappingExpr = xPath.compile("@value");
        }
        return attrMappingExpr;
    }

    private XPathExpression getMappingExpr() throws XPathExpressionException {
        if (mappingExpr == null) {
            mappingExpr = xPath.compile("superitem[@id='attributes']/item");
        }
        return mappingExpr;
    }

    private XPathExpression getTrgtNameExpr(String domain)
            throws XPathExpressionException {
        if (trgtNameExpr == null) {
            trgtNameExpr = xPath.compile("superitem/item[starts-with(@value,'«"
                    + domain + "')]/@value");
        }
        return trgtNameExpr;
    }

    /**
     * @return Expression to find entity name
     *         <em>relative to entity element itself</em>.
     * @throws XPathExpressionException
     */
    private XPathExpression getEntityNameExpr() throws XPathExpressionException {
        if (entityNameExpr == null) {
            entityNameExpr = xPath.compile("item[@id='name']/@value");
        }
        return entityNameExpr;
    }

    private XPathExpression getEntityExpr() throws XPathExpressionException {
        if (entityExpr == null) {
            entityExpr = xPath
                    .compile("//UMLClass[superitem/item/@value='«entity»']");
        }
        return entityExpr;
    }

    // protected Class<?> getTargetType(String modelResource, String domain,
    // String trgtPkg) {
    // Document document = parseModel(modelResource, domain);
    // Class trgtType = getTargetType(modelResource, domain, t);
    // getClass().getClassLoader().loadClass(trgtPkg+"."+)
    // }

    private Document parseModel(String modelResource, String domain) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        InputStream is = null;
        try {
            is = getClass().getResourceAsStream(modelResource);
            return factory.newDocumentBuilder().parse(is);
        } catch (SAXException | IOException | ParserConfigurationException e) {
            String msg = String.format(
                    "Unable to read model for %1$s from %2$s", domain,
                    modelResource);
            LOGGER.error(msg, e);
            throw new IllegalArgumentException(msg, e);
        } finally {
            try {
                is.close();
            } catch (Exception e) {
                ;
            }
        }
    }

    private String createScript(String domain, String srcPkg, String srcType,
            String trgtPkg, String trgtType, NodeList attributes)
            throws XPathExpressionException {
        // LOGGER.debug("Define conversion script from " + attributes);
        String startMarker = "«" + domain + ".";

        StringBuilder sb = new StringBuilder();
        sb.append("importPackage(Packages." + srcPkg + ");\n");
        sb.append("importPackage(Packages." + trgtPkg + ");\n");
        sb.append("var o = new ").append(trgtType).append("();\n");
        for (int i = 0; i < attributes.getLength(); i++) {
            String val = getAttrMappingExpr().evaluate(attributes.item(i));
            if (val.contains(startMarker) && val.contains("»")) {
                String value = val.substring(val.indexOf(startMarker)
                        + startMarker.length(), val.indexOf('»'));
                String key = val.substring(val.indexOf('+') + 1,
                        val.indexOf(':'));
                sb.append("o.setCustom !== undefined ? o.setCustom('")
                        .append(value).append("', src.").append(key)
                        .append("): o.")
                        .append(value).append(" = src.")
                        .append(key).append(";\n");
            } else {
                LOGGER.warn("No mapping found for: " + val);
            }
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(" script created:\n" + sb.toString());
        }
        return sb.toString();
    }

    protected Object convert(Object value) {
        if (value == null) {
            return null;
        } else {
            return convert(value, mappings.get(value.getClass().getName()));
        }
    }

}
