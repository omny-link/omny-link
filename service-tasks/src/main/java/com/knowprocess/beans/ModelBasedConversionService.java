package com.knowprocess.beans;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.converter.Converter;
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

    private XPathExpression entityMappingExpr;

    private XPathExpression assocStereotypeMappingExpr;

    protected XPathFactory xFactory = XPathFactory.newInstance();

    protected XPath xPath = xFactory.newXPath();

    private XPathExpression entityExpr;

    private XPathExpression entityNameExpr;

    private XPathExpression trgtNameExpr;

    private XPathExpression mappingExpr;

    public ModelBasedConversionService() {
        super();
    }

    public ModelBasedConversionService(String modelResource, String domain,
            String srcPkg, String trgtPkg) {
        this();
        init(modelResource, domain, srcPkg, trgtPkg);
    }

    protected void init(String modelResource, String domain, String srcPkg,
            String trgtPkg) {
        LOGGER.info(String.format(
                "Init conversion service for %1$s domain of model %2$s. "
                        + "\n  Source package: %3$s, "
                        + "\n  Target package: %4$s", domain, modelResource,
                srcPkg, trgtPkg));
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

                        String entityId = entity.getAttributes()
                                .getNamedItem("id").getNodeValue();
                        LOGGER.debug("Searching for associations to: "
                                + entityId);
                        String entityToProfileScript = createToProfileScript(
                                domain,
                                srcPkg,
                                srcName,
                                trgtPkg,
                                trgtName,
                                document,
                                (NodeList) getAttrMappingExpr().evaluate(
                                        entity, XPathConstants.NODESET),
                                (NodeList) getEntityMappingExpr(entityId)
                                        .evaluate(entity, XPathConstants.NODESET),
                                entityId);
                        addConverter(srcType, trgtType, entityToProfileScript);

                        String entityFromProfileScript = createFromProfileScript(
                                domain,
                                trgtPkg,
                                trgtName,
                                srcPkg,
                                srcName,
                                (NodeList) getAttrMappingExpr().evaluate(
                                        entity, XPathConstants.NODESET));
                        addConverter(trgtType, srcType, entityFromProfileScript);
                    } catch (ClassNotFoundException e) {
                        LOGGER.warn(String
                                .format("No class found for one or both of source %1$s or target %2$s defined in model %3$s, detail: %4$s.",
                                        srcName, trgtName, modelResource,
                                        e.getMessage()));
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

    private <S, T> void addConverter(Class<S> srcType, Class<T> trgtType,
            String entityToProfileScript) {
        LOGGER.info(String.format("Registering converter from %1$s to %2$s.",
                srcType.getName(), trgtType.getName()));

        @SuppressWarnings("unchecked")
        Converter<? super S, ? extends T> converter
                = (Converter<? super S, ? extends T>)
                new JSConverter(srcType, trgtType, entityToProfileScript);
        super.addConverter(srcType, trgtType, converter);
    }

    private XPathExpression getValueMappingExpr()
            throws XPathExpressionException {
        if (attrMappingExpr == null) {
            attrMappingExpr = xPath.compile("@value");
        }
        return attrMappingExpr;
    }

    private XPathExpression getAttrMappingExpr()
            throws XPathExpressionException {
        if (mappingExpr == null) {
            mappingExpr = xPath.compile("superitem[@id='attributes']/item");
        }
        return mappingExpr;
    }

    private XPathExpression getEntityMappingExpr(String id)
            throws XPathExpressionException {
        if (entityMappingExpr == null) {
            entityMappingExpr = xPath.compile("//UMLAssociation[@side_B='" + id
                    + "']");
        }
        return entityMappingExpr;
    }

    private XPathExpression getAssociationStereotypeMappingExpr(String id)
            throws XPathExpressionException {
        if (assocStereotypeMappingExpr == null) {
            assocStereotypeMappingExpr = xPath
                    .compile("//UMLClass[@id='" + id
                    + "']/item[@id='name']/@value");
        }
        return assocStereotypeMappingExpr;
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

    private String createToProfileScript(String domain, String srcPkg,
            String srcType, String trgtPkg, String trgtType, Document document,
            NodeList attributes, NodeList associatedEntities,
            String srcClassId)
            throws XPathExpressionException {
        // LOGGER.debug("Define conversion script from " + attributes);
        String startMarker = "«" + domain + ".";

        StringBuilder sb = new StringBuilder();
        addNashornCompatibility(sb);
        sb.append("importPackage(Packages." + srcPkg + ");\n");
        sb.append("importPackage(Packages." + trgtPkg + ");\n");
        sb.append("var o = new ").append(trgtType).append("();\n");
        // Surrogate key is not modelled so must add explicitly here
        sb.append("o.id = src.id;\n");
        for (int i = 0; i < attributes.getLength(); i++) {
            String val = getValueMappingExpr().evaluate(attributes.item(i));
            if (val.contains(startMarker) && val.contains("»")) {
                String value = getProfileFieldName(startMarker, val);
                String key = getFieldName(val);
                // String type = getFieldType(val);
                sb.append("o.setCustom !== undefined ? o.setCustom('")
                        .append(value).append("', src.").append(key)
                        .append("): o.").append(value).append(" = src.")
                        .append(key).append(";\n");
            } else {
                LOGGER.warn("No mapping found for: " + val);
            }
        }
        for (int i = 0; i < associatedEntities.getLength(); i++) {
            // String val =
            // getAssociationStereotypeProfileMappingExpr().evaluate(
            // associatedEntities.item(i));
            String val = xPath.compile(
                    "//UMLAssociation[@side_B='" + srcClassId
                            + "']/superitem[@id='stereotype']/item/@value")
                    .evaluate(document);
            if (val.contains(startMarker) && val.contains("»")) {
                System.err.println("TODO handle entity relationships");
                String value = getProfileFieldName(startMarker, val);

                String trgtClassId = xPath.compile(
                        "//UMLAssociation[@side_B='" + srcClassId
                                + "']/@side_A").evaluate(document);
                String key = toCamelCase(getAssociationStereotypeMappingExpr(
                        trgtClassId).evaluate(associatedEntities.item(i)));
                // String type = getFieldType(val);
                StringBuffer tmp = new StringBuffer();
                tmp.append("o.setCustom !== undefined ? o.setCustom('")
                        .append(value).append("', src.").append(key)
                        .append("): o.").append(value).append(" = src.")
                        .append(key).append(";\n");
                System.err.println("  wip: " + tmp.toString());
            } else {
                LOGGER.warn("No mapping found for: " + val);
            }
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(" script created:\n" + sb.toString());
        }
        return sb.toString();
    }

    private void addNashornCompatibility(StringBuilder sb) {
        // Nashorn compatibility layer
        String vsn = System.getProperty("java.version");
        if (!vsn.startsWith("1.6") && !vsn.startsWith("1.7")) {
            sb.append("load(\"nashorn:mozilla_compat.js\");");
        }
    }

    private String toCamelCase(String s) {
        return String.valueOf(s.charAt(0)).toLowerCase() + s.substring(1);
    }

    private String getProfileFieldName(String startMarker, String val) {
        return val.substring(val.indexOf(startMarker) + startMarker.length(),
                val.indexOf('»'));
    }

    private String getFieldName(String val) {
        return val.substring(val.indexOf('+') + 1, val.indexOf(':'));
    }

    private String createFromProfileScript(String domain, String srcPkg,
            String srcType, String trgtPkg, String trgtType, NodeList attributes)
            throws XPathExpressionException {
        // LOGGER.debug("Define conversion script from " + attributes);
        String startMarker = "«" + domain + ".";

        StringBuilder sb = new StringBuilder();
        addNashornCompatibility(sb);
        sb.append("importPackage(Packages." + srcPkg + ");\n");
        sb.append("importPackage(Packages." + trgtPkg + ");\n");
        sb.append("var o = new ").append(trgtType).append("();\n");
        // Surrogate key is not modelled
        sb.append("o.id = src.id;\n");
        for (int i = 0; i < attributes.getLength(); i++) {
            String val = getValueMappingExpr().evaluate(attributes.item(i));
            if (val.contains(startMarker) && val.contains("»")) {
                String value = getProfileFieldName(startMarker, val);
                String key = getFieldName(val);

                // TODO getCustom is specific to the Sugar API wrapper we made
                sb.append("o.").append(key).append(" = (src.").append(key)
                        .append(" === undefined ? src.getCustom('")
                        .append(value).append("'").append("): src.")
                        .append(key).append(");\n");
            } else {
                LOGGER.warn("No mapping found for: " + val);
            }
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(" script created:\n" + sb.toString());
        }
        return sb.toString();
    }

}
