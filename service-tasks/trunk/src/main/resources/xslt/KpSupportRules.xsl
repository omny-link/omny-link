<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" 
    xmlns:activiti="http://activiti.org/bpmn"
    xmlns:dc="http://www.omg.org/spec/DD/20100524/DC" 
    xmlns:bpsim="http://www.bpsim.org/schemas/1.0" 
    xmlns:di="http://www.omg.org/spec/DD/20100524/DI" 
    xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL" 
    xmlns:semantic="http://www.omg.org/spec/BPMN/20100524/MODEL" 
    xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI" 
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:output omit-xml-declaration="yes"/>
    
  <!-- Note: An imported style sheet has lower precedence than the importing style sheet. -->
  <xsl:import href="/xslt/ActivitiSupportRules.xsl"/>
  
  <xsl:template match="/">
		<xsl:apply-templates/>
	</xsl:template>
  
  <xsl:template match="semantic:serviceTask|serviceTask">
    <xsl:choose>
      <xsl:when test="@activiti:class = 'com.knowprocess.resource.spi.Fetcher'">
        <xsl:text>ERROR: Cannot handle service task '</xsl:text>
        <xsl:value-of select="./@id"/>
        <xsl:text>' with Activiti extension: </xsl:text>
        <xsl:value-of select="@activiti:class"/>
      </xsl:when>
      <xsl:when test="@activiti:class">
        <xsl:text>ERROR: Cannot handle service task '</xsl:text>
        <xsl:value-of select="./@id"/>
        <xsl:text>' with Activiti extension: </xsl:text>
        <xsl:value-of select="@activiti:class"/>
      </xsl:when>
      <xsl:when test="@activiti:delegateExpression">
        <xsl:text>ERROR: Cannot handle service task '</xsl:text>
        <xsl:value-of select="./@id"/>
        <xsl:text>' with Activiti delegate expression: </xsl:text>
        <xsl:value-of select="@activiti:delegateExpression"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:text>ERROR: Cannot handle service task with id: </xsl:text>
        <xsl:value-of select="./@id"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

</xsl:stylesheet>