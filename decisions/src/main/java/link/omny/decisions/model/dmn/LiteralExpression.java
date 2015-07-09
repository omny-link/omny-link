//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2014.12.02 at 12:08:24 PM GMT 
//


package link.omny.decisions.model.dmn;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;

/**
 * <p>Java class for tLiteralExpression complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="tLiteralExpression">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.omg.org/spec/DMN/20130901}tExpression">
 *       &lt;choice minOccurs="0">
 *         &lt;element name="text">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;any processContents='lax' namespace='##other' maxOccurs="unbounded" minOccurs="0"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element ref="{http://www.omg.org/spec/DMN/20130901}Import"/>
 *       &lt;/choice>
 *       &lt;attribute name="expressionLanguage" type="{http://www.w3.org/2001/XMLSchema}anyURI" />
 *       &lt;anyAttribute processContents='lax' namespace='##other'/>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlRootElement(name = "LiteralExpression")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "tLiteralExpression", propOrder = {
 "text", "_import"
})
public class LiteralExpression extends Expression {

	@XmlElement(name = "text", namespace = Definitions.DMN_1_0)
    protected Text text;
    @XmlElement(name = "Import")
    protected DecisionModelImport _import;
    @XmlAttribute(name = "expressionLanguage")
    @XmlSchemaType(name = "anyURI")
    protected String expressionLanguage;

    /**
     * Gets the value of the text property.
     * 
     * @return
     *     possible object is
     *     {@link LiteralExpression.Text }
     *     
     */
    public Text getText() {
        return text;
    }

    /**
     * Sets the value of the text property.
     * 
     * @param value
     *            allowed object is {@link LiteralExpression.Text }
     * @return
     * 
     */
    public LiteralExpression setText(Text value) {
        this.text = value;
        return this;
    }

    /**
     * Gets the value of the import property.
     * 
     * @return
     *     possible object is
     *     {@link DecisionModelImport }
     *     
     */
    public DecisionModelImport getImport() {
        return _import;
    }

    /**
     * Sets the value of the import property.
     * 
     * @param value
     *     allowed object is
     *     {@link DecisionModelImport }
     *     
     */
    public void setImport(DecisionModelImport value) {
        this._import = value;
    }

    /**
     * Gets the value of the expressionLanguage property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getExpressionLanguage() {
        return expressionLanguage;
    }

    @Override
    public LiteralExpression setItemDefinition(QName value) {
        return (LiteralExpression) super.setItemDefinition(value);
    }

    @Override
    public LiteralExpression setDescription(String value) {
        return (LiteralExpression) super.setDescription(value);
    }

    @Override
    public LiteralExpression setId(String value) {
        return (LiteralExpression) super.setId(value);
    }

    @Override
    public LiteralExpression setName(String value) {
        return (LiteralExpression) super.setName(value);
    }

    /**
     * Sets the value of the expressionLanguage property.
     *
     * @param value
     *            allowed object is {@link String }
     *
     */
    public void setExpressionLanguage(String value) {
        this.expressionLanguage = value;
    }
}
