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
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:activiti="http://activiti.org/bpmn">
  
  <!-- 
    When the BPMN file contains more than one process there should be a 
    collaboration containing participants. We'll specify one of these 
    participants to identify the process intended for execution
  -->
  <xsl:param name="processParticipantToExecute"/>
  
  <!-- TODO this looks like a restriction in Activiti that could (ought) to be removed -->
  <xsl:template match="semantic:message[not(@name)]">
    <xsl:copy>
      <xsl:attribute name="name">
        <xsl:value-of select="@id"/>
      </xsl:attribute>
      <xsl:apply-templates select="@*"/>
      <xsl:apply-templates/>
    </xsl:copy>
  </xsl:template>
  
  <!-- Suppress processes not intended for execution -->
  <xsl:template match="semantic:process[@id!=//semantic:participant[@name=$processParticipantToExecute]/@processRef]">
    <xsl:variable name="id"><xsl:value-of select="@id"/></xsl:variable>
    <xsl:comment>
      <xsl:text>Suppressing non-executable process: </xsl:text>
      <xsl:value-of select="//semantic:participant[@processRef=$id]/@name"/>
      <xsl:text> (</xsl:text>
      <xsl:value-of select="$id"/>
      <xsl:text>)</xsl:text>
    </xsl:comment>
  </xsl:template>
  
	<xsl:template match="semantic:process/@isExecutable">
		<xsl:attribute name="isExecutable">true</xsl:attribute>
	</xsl:template>
  
  <!-- Convert resource ref into formal expressions -->
  <xsl:template match="semantic:resourceRef">
    <xsl:variable name="id">
      <xsl:value-of select="text()"/>
    </xsl:variable>
    
    <xsl:element name="semantic:resourceAssignmentExpression">
      <xsl:element name="semantic:formalExpression">
        <xsl:value-of select="//semantic:resource[@id=$id]/@name"/>
      </xsl:element>
    </xsl:element>
  </xsl:template>
  
  <!-- 
    Convert all non-user tasks into user tasks.
  -->
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
  
  <!-- 
    Convert unsupported service tasks into user tasks.
  -->
  <xsl:template match="semantic:serviceTask|serviceTask">
    <xsl:comment> Service Task converted to user task and assigned to initiator</xsl:comment>
    <xsl:element name="userTask">
      <xsl:attribute name="activiti:assignee">${initiator}</xsl:attribute>
      <xsl:apply-templates select="@*[not(local-name='delegateExpression')]"/>
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