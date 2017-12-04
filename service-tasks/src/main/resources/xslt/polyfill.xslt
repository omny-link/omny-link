<?xml version="1.0" encoding="UTF-8"?>
<!--
  This stylesheet attempts to fill in gaps that have been observed in some 
  imports. It does _not_ add any changes to make the processes more executable.

  Polyfills applied:
  - Add label bounds when they do not exist in diagram interchange.
  
-->
<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:activiti="http://activiti.org/bpmn"
  xmlns:bpmn="http://www.omg.org/spec/BPMN/20100524/MODEL"
  xmlns:bpmn2="http://www.omg.org/spec/BPMN/20100524/MODEL"
  xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI"
  xmlns:dc="http://www.omg.org/spec/DD/20100524/DC"
  xmlns:di="http://www.omg.org/spec/DD/20100524/DI"
  xmlns="http://www.w3.org/2000/svg"
  xmlns:svg="http://www.w3.org/2000/svg"
  xmlns:xlink="http://www.w3.org/1999/xlink"
  xmlns:tns="http://sourceforge.net/bpmn/definitions/_1447448008288"
  xmlns:xsd="http://www.w3.org/2001/XMLSchema"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xmlns:yaoqiang="http://bpmn.sourceforge.net">

  <xsl:variable name="debug">true</xsl:variable>

	<xsl:template match="bpmndi:BPMNLabel">
    <xsl:variable name="fontSize" select="./ancestor::bpmndi:BPMNDiagram//dc:Font/@size"/>
    <xsl:variable name="margin">2</xsl:variable>
    <xsl:variable name="semId" select="../@bpmnElement"/>
    <xsl:variable name="semElement" select="//bpmn:*[@id=$semId]"/>
    <xsl:variable name="diElement" select=".."/>

    <xsl:if test="$debug='true'">
      <xsl:comment>
        <xsl:text>semId = </xsl:text><xsl:value-of select="$semId"/> 
      </xsl:comment>
    </xsl:if>

    <xsl:choose>
      <xsl:when test="dc:Bounds">
        <xsl:element name="bpmndi:BPMNLabel">
          <xsl:apply-templates select="@*"/>
          <xsl:apply-templates/>
        </xsl:element>
      </xsl:when>
      <xsl:otherwise>
        <!-- Make an educated guess at label bounds -->
        <xsl:variable name="labelWidth">
          <xsl:choose>
            <xsl:when test="$diElement/dc:Bounds and (contains(local-name($semElement),'callActivity') or contains(local-name($semElement),'subProcess') or contains(local-name($semElement),'task') or contains(local-name($semElement),'Task'))">
              <xsl:value-of select="$diElement/dc:Bounds/@width - (2*$margin)"/>
            </xsl:when>
            <xsl:when test="$diElement/dc:Bounds">
              <xsl:value-of select="$diElement/dc:Bounds/@width*2"/>
            </xsl:when>
            <xsl:otherwise>0</xsl:otherwise>
          </xsl:choose>
        </xsl:variable>
        <xsl:variable name="labelHeight">
          <xsl:choose>
            <xsl:when test="string-length($semElement/@name)*6.5 &gt; $labelWidth and (contains(local-name($semElement),'callActivity') or contains(local-name($semElement),'subProcess') or contains(local-name($semElement),'task') or contains(local-name($semElement),'Task'))">
              <!-- Assume two lines enough -->
              <xsl:value-of select="$fontSize*3"/>
            </xsl:when>
            <xsl:when test="string-length($semElement/@name)*6.25 &gt; $labelWidth and (contains(local-name($semElement),'Event') or contains(local-name($semElement),'Gateway'))">
              <!-- Assume two lines enough -->
              <xsl:value-of select="$fontSize*3"/>
            </xsl:when>
            <xsl:otherwise>
              <!-- Assume one line enough -->
              <xsl:value-of select="$fontSize*1.5"/>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:variable>        
        <xsl:variable name="labelX">
          <xsl:choose>
            <xsl:when test="$diElement/bpmndi:BPMNLabel/dc:Bounds">
              <xsl:value-of select="$diElement/bpmndi:BPMNLabel/dc:Bounds/@x"/>
            </xsl:when>
            <xsl:when test="contains(local-name($semElement),'callActivity') or contains(local-name($semElement),'subProcess') or contains(local-name($semElement),'task') or contains(local-name($semElement),'Task')">
              <xsl:value-of select="$diElement/dc:Bounds/@x+$margin"/>
            </xsl:when>
            <xsl:when test="local-name($diElement)='BPMNEdge' and not(bpmndi:BPMNLabel/dc:Bounds)">
              <xsl:value-of select="$diElement/di:waypoint[position()=1]/@x"/>
            </xsl:when>
            <xsl:otherwise>
              <!-- includes events, data object refs etc. -->
              <xsl:value-of select="$diElement/dc:Bounds/@x - ($diElement/dc:Bounds/@width div 3)"/>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:variable>
        <xsl:variable name="labelY">
          <xsl:choose>
            <xsl:when test="$diElement/bpmndi:BPMNLabel/dc:Bounds">
              <xsl:value-of select="$diElement/bpmndi:BPMNLabel/dc:Bounds/@y"/>
            </xsl:when>
            <xsl:when test="contains(local-name($semElement),'callActivity') or contains(local-name($semElement),'subProcess') or contains(local-name($semElement),'task') or contains(local-name($semElement),'Task')">
              <xsl:value-of select="$diElement/dc:Bounds/@y+($diElement/dc:Bounds/@height div 2)"/>
            </xsl:when>
            <xsl:when test="local-name($diElement)='BPMNEdge' and not(bpmndi:BPMNLabel/dc:Bounds)">
              <xsl:value-of select="$diElement/di:waypoint[position()=1]/@y"/>
            </xsl:when>
            <xsl:otherwise>
              <xsl:value-of select="$diElement/dc:Bounds/@y+$diElement/dc:Bounds/@height"/>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:variable>

        <xsl:element name="bpmndi:BPMNLabel">
          <xsl:apply-templates select="@*"/>
          <xsl:apply-templates/>
          <xsl:element name="dc:Bounds">
            <xsl:attribute name="height">
              <xsl:value-of select="$labelHeight"/>
            </xsl:attribute>
            <xsl:attribute name="width">
              <xsl:value-of select="$labelWidth"/>
            </xsl:attribute>
            <xsl:attribute name="x">
              <xsl:value-of select="$labelX"/>
            </xsl:attribute>
            <xsl:attribute name="y">
              <xsl:value-of select="$labelY"/>
            </xsl:attribute>
          </xsl:element>
        </xsl:element>
      </xsl:otherwise>
    </xsl:choose>
	</xsl:template>

	<!-- standard copy template -->
	<xsl:template match="@*|node()">
		<xsl:copy>
			<xsl:apply-templates select="@*"/>
			<xsl:apply-templates/>
		</xsl:copy>
	</xsl:template>	
</xsl:stylesheet>