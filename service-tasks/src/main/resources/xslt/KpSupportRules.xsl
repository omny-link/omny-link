<?xml version="1.0" encoding="UTF-8"?>
<!--
  Copyright 2011-2018 Tim Stephenson
  
  Licensed under the Apache License, Version 2.0 (the "License"); you may not
  use this file except in compliance with the License.  You may obtain a copy
  of the License at
  
    http://www.apache.org/licenses/LICENSE-2.0
  
  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
  License for the specific language governing permissions and limitations under
  the License.
-->
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
  <!-- Note: An imported style sheet has lower precedence than the importing style sheet. -->
  <xsl:import href="/xslt/ActivitiSupportRules.xsl"/>
  
  <xsl:output omit-xml-declaration="yes"/>
    
  <xsl:template match="/">
		<xsl:apply-templates/>
	</xsl:template>
  
  <xsl:template match="semantic:serviceTask|serviceTask">
    <xsl:choose>
      <xsl:when test="starts-with(@activiti:class,'com.knowprocess.fbook') or starts-with(@activiti:class,'com.knowprocess.identity') or starts-with(@activiti:class,'com.knowprocess.resource.spi') or  starts-with(@activiti:class,'com.knowprocess.xslt')">
        <xsl:text>INFO: Can handle service task '</xsl:text>
        <xsl:value-of select="./@id"/>
        <xsl:text>' with Activiti extension: </xsl:text>
        <xsl:value-of select="@activiti:class"/>
      </xsl:when>
      <xsl:when test="starts-with(@activiti:class,'com.knowprocess.logging.LoggingService')">
        <xsl:text>INFO: Can handle service task '</xsl:text>
        <xsl:value-of select="./@id"/>
        <xsl:text>' with Activiti extension: </xsl:text>
        <xsl:value-of select="@activiti:class"/>
      </xsl:when>
      <xsl:when test="starts-with(@activiti:class,'com.knowprocess.mk')">
        <xsl:text>INFO: Can handle service task '</xsl:text>
        <xsl:value-of select="./@id"/>
        <xsl:text>' with Activiti extension: </xsl:text>
        <xsl:value-of select="@activiti:class"/>
      </xsl:when>
      <xsl:when test="starts-with(@activiti:class,'com.knowprocess.activiti.sugarcrm')">
        <xsl:text>INFO: Can handle service task '</xsl:text>
        <xsl:value-of select="./@id"/>
        <xsl:text>' with Activiti extension: </xsl:text>
        <xsl:value-of select="@activiti:class"/>
      </xsl:when>
      <xsl:when test="@activiti:type = 'mail'">
        <xsl:text>INFO: Can handle mail service task '</xsl:text>
        <xsl:value-of select="./@id"/>
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
