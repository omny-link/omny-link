<?xml version="1.0" encoding="UTF-8"?>
<!-- 
  This stylesheet applies some fixes to make better auto-laid out images than 
  Activiti would otherwise offer. 
  
  Tweaks applied: 
  - Add diagram interchange info for labels.
  - Set diagram name derived from process name.
  - Add margin to prevent flow elements being rendered tight to top left edge.
-->
<xsl:stylesheet version="1.0" 
  xmlns:dc="http://www.omg.org/spec/DD/20100524/DC" 
  xmlns:bpsim="http://www.bpsim.org/schemas/1.0" 
  xmlns:di="http://www.omg.org/spec/DD/20100524/DI" 
  xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL" 
  xmlns:semantic="http://www.omg.org/spec/BPMN/20100524/MODEL" 
  xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI" 
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:activiti="http://activiti.org/bpmn"
  xmlns:omgdc="http://www.omg.org/spec/DD/20100524/DC"
  xmlns:omgdi="http://www.omg.org/spec/DD/20100524/DI">
  
  <xsl:output method="xml" omit-xml-declaration="no" indent="yes"
     cdata-section-elements="activiti:expression documentation semantic:script script"/>

  <xsl:variable name="marginLeft" select="50" />
  <xsl:variable name="marginTop" select="50"/>
  <xsl:variable name="lcase" select="'abcdefghijklmnopqrstuvwxyz'" />
  <xsl:variable name="ucase" select="'ABCDEFGHIJKLMNOPQRSTUVWXYZ'" />

  <xsl:template match="bpmndi:BPMNDiagram">
    <xsl:copy>
      <xsl:apply-templates select="@*"/>
        <xsl:if test="not(@name)">
          <xsl:attribute name="name">
            <xsl:value-of select="//semantic:process/@name"/>
          </xsl:attribute>
        </xsl:if>
      <xsl:apply-templates/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="bpmndi:BPMNShape">
    <xsl:variable name="bpmnElementId">
      <xsl:value-of select="@bpmnElement"/>
    </xsl:variable>
    <xsl:variable name="bpmnElementType">
      <xsl:value-of select="local-name(//*[@id=$bpmnElementId])"/>
    </xsl:variable>
  
    <xsl:choose>
      <xsl:when test="$bpmnElementType='subProcess'">
        <xsl:copy>
          <xsl:apply-templates select="@*|dc:Bounds"/>
        
          <xsl:element name="bpmndi:BPMNLabel">
            <xsl:element name="dc:Bounds">
              <xsl:attribute name="height"><xsl:value-of select="number(dc:Bounds/@height)"/></xsl:attribute>
              <xsl:attribute name="width"><xsl:value-of select="number(dc:Bounds/@width)"/></xsl:attribute>
              <xsl:attribute name="x"><xsl:value-of select="dc:Bounds/@x"/></xsl:attribute>
              <xsl:attribute name="y"><xsl:value-of select="dc:Bounds/@y"/></xsl:attribute>
            </xsl:element>
          </xsl:element>
        </xsl:copy>
      </xsl:when>
      <xsl:otherwise>
        <xsl:copy>
          <xsl:apply-templates select="@*|dc:Bounds"/>
        
          <xsl:element name="bpmndi:BPMNLabel">
            <xsl:element name="dc:Bounds">
              <xsl:attribute name="height"><xsl:value-of select="number(dc:Bounds/@height)-36"/></xsl:attribute>
              <xsl:attribute name="width"><xsl:value-of select="number(dc:Bounds/@width)-36"/></xsl:attribute>
              <xsl:attribute name="x"><xsl:value-of select="dc:Bounds/@x+$marginLeft+18"/></xsl:attribute>
              <xsl:attribute name="y"><xsl:value-of select="dc:Bounds/@y+$marginTop+18"/></xsl:attribute>
            </xsl:element>
          </xsl:element>
        </xsl:copy>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="bpmndi:BPMNShape/omgdc:Bounds/@x|bpmndi:BPMNEdge/omgdi:waypoint/@x">
    <xsl:attribute name="x">
      <xsl:value-of select="number(.)+$marginLeft"/>
    </xsl:attribute>
  </xsl:template>

  <xsl:template match="bpmndi:BPMNShape/omgdc:Bounds/@y|bpmndi:BPMNEdge/omgdi:waypoint/@y">
    <xsl:attribute name="y">
      <xsl:value-of select="number(.)+$marginTop"/>
    </xsl:attribute>
  </xsl:template>

  <!-- standard copy template -->
  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*"/>
      <xsl:apply-templates/>
    </xsl:copy>
  </xsl:template>
  
  <!-- ********************** -->
  <!-- NAMED TEMPLATES FOLLOW -->
  <!-- ********************** -->
  
</xsl:stylesheet>