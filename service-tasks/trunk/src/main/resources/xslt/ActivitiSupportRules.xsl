<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" 
    xmlns:dc="http://www.omg.org/spec/DD/20100524/DC" 
    xmlns:bpsim="http://www.bpsim.org/schemas/1.0" 
    xmlns:di="http://www.omg.org/spec/DD/20100524/DI" 
    xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL" 
    xmlns:semantic="http://www.omg.org/spec/BPMN/20100524/MODEL" 
    xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI" 
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output omit-xml-declaration="yes"/>
  
  <xsl:template match="semantic:definitions|definitions">
    <!-- Nothing to say -->
    <xsl:apply-templates/>
  </xsl:template>
  
  <xsl:template match="semantic:process|process">
    <!-- Nothing to say -->
    <xsl:apply-templates/>
  </xsl:template>
  
  <xsl:template match="semantic:boundaryEvent|boundaryEvent">
    <xsl:choose>
      <xsl:when test="not(semantic:cancelEventDefinition|semantic:compensateEventDefinition|semantic:errorEventDefinition|semantic:messageEventDefinition|semantic:signalEventDefinition|semantic:timerEventDefinition)">
        <xsl:text>ERROR: Boundary events must specify exactly one of: cancelEventDefinition, compensateEventDefinition, errorEventDefinition, messageEventDefinition, signalEventDefinition, timerEventDefinition</xsl:text>
      </xsl:when>
      <xsl:otherwise>
        <xsl:text>Pass: boundaryEvent '</xsl:text>
        <xsl:value-of select="@id"/>
        <xsl:text>', checks of the specific event type follow.</xsl:text>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <xsl:template match="semantic:endEvent|endEvent">
    <xsl:choose>
      <xsl:when test="text() and not(semantic:errorEventDefinition|semantic:cancelEventDefinition)">
        <xsl:text>Pass: End events may be empty or may specify one of: errorEventDefinition, cancelEventDefinition</xsl:text>
      </xsl:when>
      <xsl:when test="text() and (semantic:extensionElements|semantic:incoming)">
        <xsl:text>Ignored: extensionElements and/or incoming [flow] of </xsl:text>
        <xsl:value-of select="@id"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:text>Pass: endEvent </xsl:text>
        <xsl:value-of select="@id"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <xsl:template match="semantic:eventBasedGateway|eventBasedGateway|semantic:exclusiveGateway|exclusiveGateway|semantic:inclusiveGateway|inclusiveGateway|semantic:parallelGateway|parallelGateway">
    <xsl:choose>
      <xsl:when test="text() and normalize-space(text())!=''">
        <xsl:text>ERROR: </xsl:text>
        <xsl:value-of select="local-name(.)"/>
        <xsl:text> must be empty, in fact contains: </xsl:text>
        <xsl:value-of select="normalize-space(text())"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:text>Pass: </xsl:text>
        <xsl:value-of select="local-name(.)"/>
        <xsl:text> </xsl:text>
        <xsl:value-of select="@id"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <xsl:template match="semantic:intermediateCatchEvent|intermediateCatchEvent">
    <xsl:choose>
      <xsl:when test="not(semantic:messageEventDefinition|semantic:signalEventDefinition)">
        <xsl:text>ERROR: Intermediate catch events must specify exactly one of: messageEventDefinition, signalEventDefinition</xsl:text>
      </xsl:when>
      <xsl:otherwise>
        <xsl:text>Pass: intermediateEvent '</xsl:text>
        <xsl:value-of select="@id"/>
        <xsl:text>', checks of the specific event type follow.</xsl:text>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <xsl:template match="semantic:intermediateThrowEvent|intermediateThrowEvent">
    <xsl:choose>
      <xsl:when test="text() and not(semantic:signalEventDefinition)">
        <xsl:text>ERROR: Intermediate throw events must be empty or specify exactly one of: signalEventDefinition</xsl:text>
      </xsl:when>
      <xsl:otherwise>
        <xsl:text>Pass: intermediateThrowEvent '</xsl:text>
        <xsl:value-of select="@id"/>
        <xsl:text>', checks of the specific event type follow.</xsl:text>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <xsl:template match="semantic:message|message">
    <xsl:choose>
      <xsl:when test="1=2">
        <!-- No tests defined yet -->
      </xsl:when>
      <xsl:otherwise>
        <xsl:text>Pass: message</xsl:text>
        <xsl:value-of select="@id"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <xsl:template match="semantic:sequenceFlow|sequenceFlow">
    <xsl:choose>
      <xsl:when test="not(@sourceRef) or not(@targetRef)">
        <xsl:text>ERROR: Sequence flows require both source and target references.</xsl:text>
      </xsl:when>
      <xsl:when test="text() and not(semantic:conditionExpression)">
        <xsl:text>ERROR: Sequence flows must be empty or specify exactly one of: conditionExpression</xsl:text>
      </xsl:when>
      <xsl:otherwise>
        <xsl:text>Pass: sequenceFlow</xsl:text>
        <xsl:value-of select="@id"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <xsl:template match="semantic:signal|signal">
    <xsl:choose>
      <xsl:when test="1=2">
        <!-- No tests defined yet -->
      </xsl:when>
      <xsl:otherwise>
        <xsl:text>Pass: signal</xsl:text>
        <xsl:value-of select="@id"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <xsl:template match="semantic:startEvent|startEvent">
    <xsl:choose>
      <xsl:when test="text() and (semantic:errorEventDefinition|semantic:messageEventDefinition|semantic:timerEventDefinition)">
        <xsl:text>Pass: Start events may be empty or specify: errorEventDefinition, messageEventDefinition, timerEventDefinition</xsl:text>
      </xsl:when>
      <xsl:when test="text() and (semantic:extensionElements|semantic:outgoing)">
        <xsl:text>Ignored: extensionElements and/or outgoing [flow] of </xsl:text>
        <xsl:value-of select="@id"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:text>Pass: startEvent </xsl:text>
        <xsl:value-of select="@id"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <xsl:template match="semantic:timerEventDefinition|timerEventDefinition">
    <xsl:choose>
      <xsl:when test="not(semantic:timeDate|semantic:timeDuration|semantic:timeCycle)">
        <xsl:text>ERROR: Timer events must specify exactly one of: timeDate, timeDuration or timeCycle for timerEventDefinition with id: </xsl:text>
        <xsl:value-of select="@id"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:text>Pass: timerEventDefinition</xsl:text>
        <xsl:value-of select="@id"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <xsl:template match="semantic:userTask|userTask">
    <xsl:choose>
      <!-- 
      <xsl:when test="text() and not(semantic:errorEventDefinition|semantic:messageEventDefinition|semantic:timerEventDefinition)">
        <xsl:text>ERROR: Start events must be empty or specify exactly one of: errorEventDefinition, messageEventDefinition, timerEventDefinition</xsl:text>
      </xsl:when>
       -->
      <xsl:when test="text() and (semantic:extensionElements|semantic:incoming|semantic:outgoing)">
        <xsl:text>Ignored: extensionElements, incoming [flow] and/or outgoing [flow] of </xsl:text>
        <xsl:value-of select="@id"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:text>Pass: startEvent </xsl:text>
        <xsl:value-of select="@id"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
	<!-- template to suppress anything else -->
	<xsl:template match="semantic:*">
    <xsl:text>Unsupported: </xsl:text>
    <xsl:value-of select="name(.)"/>
    <xsl:apply-templates/>
  </xsl:template> 
  
  <!-- template to suppress anything else -->
  <xsl:template match="bpmndi:*|di:*|dc:*">
		<xsl:text>Ignored: </xsl:text>
    <xsl:value-of select="name(.)"/>
    <xsl:apply-templates/>
	</xsl:template>	

</xsl:stylesheet>