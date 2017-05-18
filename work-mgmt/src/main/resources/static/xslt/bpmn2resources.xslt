<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:bpmn="http://www.omg.org/spec/BPMN/20100524/MODEL" 
  xmlns:bpmn2="http://www.omg.org/spec/BPMN/20100524/MODEL" 
  xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI" 
  xmlns:dc="http://www.omg.org/spec/DD/20100524/DC" 
  xmlns:di="http://www.omg.org/spec/DD/20100524/DI"
  xmlns:xlink="http://www.w3.org/1999/xlink"
  xmlns:tns="http://sourceforge.net/bpmn/definitions/_1447448008288"
  xmlns:xsd="http://www.w3.org/2001/XMLSchema"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xmlns:yaoqiang="http://bpmn.sourceforge.net">

  <xsl:output method="text" encoding="utf-8" indent="yes" />
  
  <xsl:template match="/">
    <xsl:apply-templates select="//bpmn:resource"/>
  </xsl:template>
  
  <xsl:template match="bpmn:resource">
    <xsl:value-of select="@name"/>
    <xsl:text>,</xsl:text>
  </xsl:template>
  
</xsl:stylesheet>
