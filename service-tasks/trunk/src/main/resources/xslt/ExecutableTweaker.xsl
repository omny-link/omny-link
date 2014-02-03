<?xml version="1.0" encoding="UTF-8"?>
<!-- 
  This stylesheet optimistically converts BPMN files in ways that it knows 
  it is possible for the runtime to execute. It does not validate out features 
  that cannot be supported which is the responsibility of other tools. 
  
  Tweaks applied: 
  - set isExecutable to true.
  - Manual task -> User task 
  - Set default transition for exclusive gateway
  - Suppress non-executable process
  - Add an activiti initiator (named 'initiator') if one does not exist. 
  - Convert unsupported service tasks into user tasks.
  
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
  
  <xsl:output method="xml" omit-xml-declaration="no" indent="yes"
     cdata-section-elements="activiti:expression documentation semantic:script script"/>
  
  <!-- 
    When the BPMN file contains more than one process there should be a 
    collaboration containing participants. We'll specify one of these 
    participants to identify the process intended for execution
  -->
  <xsl:param name="processParticipantToExecute"/>
  
  <xsl:template match="bpmndi:BPMNEdge|BPMNEdge">
    <xsl:variable name="bpmnId" select="@bpmnElement"/>
    <xsl:if test="//process[@name=$processParticipantToExecute]//@id=$bpmnId">
      <xsl:copy>
        <xsl:apply-templates select="@*"/>
        <xsl:apply-templates/>
      </xsl:copy>
    </xsl:if>
  </xsl:template>
  
  <xsl:template match="BPMNShape">
    <xsl:variable name="bpmnId" select="@bpmnElement"/>
	  <xsl:if test="//participant[@name=$processParticipantToExecute]/@id=$bpmnId or //process[@name=$processParticipantToExecute]//@id=$bpmnId">
	    <xsl:copy>
	      <xsl:apply-templates select="@*"/>
	      <xsl:apply-templates/>
	    </xsl:copy>
	  </xsl:if>
  </xsl:template>
  
  <xsl:template match="semantic:collaboration|collaboration">
    <xsl:copy>
      <xsl:choose>
        <xsl:when test="$processParticipantToExecute!=''">
          <xsl:apply-templates select="participant[@name=$processParticipantToExecute]"/>  
        </xsl:when>
        <xsl:otherwise>
          <xsl:apply-templates select="participant"/>
        </xsl:otherwise>
      </xsl:choose>
      
    </xsl:copy>
  </xsl:template>
  
  <xsl:template match="semantic:exclusiveGateway|exclusiveGateway">
    <xsl:variable name="id">
      <xsl:value-of select="@id"/>
    </xsl:variable>
    
    <xsl:choose>
      <xsl:when test="not(@default) and count(//semantic:sequenceFlow[@sourceRef=$id]) > 1 and count(//semantic:sequenceFlow[@sourceRef=$id and not(semantic:conditionExpression|conditionExpression)]) = 1">
        <xsl:comment>
          <xsl:text>Setting default for gateway: </xsl:text>
          <xsl:value-of select="@id"/>
        </xsl:comment>
        <xsl:copy>
          <xsl:attribute name="default">
            <xsl:value-of select="//semantic:sequenceFlow[@sourceRef=$id and not(conditionExpression)]/@id"/>
          </xsl:attribute>
          <xsl:apply-templates select="@*"/>
          <xsl:apply-templates/>
        </xsl:copy>
      </xsl:when>
      <xsl:otherwise>
        <xsl:copy>
          <xsl:apply-templates select="@*"/>
          <xsl:apply-templates/>
        </xsl:copy>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
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
  <xsl:template match="process[@id!=//participant[@name=$processParticipantToExecute]/@processRef]">
    <xsl:variable name="id"><xsl:value-of select="@id"/></xsl:variable>
    <xsl:comment>
      <xsl:text>Suppressing non-executable process: </xsl:text>
      <xsl:value-of select="//participant[@processRef=$id]/@name"/>
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
  
  <xsl:template match="semantic:startEvent|startEvent">
    <xsl:copy>
      <xsl:if test="not(@activiti:initiator)">
        <xsl:attribute name="activiti:initiator">
          <xsl:text>initiator</xsl:text>
        </xsl:attribute>
      </xsl:if>
      <xsl:apply-templates select="@*"/>
      <xsl:apply-templates/>
    </xsl:copy>
  </xsl:template>
  
  <!-- 
    Convert all non-user tasks into user tasks.
  -->
  <xsl:template match="semantic:manualTask|manualTask|semantic:task|task">
    <xsl:variable name="elName">
      <xsl:choose>
        <xsl:when test="name(.) = 'manualTask' or name(.) = 'task'">
          <xsl:text>userTask</xsl:text>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="substring-before(name(.),':')"/>
          <xsl:text>:userTask</xsl:text>    
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:variable name="process" select="parent::node()"/>
    <xsl:for-each select="//collaboration/participant">
      <xsl:choose>
        <xsl:when test="$process/@id=@processRef">
          <xsl:text>HELLO 2!</xsl:text>
            <xsl:value-of select="@name"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:text>GOODBYE 2</xsl:text>
          <xsl:attribute name="activiti:assignee">${initiator}</xsl:attribute>
        </xsl:otherwise>
      </xsl:choose>
      </xsl:for-each>
    <xsl:element name="{$elName}">
      <xsl:attribute name="activiti:candidateGroups">*
      <xsl:apply-templates select="//collaboration/participant[@processRef=$process/@id]" mode="resource">
        <xsl:with-param name="id" select="@id"/>
        <xsl:with-param name="process" select="parent::node()"/>
      </xsl:apply-templates></xsl:attribute>
      <xsl:apply-templates select="@*"/>
      <xsl:apply-templates/>
    </xsl:element>
  </xsl:template>
  
  <!-- 
    Convert unsupported service tasks into user tasks.
  -->
  <xsl:template match="semantic:receiveTask|receiveTask|semantic:sendTask|sendTask|semantic:serviceTask|serviceTask">
    <xsl:comment>
      <xsl:value-of select="local-name(.)"/>
      <xsl:text> converted to user task and assigned to initiator.</xsl:text>
    </xsl:comment>
    <xsl:text>&#x0A;</xsl:text>
    <xsl:element name="userTask">
      <xsl:apply-templates select="//collaboration/participant" mode="resource">
        <xsl:with-param name="id" select="@id"/>
        <xsl:with-param name="process" select="parent::node()"/>
      </xsl:apply-templates>
      <xsl:apply-templates select="@*[not(local-name(.)='delegateExpression' or local-name(.)='messageRef')]"/>
      <xsl:apply-templates/>
    </xsl:element>
  </xsl:template>
  
  <!-- Pass thru template of the service tasks that are supported follows -->
  
  <xsl:template match="semantic:serviceTask[@activiti:class='com.knowprocess.activiti.sugarcrm.CreateContactAndAccountTask']|serviceTask[@activiti:class='com.knowprocess.activiti.sugarcrm.CreateContactAndAccountTask']">
    <xsl:copy-of select="."/>
  </xsl:template>
  
  <xsl:template match="semantic:serviceTask[@activiti:class='com.knowprocess.activiti.sugarcrm.CreateLeadTask']|serviceTask[@activiti:class='com.knowprocess.activiti.sugarcrm.CreateLeadTask']">
    <xsl:copy-of select="."/>
  </xsl:template>
  
  <xsl:template match="semantic:serviceTask[@activiti:class='com.knowprocess.activiti.sugarcrm.GetContactTask']|serviceTask[@activiti:class='com.knowprocess.activiti.sugarcrm.GetContactTask']">
    <xsl:copy-of select="."/>
  </xsl:template>

  <xsl:template match="semantic:serviceTask[@activiti:class='com.knowprocess.activiti.sugarcrm.RecordMailToLeadTask']|serviceTask[@activiti:class='com.knowprocess.activiti.sugarcrm.RecordMailToLeadTask']">
    <xsl:copy-of select="."/>
  </xsl:template>
    
  <xsl:template match="semantic:serviceTask[@activiti:class='com.knowprocess.deployment.DeploymentService']|serviceTask[@activiti:class='com.knowprocess.deployment.DeploymentService']">
    <xsl:copy-of select="."/>
  </xsl:template>
  
  <xsl:template match="semantic:serviceTask[@activiti:class='com.knowprocess.fbook.FacebookTask']|serviceTask[@activiti:class='com.knowprocess.fbook.FacebookTask']">
    <xsl:copy-of select="."/>
  </xsl:template>
  
  <xsl:template match="semantic:serviceTask[@activiti:class='com.knowprocess.resource.spi.Fetcher']|serviceTask[@activiti:class='com.knowprocess.resource.spi.Fetcher']">
    <xsl:copy-of select="."/>
  </xsl:template>
  
  <xsl:template match="semantic:serviceTask[@activiti:class='com.knowprocess.identity.IdentityTask']|serviceTask[@activiti:class='com.knowprocess.identity.IdentityTask']">
    <xsl:copy-of select="."/>
  </xsl:template>
  
  <xsl:template match="semantic:serviceTask[@activiti:class='com.knowprocess.xslt.TransformTask']|serviceTask[@activiti:class='com.knowprocess.xslt.TransformTask']">
    <xsl:copy-of select="."/>
  </xsl:template>
  
  <xsl:template match="semantic:serviceTask[@activiti:type='mail']|serviceTask[@activiti:type='mail']">
    <xsl:copy-of select="."/>
  </xsl:template>
  
  <!-- 
  <xsl:template match="semantic:sequenceFlow|sequenceFlow">
    <xsl:variable name="id" select="@id"/>
    <xsl:variable name="sourceId" select="@sourceRef"/>
    <xsl:variable name="source" select="//*[@id=$sourceId]"/>
    <xsl:choose>
      <xsl:when test="local-name($source) = 'exclusiveGateway' and count(//semantic:sequenceFlow[@sourceRef=$source/@id])>1 and not(./semantic:conditionExpression) and $source/@default!=$id">
        <xsl:text>ERROR: REQUIRED: Need condition on sequence flow with id: </xsl:text>
        <xsl:value-of select="@id"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:copy>
          <xsl:apply-templates select="@*"/>
          <xsl:apply-templates/>
        </xsl:copy>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
   -->
   
  <xsl:template match="collaboration/participant" mode="resource">
    <xsl:param name="id"/>
    <xsl:param name="process"/>
      
    <xsl:value-of select="$process/@id"/>*  
    <xsl:value-of select="//collaboration/participant[@processRef=$process/@id]"/>^
    <xsl:value-of select="$process/@id=@processRef"/>%
    <xsl:choose>
      <xsl:when test="$process/@id=@processRef">
        <xsl:text>HELLO!</xsl:text>
          <xsl:value-of select="@name"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:text>GOODBYE ${initiator}</xsl:text>
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