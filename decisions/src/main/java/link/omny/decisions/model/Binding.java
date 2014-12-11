//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2014.12.02 at 12:08:24 PM GMT 
//


package link.omny.decisions.model;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;


/**
 * <p>Java class for tBinding complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="tBinding">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.omg.org/spec/DMN/20130901}Expression" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="parameter" use="required" type="{http://www.w3.org/2001/XMLSchema}QName" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "tBinding", propOrder = {
    "expression"
})
public class Binding {

    @XmlElementRef(name = "Expression", namespace = "http://www.omg.org/spec/DMN/20130901", type = JAXBElement.class, required = false)
    protected JAXBElement<? extends Expression> expression;
    @XmlAttribute(name = "parameter", required = true)
    protected QName parameter;

    /**
     * Gets the value of the expression property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link Invocation }{@code >}
     *     {@link JAXBElement }{@code <}{@link DecisionTable }{@code >}
     *     {@link JAXBElement }{@code <}{@link LiteralExpression }{@code >}
     *     {@link JAXBElement }{@code <}{@link Expression }{@code >}
     *     
     */
    public JAXBElement<? extends Expression> getExpression() {
        return expression;
    }

    /**
     * Sets the value of the expression property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link Invocation }{@code >}
     *     {@link JAXBElement }{@code <}{@link DecisionTable }{@code >}
     *     {@link JAXBElement }{@code <}{@link LiteralExpression }{@code >}
     *     {@link JAXBElement }{@code <}{@link Expression }{@code >}
     *     
     */
    public void setExpression(JAXBElement<? extends Expression> value) {
        this.expression = value;
    }

    /**
     * Gets the value of the parameter property.
     * 
     * @return
     *     possible object is
     *     {@link QName }
     *     
     */
    public QName getParameter() {
        return parameter;
    }

    /**
     * Sets the value of the parameter property.
     * 
     * @param value
     *     allowed object is
     *     {@link QName }
     *     
     */
    public void setParameter(QName value) {
        this.parameter = value;
    }

}
