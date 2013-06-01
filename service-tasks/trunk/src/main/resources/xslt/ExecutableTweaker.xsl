<?xml version="1.0" encoding="UTF-8"?>
<!-- 
  This stylesheet optimistically converts BPMN files in ways that it knows 
  it is possible for the runtime to execute. It does not validate out features 
  that cannot be supported which is the responsibility of other tools. 
  
  Tweaks applied: 
  - set isExecutable to true.
  - Manual task -> User task 
  
-->
<xsl:stylesheet version="1.0" 
  xmlns:dc="http://www.omg.org/spec/DD/20100524/DC" 
  xmlns:bpsim="http://www.bpsim.org/schemas/1.0" 
  xmlns:di="http://www.omg.org/spec/DD/20100524/DI" 
  xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL" 
  xmlns:semantic="http://www.omg.org/spec/BPMN/20100524/MODEL" 
  xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI" 
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:template match="semantic:process/@isExecutable">
		<xsl:attribute name="isExecutable">true</xsl:attribute>
	</xsl:template>
  
  <xsl:template match="semantic:task|task">
    <xsl:variable name="elName">
      <xsl:choose>
        <xsl:when test="name(.) = 'task'">
          <xsl:text>userTask</xsl:text>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="substring-before(name(.),':')"/>
          <xsl:text>:userTask</xsl:text>    
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:element name="{$elName}">
      <xsl:apply-templates select="@*"/>
      <xsl:apply-templates/>
    </xsl:element>
  </xsl:template>
  
	<!-- standard copy template -->
	<xsl:template match="@*|node()">
		<xsl:copy>
			<xsl:apply-templates select="@*"/>
			<xsl:apply-templates/>
		</xsl:copy>
	</xsl:template>	
</xsl:stylesheet>