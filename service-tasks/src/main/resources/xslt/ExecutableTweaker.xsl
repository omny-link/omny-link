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
  - Convert activiti resource shortcuts (activiti:candidateXxx) to BPMN standard ones
  - Convert unsupported service tasks into user tasks, suppressing ST attributes, and assign to either pool name or initiator if no pools.
  - Add form property extensions for data objects (TODO when there is no form key???)
  - Suppress assignee and candidate group shortcuts when potentialOwner is specified (also helps avoid namespace clash with camunda)
  - Suppress Camunda formKey (those with extension jsf)
  - Suppress interface that does not have complete operation and message ref
  - Suppress incomplete start events (e.g. signalEvent without signalRef)
  - Suppress message/@itemRef (to avoid Activiti validation: [Validation set: 'activiti-executable-process' | Problem: 'activiti-message-invalid-item-ref'] : Item reference is invalid: not found - [Extra info : ] ( line: 6, column: 104))
  - When dataInput/dataOutput has no name infer one from itemSubjectRef for activiti:formProperty
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
  xmlns:camunda="http://activiti.org/bpmn"
  xmlns:signavio="http://activiti.org/bpmn">

  <xsl:output method="xml" omit-xml-declaration="no" indent="yes"
     cdata-section-elements="activiti:expression documentation semantic:script script"/>

  <xsl:variable name="lcase" select="'abcdefghijklmnopqrstuvwxyz'" />
  <xsl:variable name="ucase" select="'ABCDEFGHIJKLMNOPQRSTUVWXYZ'" />

  <!--
    When the BPMN file contains more than one process there should be a
    collaboration containing participants. We'll specify one of these
    participants to identify the process intended for execution
  -->
  <xsl:param name="processParticipantToExecute"/>

  <xsl:template match="bpmndi:BPMNEdge|BPMNEdge">
    <xsl:variable name="bpmnId" select="@bpmnElement"/>
    <xsl:if test="not($processParticipantToExecute) or /process[@name=$processParticipantToExecute]//@id=$bpmnId">
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

  <xsl:template match="semantic:conditionExpression">
    <xsl:choose>
      <xsl:when test="/semantic:definitions/@expressionLanguage='http://www.w4.eu/spec/EL/20110701'">
        <xsl:comment>ATTEMPT TO CONVERT W4 UEL EXPRESSION</xsl:comment>
        <xsl:copy>
          <xsl:apply-templates select="@*"/>
          <xsl:value-of select="substring-before(text(),'processInstance.dataEntries.')"/>
          <xsl:value-of select="substring-before(substring-after(text(),'processInstance.dataEntries.'),'.value')"/>
          <xsl:value-of select="substring-after(substring-after(text(),'processInstance.dataEntries.'),'.value')"/>
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

  <xsl:template match="semantic:dataInput|dataInput|semantic:dataOutput|dataOutput" mode="formProperty">
    <xsl:variable name="name">
      <xsl:choose>
        <xsl:when test="name"><xsl:value-of select="@name"/></xsl:when>
        <xsl:when test="@itemSubjectRef and starts-with(@itemSubjectRef, 'itemDef')">
          <xsl:value-of select="substring-after(@itemSubjectRef,'itemDef')"/>
        </xsl:when>
        <xsl:when test="@itemSubjectRef">
          <xsl:value-of select="@itemSubjectRef"/>
        </xsl:when>
        <xsl:otherwise></xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:element name="activiti:formProperty">
      <xsl:attribute name="id"><xsl:value-of select="$name"/></xsl:attribute>
      <xsl:attribute name="name">
        <xsl:value-of select="translate(substring($name,1,1), $lcase, $ucase)" />
        <xsl:value-of select="substring($name,2)" />
      </xsl:attribute>
      <xsl:attribute name="readable">true</xsl:attribute>
      <xsl:attribute name="writeable">true</xsl:attribute>
      <!-- Some conventions to allow type to be set for simple XSD types -->
      <xsl:choose>
        <xsl:when test="@itemSubjectRef='xsdBool'">
          <xsl:attribute name="type">boolean</xsl:attribute>
        </xsl:when>
        <xsl:when test="@itemSubjectRef='xsdDate'">
          <xsl:attribute name="type">date</xsl:attribute>
        </xsl:when>
        <xsl:when test="@itemSubjectRef='xsdDatetime'">
          <xsl:attribute name="type">datetime</xsl:attribute>
        </xsl:when>
        <xsl:when test="@itemSubjectRef='xsdInt'">
          <xsl:attribute name="type">long</xsl:attribute>
        </xsl:when>
        <xsl:when test="@itemSubjectRef='xsdLong'">
          <xsl:attribute name="type">long</xsl:attribute>
        </xsl:when>
        <xsl:when test="@itemSubjectRef='xsdString'">
          <xsl:attribute name="type">string</xsl:attribute>
        </xsl:when>
      </xsl:choose>
    </xsl:element>
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

  <!--
    Suppress incomplete start events
    Arose in 2016 Reston demo
  -->
  <xsl:template match="semantic:flowNodeRef">
    <xsl:variable name="flowNodeId"><xsl:value-of select="text()"/></xsl:variable>
    <xsl:choose>
      <xsl:when test="//semantic:startEvent[@id=$flowNodeId]/semantic:signalEventDefinition[not(@signalRef)]">
        <xsl:comment>Suppressed ref to incomplete start event <xsl:value-of select="$flowNodeId"/></xsl:comment>
      </xsl:when>
      <xsl:otherwise>
        <xsl:copy>
          <xsl:apply-templates select="@*"/>
          <xsl:apply-templates/>
        </xsl:copy>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!--
    Suppress interface that does not have complete operation and message ref
    Arose in 2016 Reston demo
  -->
  <xsl:template match="semantic:interface|interface">
    <xsl:choose>
      <xsl:when test="semantic:operation|operation and .//semantic:inMessageRef|.//inMessageRef|.//semantic:outMessageRef|.//outMessageRef and .//semantic:inMessageRef/text()|.//inMessageRef/text()|.//semantic:outMessageRef/text()|.//outMessageRef/text()">
        <xsl:copy>
          <xsl:apply-templates select="@*"/>
          <xsl:apply-templates/>
        </xsl:copy>
      </xsl:when>
      <xsl:otherwise>
        <xsl:comment>Suppressed incomplete interface with id: <xsl:value-of select="@id"/></xsl:comment>
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

  <!--
     Suppress message/@itemRef to avoid Activiti validation error: [Validation set: 'activiti-executable-process' | Problem: 'activiti-message-invalid-item-ref'] : Item reference is invalid: not found - [Extra info : ] ( line: 6, column: 104)
     Occurred in Brussels 2017 MIWG demo

  -->
  <xsl:template match="semantic:message/@itemRef|message/@itemRef">
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

  <!--
    No longer needed as have implemented support for resourceRef (awaiting merge)
    Convert resource ref into formal expressions
  <xsl:template match="semantic:resourceRef">
    <xsl:variable name="id">
      <xsl:value-of select="text()"/>
    </xsl:variable>

    <xsl:element name="semantic:resourceAssignmentExpression">
      <xsl:element name="semantic:formalExpression">
        <xsl:value-of select="//semantic:resource[@id=$id]/@name"/>
      </xsl:element>
    </xsl:element>
  </xsl:template>-->

  <!--
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
  </xsl:template>-->

  <!--
    Convert all manual and unspecified tasks into user tasks.
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

    <xsl:element name="{$elName}">
      <xsl:choose>
        <xsl:when test="//collaboration/participant[@processRef=$process/@id]">
          <xsl:attribute name="activiti:candidateGroups">
            <xsl:apply-templates select="//collaboration/participant[@processRef=$process/@id]" mode="resource">
              <xsl:with-param name="id" select="@id"/>
              <xsl:with-param name="process" select="parent::node()"/>
            </xsl:apply-templates>
          </xsl:attribute>
        </xsl:when>
        <xsl:otherwise>
          <xsl:attribute name="activiti:assignee">${initiator}</xsl:attribute>
        </xsl:otherwise>
      </xsl:choose>
      <xsl:apply-templates select="@*"/>
      <xsl:apply-templates/>
    </xsl:element>
  </xsl:template>

  <!--
    Convert unsupported service tasks into user tasks.
  -->
  <xsl:template match="semantic:sendTask|sendTask|semantic:serviceTask|serviceTask">
    <xsl:comment>
      <xsl:value-of select="local-name(.)"/>
      <xsl:text> converted to user task.</xsl:text>
    </xsl:comment>
    <xsl:text>&#x0A;</xsl:text>
    <xsl:element name="userTask">
      <xsl:choose>
        <xsl:when test="//collaboration/participant">
          <xsl:apply-templates select="//collaboration/participant" mode="resource">
            <xsl:with-param name="id" select="@id"/>
            <xsl:with-param name="process" select="parent::node()"/>
          </xsl:apply-templates>
        </xsl:when>
        <xsl:when test="not(potentialOwner)">
          <xsl:attribute name="activiti:assignee">${initiator}</xsl:attribute>
        </xsl:when>
        <xsl:otherwise>
        </xsl:otherwise>
      </xsl:choose>
      <xsl:apply-templates select="@*[not(local-name(.)='delegateExpression' or local-name(.)='instantiate' or local-name(.)='messageRef' or local-name(.)='operationRef')]"/>
      <xsl:apply-templates/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="semantic:scriptTask|scriptTask">
    <xsl:choose>
      <xsl:when test="@scriptFormat='text/x-apache-velocity'">
        <xsl:comment>ATTEMPT TO CONVERT text/x-apache-velocity SCRIPT (PROBABLY FROM W4)</xsl:comment>
        <xsl:element name="semantic:scriptTask">
          <xsl:attribute name="scriptFormat">JavaScript</xsl:attribute>
          <xsl:apply-templates select="@id"/>
          <xsl:apply-templates select="@isForCompensation"/>
          <xsl:apply-templates select="@name"/>
          <xsl:apply-templates select="@name"/>
          <semantic:script>
            <xsl:text><![CDATA[1==1;]]></xsl:text>
          </semantic:script>
        </xsl:element>
      </xsl:when>
      <xsl:otherwise>
        <xsl:copy>
          <xsl:apply-templates select="@*"/>
          <xsl:apply-templates/>
        </xsl:copy>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!--
    Suppress incomplete start events
    Arose in 2016 Reston demo
  -->
  <xsl:template match="semantic:sequenceFlow">
    <xsl:variable name="flowNodeId"><xsl:value-of select="@sourceRef"/></xsl:variable>
    <xsl:choose>
      <xsl:when test="//semantic:startEvent[@id=$flowNodeId]/semantic:signalEventDefinition[not(@signalRef)]">
        <xsl:comment>Suppressed ref to incomplete start event <xsl:value-of select="$flowNodeId"/></xsl:comment>
      </xsl:when>
      <xsl:otherwise>
        <xsl:copy>
          <xsl:apply-templates select="@*"/>
          <xsl:apply-templates/>
        </xsl:copy>
      </xsl:otherwise>
    </xsl:choose>

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
  <xsl:template match="semantic:serviceTask[@activiti:class='com.knowprocess.el.TemplateTask']|serviceTask[@activiti:class='com.knowprocess.el.TemplateTask']">
    <xsl:copy-of select="."/>
  </xsl:template>
  <xsl:template match="semantic:serviceTask[@activiti:class='com.knowprocess.fbook.FacebookTask']|serviceTask[@activiti:class='com.knowprocess.fbook.FacebookTask']">
    <xsl:copy-of select="."/>
  </xsl:template>
  <xsl:template match="semantic:serviceTask[@activiti:class='com.knowprocess.logging.LoggingService']|serviceTask[@activiti:class='com.knowprocess.logging.LoggingService']">
    <xsl:copy-of select="."/>
  </xsl:template>
     <xsl:template match="semantic:serviceTask[@activiti:class='com.knowprocess.mk.EstimateFare']|serviceTask[@activiti:class='com.knowprocess.mk.EstimateFare']">
    <xsl:copy-of select="."/>
  </xsl:template>
  <xsl:template match="semantic:serviceTask[@activiti:class='com.knowprocess.resource.spi.RestDelete']|serviceTask[@activiti:class='com.knowprocess.resource.spi.RestDelete']">
    <xsl:copy-of select="."/>
  </xsl:template>
  <xsl:template match="semantic:serviceTask[@activiti:class='com.knowprocess.resource.spi.Fetcher']|serviceTask[@activiti:class='com.knowprocess.resource.spi.Fetcher']">
    <xsl:copy-of select="."/>
  </xsl:template>
  <xsl:template match="semantic:serviceTask[@activiti:class='com.knowprocess.resource.spi.RestGet']|serviceTask[@activiti:class='com.knowprocess.resource.spi.RestGet']">
    <xsl:copy-of select="."/>
  </xsl:template>
  <xsl:template match="semantic:serviceTask[@activiti:class='com.knowprocess.resource.spi.RestPost']|serviceTask[@activiti:class='com.knowprocess.resource.spi.RestPost']">
    <xsl:copy-of select="."/>
  </xsl:template>
  <xsl:template match="semantic:serviceTask[@activiti:class='com.knowprocess.resource.spi.RestPut']|serviceTask[@activiti:class='com.knowprocess.resource.spi.RestPut']">
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

  <!--
    Suppress incomplete start events
    Arose in 2016 Reston demo
  -->
  <xsl:template match="semantic:startEvent">
    <xsl:choose>
      <xsl:when test="semantic:signalEventDefinition[not(@signalRef)]">
        <xsl:comment>Suppress incomplete signal start event (without signal reference)</xsl:comment>
      </xsl:when>
      <xsl:otherwise>
        <xsl:copy>
          <xsl:apply-templates select="@*|*"/>
        </xsl:copy>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

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

  <!--
    See if there are more extensions we can infer from standards-based elements
  -->
  <xsl:template match="semantic:userTask|userTask">
    <xsl:comment>user task <xsl:value-of select="@id"/> has extensions: <xsl:choose><xsl:when test="semantic:extensionElements|extensionElements">Yes</xsl:when><xsl:otherwise>No</xsl:otherwise></xsl:choose></xsl:comment>
    <xsl:choose>
      <xsl:when test="semantic:extensionElements|extensionElements">
        <xsl:copy>
          <xsl:apply-templates select="@*|*"/>
        </xsl:copy>
      </xsl:when>
      <xsl:otherwise>
        <xsl:comment>Creating extensionElements</xsl:comment>
        <xsl:copy>
          <xsl:apply-templates select="@*"/>
          <xsl:element name="extensionElements">
            <xsl:apply-templates select="semantic:ioSpecification/semantic:dataInput" mode="formProperty"/>
            <xsl:apply-templates select="ioSpecification/dataInput" mode="formProperty"/>
            <xsl:apply-templates select="semantic:ioSpecification/semantic:dataOutput" mode="formProperty"/>
            <xsl:apply-templates select="ioSpecification/dataOutput" mode="formProperty"/>
          </xsl:element>
          <xsl:apply-templates select="*"/>
        </xsl:copy>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="semantic:userTask/semantic:extensionElements|userTask/extensionElements">
    <xsl:comment>Extending extensionElements</xsl:comment>
    <xsl:copy>
      <xsl:apply-templates select="@*|*"/>
      <xsl:apply-templates select="../semantic:ioSpecification/semantic:dataInput" mode="formProperty"/>
      <xsl:apply-templates select="../ioSpecification/dataInput" mode="formProperty"/>
      <xsl:apply-templates select="../semantic:ioSpecification/semantic:dataOutput" mode="formProperty"/>
      <xsl:apply-templates select="../ioSpecification/dataOutput" mode="formProperty"/>
    </xsl:copy>
  </xsl:template>

  <!--
    If we have both assignee/candidateGroups and potentialOwner give preference to the standard form.
  -->
  <xsl:template match="semantic:userTask/@camunda:assignee|semantic:userTask/@camunda:candidateGroups">
    <xsl:choose>
      <xsl:when test="../semantic:potentialOwner|../potentialOwner">
      </xsl:when>
      <xsl:otherwise>
        <xsl:copy>
          <xsl:apply-templates select="@*"/>
        </xsl:copy>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!--
    Because Camunda never changed their namespace we get potential clashes on formKey. Assume theirs will end .jsf.
  -->
  <xsl:template match="@camunda:formKey|@signavio:formKey">
    <xsl:choose>
      <xsl:when test="substring(., string-length(.) - string-length('.jsf') +1)='.jsf'">
      </xsl:when>
      <xsl:otherwise>
        <xsl:copy>
          <xsl:apply-templates select="@*"/>
        </xsl:copy>
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

  <!-- ********************** -->
  <!-- NAMED TEMPLATES FOLLOW -->
  <!-- ********************** -->

  <!-- TODO No worky -->
  <xsl:template name="toLabel">
    <xsl:param name="text"/>
    <xsl:comment>HELLO LABEL</xsl:comment>
    <xsl:variable name="lcase" select="'abcdefghijklmnopqrstuvwxyz'" />
    <xsl:variable name="ucase" select="'ABCDEFGHIJKLMNOPQRSTUVWXYZ'" />
    <xsl:value-of select="translate(substring($text,1,1), $lcase, $ucase)" />
    <xsl:value-of select="substring($text,2)" />
    <xsl:value-of select="$text" />
  </xsl:template>

</xsl:stylesheet>
