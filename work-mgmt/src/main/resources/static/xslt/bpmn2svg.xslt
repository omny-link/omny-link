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
  
  <xsl:param name="diagramId" select="//bpmndi:BPMNDiagram[position()=1]/@id"/>

  <xsl:variable name="fontSize">12</xsl:variable>
  <xsl:variable name="lineSpacing">3</xsl:variable>
  <xsl:variable name="showBounds">false</xsl:variable>
  <xsl:variable name="debug">false</xsl:variable>
  <xsl:variable name="diagram" select="//bpmndi:BPMNDiagram[@id=$diagramId]"/>
  <xsl:variable name="firstParticipant" select="//bpmn:participant[@id=$diagram//@bpmnElement][position()=1]/@id"/>
  <xsl:variable name="lastParticipant" select="//bpmn:participant[@id=$diagram//@bpmnElement][position()=last()]/@id"/>

  <xsl:template match="/">
    <xsl:if test="$debug='true'">
      <xsl:comment> # of participants: <xsl:value-of select="count(//bpmn:participant)"/></xsl:comment>
      <xsl:comment> # of participants: <xsl:value-of select="count(//participant)"/></xsl:comment>
      <xsl:comment> first participant id: <xsl:value-of select="$firstParticipant"/></xsl:comment>
      <xsl:comment> last participant id: <xsl:value-of select="$lastParticipant"/></xsl:comment>
    </xsl:if>
    <xsl:element name="svg">
      <xsl:attribute name="id"><xsl:value-of select="$diagramId"/></xsl:attribute>
      <xsl:attribute name="name"><xsl:value-of select="//bpmndi:BPMNDiagram[@id=$diagramId]/@name"/></xsl:attribute>
      <xsl:attribute name="version">1.1</xsl:attribute>
      <xsl:choose>
        <xsl:when test="$firstParticipant">
          <!-- x of 1st participant + y of last + height of last -->
          <xsl:attribute name="height"><xsl:value-of select="//bpmndi:BPMNShape[@bpmnElement=$firstParticipant]/dc:Bounds/@y + //bpmndi:BPMNShape[@bpmnElement=$lastParticipant]/dc:Bounds/@y + //bpmndi:BPMNShape[@bpmnElement=$lastParticipant]/dc:Bounds/@height"/></xsl:attribute>
          <xsl:attribute name="width"><xsl:value-of select="//bpmndi:BPMNShape[@bpmnElement=$firstParticipant]/dc:Bounds/@x + //bpmndi:BPMNShape[@bpmnElement=$firstParticipant]/dc:Bounds/@width+20"/></xsl:attribute>
        </xsl:when>
        <xsl:otherwise>
          <!-- No Participants - NOTE THIS REQUIRES XALAN 2.7.1 not JRE XSLT engine -->
          <xsl:attribute name="height">
            <xsl:call-template name="maximum">
			        <xsl:with-param name="sequence" select="//dc:Bounds/@y"/>
			        <xsl:with-param name="margin">50</xsl:with-param>
			      </xsl:call-template>
          </xsl:attribute>
          <xsl:attribute name="width">
              <xsl:call-template name="maximum">
	              <xsl:with-param name="sequence" select="//dc:Bounds/@x"/>
	              <xsl:with-param name="margin">50</xsl:with-param>
	            </xsl:call-template>
          </xsl:attribute>
        </xsl:otherwise>
      </xsl:choose>
     <defs>
        <marker id="backslash" markerWidth="6" markerHeight="6" refX="3" refY="0" markerUnits="strokeWidth">
          <path d="M 0,0 10,10" stroke="#666" stroke-width="1px"/>
        </marker>
	      <marker id="filled-arrow" markerWidth="10" markerHeight="10" refX="8" refY="3" orient="auto" markerUnits="strokeWidth">
		      <path d="M0,0 L0,6 L9,3 z" fill="#666" />
		    </marker>
        <marker id="open-arrow" markerWidth="10" markerHeight="10" refX="8" refY="3" orient="auto" markerUnits="strokeWidth">
          <path d="M 0,0 C 4.514306,2.8087928 4.7790399,2.7522338 0,6 M 0,6 9,3 0,0"  fill="#666" />
			  </marker>
      </defs>

      <!--rect class="background" fill="#333" height="1080" width="1920" x="0" y="0"/-->

      <!-- LANES -->
      <xsl:apply-templates select="//bpmn:participant[//bpmndi:BPMNDiagram[@id=$diagramId]//@bpmnElement=@id]"/>
      <xsl:apply-templates select="//bpmn:lane[//bpmndi:BPMNDiagram[@id=$diagramId]//@bpmnElement=@id]"/>

      <!-- CONTAINERS -->
      <xsl:apply-templates select="//bpmn:subProcess[//bpmndi:BPMNDiagram[@id=$diagramId]//@bpmnElement=@id]"/>

      <!-- FLOW OBJECTS -->
      <xsl:apply-templates select="//bpmn:messageFlow[//bpmndi:BPMNDiagram[@id=$diagramId]//@bpmnElement=@id]"/>
      <xsl:apply-templates select="//bpmn:sequenceFlow[//bpmndi:BPMNDiagram[@id=$diagramId]//@bpmnElement=@id]"/>
      <!-- TEXT ANNOTATION LINES (before activities) -->
      <xsl:apply-templates select="//bpmn:association[//bpmndi:BPMNDiagram[@id=$diagramId]//@bpmnElement=@id]"/>

      <!-- GATEWAYS -->
      <xsl:apply-templates select="//bpmn:complexGateway[//bpmndi:BPMNDiagram[@id=$diagramId]//@bpmnElement=@id]"/>
      <xsl:apply-templates select="//bpmn:eventBasedGateway[//bpmndi:BPMNDiagram[@id=$diagramId]//@bpmnElement=@id]"/>
      <xsl:apply-templates select="//bpmn:exclusiveGateway[//bpmndi:BPMNDiagram[@id=$diagramId]//@bpmnElement=@id]"/>
      <xsl:apply-templates select="//bpmn:inclusiveGateway[//bpmndi:BPMNDiagram[@id=$diagramId]//@bpmnElement=@id]"/>
      <xsl:apply-templates select="//bpmn:parallelGateway[//bpmndi:BPMNDiagram[@id=$diagramId]//@bpmnElement=@id]"/>

      <!-- CALL ACTIVITIES -->
      <xsl:apply-templates select="//bpmn:callActivity[//bpmndi:BPMNDiagram[@id=$diagramId]//@bpmnElement=@id]"/>

      <!-- TASKS -->
      <xsl:apply-templates select="//bpmn:businessRuleTask[//bpmndi:BPMNDiagram[@id=$diagramId]//@bpmnElement=@id]"/>
      <xsl:apply-templates select="//bpmn:manualTask[//bpmndi:BPMNDiagram[@id=$diagramId]//@bpmnElement=@id]"/>
      <xsl:apply-templates select="//bpmn:receiveTask[//bpmndi:BPMNDiagram[@id=$diagramId]//@bpmnElement=@id]"/>
      <xsl:apply-templates select="//bpmn:scriptTask[//bpmndi:BPMNDiagram[@id=$diagramId]//@bpmnElement=@id]"/>
      <xsl:apply-templates select="//bpmn:sendTask[//bpmndi:BPMNDiagram[@id=$diagramId]//@bpmnElement=@id]"/>
      <xsl:apply-templates select="//bpmn:serviceTask[//bpmndi:BPMNDiagram[@id=$diagramId]//@bpmnElement=@id]"/>
      <xsl:apply-templates select="//bpmn:task[//bpmndi:BPMNDiagram[@id=$diagramId]//@bpmnElement=@id]"/>
      <xsl:apply-templates select="//bpmn:userTask[//bpmndi:BPMNDiagram[@id=$diagramId]//@bpmnElement=@id]"/>

      <!-- EVENTS -->
      <xsl:apply-templates select="//bpmn:startEvent[//bpmndi:BPMNDiagram[@id=$diagramId]//@bpmnElement=@id]"/>
      <xsl:apply-templates select="//bpmn:boundaryEvent[//bpmndi:BPMNDiagram[@id=$diagramId]//@bpmnElement=@id]"/>
      <xsl:apply-templates select="//bpmn:intermediateCatchEvent[//bpmndi:BPMNDiagram[@id=$diagramId]//@bpmnElement=@id]"/>
      <xsl:apply-templates select="//bpmn:intermediateThrowEvent[//bpmndi:BPMNDiagram[@id=$diagramId]//@bpmnElement=@id]"/>
      <xsl:apply-templates select="//bpmn:endEvent[//bpmndi:BPMNDiagram[@id=$diagramId]//@bpmnElement=@id]"/>

      <!-- DATA OBJECTS, after all flow lines have been drawn -->
      <xsl:apply-templates select="//bpmn:dataInput[//bpmndi:BPMNDiagram[@id=$diagramId]//@bpmnElement=@id]"/>
      <xsl:apply-templates select="//bpmn:dataObject[//bpmndi:BPMNDiagram[@id=$diagramId]//@bpmnElement=@id]"/>
      <xsl:apply-templates select="//bpmn:dataOutput[//bpmndi:BPMNDiagram[@id=$diagramId]//@bpmnElement=@id]"/>
      <xsl:apply-templates select="//bpmn:dataObjectReference[//bpmndi:BPMNDiagram[@id=$diagramId]//@bpmnElement=@id]"/>

      <xsl:apply-templates select="//bpmn:dataStore[//bpmndi:BPMNDiagram[@id=$diagramId]//@bpmnElement=@id]"/>
      <xsl:apply-templates select="//bpmn:dataStoreReference[//bpmndi:BPMNDiagram[@id=$diagramId]//@bpmnElement=@id]"/>
      
      <!-- TEXT ANNOTATIONS -->
      <xsl:apply-templates select="//bpmn:textAnnotation[//bpmndi:BPMNDiagram[@id=$diagramId]//@bpmnElement=@id]"/>

    </xsl:element>
  </xsl:template>

  <!-- DATA OBJECTS -->
  <xsl:template match="bpmn:dataObjectReference|bpmn:dataInput|bpmn:dataObject|bpmn:dataOutput">
    <xsl:variable name="id" select="@id"/>

    <!-- <xsl:element name="rect">
      <xsl:attribute name="class">dataObject</xsl:attribute>
      <xsl:attribute name="fill">#fff</xsl:attribute>
      <xsl:attribute name="height">
        <xsl:value-of select="//bpmndi:BPMNShape[@bpmnElement=$id]/dc:Bounds/@height"/>
      </xsl:attribute>
      <xsl:attribute name="stroke">#666</xsl:attribute>
      <xsl:attribute name="width">
        <xsl:value-of select="//bpmndi:BPMNShape[@bpmnElement=$id]/dc:Bounds/@width"/>
      </xsl:attribute>
      <xsl:attribute name="x">
        <xsl:value-of select="//bpmndi:BPMNShape[@bpmnElement=$id]/dc:Bounds/@x"/>
      </xsl:attribute>
      <xsl:attribute name="y">
        <xsl:value-of select="//bpmndi:BPMNShape[@bpmnElement=$id]/dc:Bounds/@y"/>
      </xsl:attribute>
    </xsl:element>-->

    <xsl:choose>
      <xsl:when test="//bpmndi:BPMNShape[@bpmnElement=$id]/dc:Bounds/@x">
		    <xsl:element name="path">
		      <xsl:attribute name="id"><xsl:value-of select="@id"/></xsl:attribute>
		      <xsl:attribute name="class"><xsl:value-of select="local-name(.)"/></xsl:attribute>
		      <xsl:attribute name="d">
		        <xsl:choose>
              <xsl:when test="local-name(.)='dataInput'"><xsl:value-of select="$dataInputIcon"/></xsl:when>		        
              <xsl:when test="local-name(.)='dataOutput'"><xsl:value-of select="$dataOutputIcon"/></xsl:when>           
              <xsl:otherwise><xsl:value-of select="$dataObjectIcon"/></xsl:otherwise>		        
		        </xsl:choose>
		      </xsl:attribute>
		      <xsl:attribute name="transform">translate(<xsl:value-of select="number(//bpmndi:BPMNShape[@bpmnElement=$id]/dc:Bounds/@x)-5"/>,<xsl:value-of select="number(//bpmndi:BPMNShape[@bpmnElement=$id]/dc:Bounds/@y)-10"/>) scale(0.035)</xsl:attribute>
		    </xsl:element>
		    <xsl:element name="path">
          <xsl:attribute name="id"><xsl:value-of select="@id"/>Fill</xsl:attribute>
          <xsl:attribute name="class"><xsl:value-of select="local-name(.)"/>Fill</xsl:attribute>
          <xsl:attribute name="d">
            <xsl:choose>
              <xsl:when test="local-name(.)='dataInput'"><xsl:value-of select="$dataInputIconFill"/></xsl:when>           
              <xsl:when test="local-name(.)='dataOutput'"><xsl:value-of select="$dataOutputIconFill"/></xsl:when>           
              <xsl:otherwise><xsl:value-of select="$dataObjectIconFill"/></xsl:otherwise>
            </xsl:choose>
          </xsl:attribute>
          <xsl:attribute name="fill">#ccc</xsl:attribute>
          <xsl:attribute name="transform">translate(<xsl:value-of select="number(//bpmndi:BPMNShape[@bpmnElement=$id]/dc:Bounds/@x)-5"/>,<xsl:value-of select="number(//bpmndi:BPMNShape[@bpmnElement=$id]/dc:Bounds/@y)-10"/>) scale(0.035)</xsl:attribute>
        </xsl:element>
		
				<xsl:call-template name="label">
		      <xsl:with-param name="id" select="$id"></xsl:with-param>
		      <xsl:with-param name="r">0</xsl:with-param>
		      <xsl:with-param name="tx">0</xsl:with-param>
		      <xsl:with-param name="ty">0</xsl:with-param>
		    </xsl:call-template>
		  </xsl:when>
		  <xsl:otherwise>
		    <xsl:if test="$debug='true'">
		      <xsl:comment>ERROR: NO BOUNDS FOR DATA OBJECT <xsl:value-of select="$id"/></xsl:comment>
		    </xsl:if>
		  </xsl:otherwise>
	  </xsl:choose>
  </xsl:template>

  <xsl:template match="bpmn:dataState|dataState">
    <xsl:variable name="id" select="../@id"/>
    
    <xsl:variable name="diElement" select="//bpmndi:*[@bpmnElement=$id]"/>
    
    <!-- not using label template as id is not data state id but its parent -->
    <xsl:element name="text">
      <xsl:attribute name="class">label <xsl:value-of select="local-name(.)"/></xsl:attribute>
      <xsl:attribute name="stroke">#0e9acd</xsl:attribute>
      <xsl:attribute name="transform">translate(0,<xsl:value-of select="$fontSize*1.5"/>)</xsl:attribute>
      <xsl:attribute name="x">
        <xsl:value-of select="$diElement/bpmndi:BPMNLabel/dc:Bounds/@x"/>
      </xsl:attribute>
      <xsl:attribute name="y">
        <xsl:value-of select="$diElement/bpmndi:BPMNLabel/dc:Bounds/@y+$fontSize"/>
      </xsl:attribute>
      <xsl:text>[</xsl:text>
      <xsl:value-of select="@name"/>
      <xsl:text>]</xsl:text>
    </xsl:element>
  </xsl:template>

  <xsl:template match="bpmn:dataStoreReference">
    <xsl:variable name="id" select="@id"/>
    <xsl:variable name="y">
      <xsl:value-of select="//bpmndi:BPMNShape[@bpmnElement=$id]/dc:Bounds/@y+(//bpmndi:BPMNShape[@bpmnElement=$id]/dc:Bounds/@height div 2)-10"/>
    </xsl:variable>

    <xsl:element name="rect">
      <xsl:attribute name="class">
        <xsl:if test="$showBounds='true'">bounds </xsl:if>
        <xsl:value-of select="local-name(.)"/>
      </xsl:attribute>
      <xsl:attribute name="data-name"><xsl:value-of select="@name"/></xsl:attribute>
      <xsl:attribute name="data-type"><xsl:value-of select="local-name(.)"/></xsl:attribute>
      <xsl:attribute name="fill">
        <xsl:choose>
          <xsl:when test="$showBounds='true'">#5F5</xsl:when>
          <xsl:otherwise>white</xsl:otherwise>
        </xsl:choose>
      </xsl:attribute>
      <xsl:attribute name="id"><xsl:value-of select="$id"/></xsl:attribute>
      <xsl:attribute name="height">
        <xsl:value-of select="//bpmndi:BPMNShape[@bpmnElement=$id]/dc:Bounds/@height"/>
      </xsl:attribute>
      <xsl:attribute name="width">
        <xsl:value-of select="//bpmndi:BPMNShape[@bpmnElement=$id]/dc:Bounds/@width"/>
      </xsl:attribute>
      <xsl:attribute name="x">
        <xsl:value-of select="//bpmndi:BPMNShape[@bpmnElement=$id]/dc:Bounds/@x"/>
      </xsl:attribute>
      <xsl:attribute name="y">
        <xsl:value-of select="//bpmndi:BPMNShape[@bpmnElement=$id]/dc:Bounds/@y"/>
      </xsl:attribute>
    </xsl:element>
    <xsl:if test="//bpmndi:BPMNShape[@bpmnElement=$id]/dc:Bounds/@x">
      <xsl:element name="g">
        <xsl:attribute name="class">icon <xsl:value-of select="local-name(.)"/>Icon</xsl:attribute>
	      <xsl:attribute name="data-name"><xsl:value-of select="@name"/></xsl:attribute>
	      <xsl:attribute name="data-type"><xsl:value-of select="local-name(.)"/></xsl:attribute>
        <xsl:choose>
          <xsl:when test="//bpmndi:BPMNShape[@bpmnElement=$id]/dc:Bounds/@height &gt; 30">
            <xsl:attribute name="transform">translate(<xsl:value-of select="number(//bpmndi:BPMNShape[@bpmnElement=$id]/dc:Bounds/@x)+5"/>,<xsl:value-of select="number($y)-15"/>) scale(0.04)</xsl:attribute>
          </xsl:when>
          <xsl:otherwise>
            <xsl:attribute name="transform">translate(<xsl:value-of select="number(//bpmndi:BPMNShape[@bpmnElement=$id]/dc:Bounds/@x)+5"/>,<xsl:value-of select="number($y)"/>) scale(0.02)</xsl:attribute>
          </xsl:otherwise>
        </xsl:choose>
        <xsl:element name="path">
          <xsl:attribute name="d">
            <xsl:value-of select="$dataStoreIcon"/>
          </xsl:attribute>
        </xsl:element>
      </xsl:element>
    </xsl:if>
      
    <xsl:call-template name="label">
      <xsl:with-param name="id" select="$id"></xsl:with-param>
      <xsl:with-param name="r">0</xsl:with-param>
      <xsl:with-param name="tx">0</xsl:with-param>
      <xsl:with-param name="ty">0</xsl:with-param>
    </xsl:call-template>
    
    <xsl:apply-templates select="bpmn:dataState"/>
  </xsl:template>

  <!-- DATA OBJECT ASSOCIATIONS -->
  <xsl:template match="bpmn:dataInputAssociation">
    <xsl:variable name="id" select="@id"/>
    <xsl:variable name="sourceRef" select="bpmn:sourceRef/text()"/>
    <xsl:variable name="targetRef" select="bpmn:targetRef/text()"/>
    <xsl:variable name="bpmnElement" select="//bpmndi:BPMNEdge[@bpmnElement=$id]"/>

    <xsl:variable name="srcElement" select="//bpmndi:BPMNShape[@bpmnElement=$sourceRef]"/>
    <xsl:variable name="trgtRef" select="//*[@id=$targetRef]/../../@id"/>
    <xsl:variable name="trgtElement" select="//bpmndi:BPMNShape[@bpmnElement=$trgtRef]"/>

    <xsl:call-template name="flowOrAssociation">
      <xsl:with-param name="this" select="."/>
      <xsl:with-param name="id" select="$id"/>
      <xsl:with-param name="baseType">association</xsl:with-param>
      <xsl:with-param name="sourceRef" select="$sourceRef"/>
      <xsl:with-param name="targetRef" select="$targetRef"/>
      <xsl:with-param name="bpmnElement" select="$bpmnElement"/>
      <xsl:with-param name="srcElement" select="$srcElement"/>
      <xsl:with-param name="trgtElement" select="$trgtElement"/>
    </xsl:call-template>
  </xsl:template>

  <xsl:template match="bpmn:dataOutputAssociation">
    <xsl:variable name="id" select="@id"/>
    <xsl:variable name="sourceRef" select="bpmn:sourceRef/text()"/>
    <xsl:variable name="targetRef" select="bpmn:targetRef/text()"/>
    <xsl:variable name="bpmnElement" select="//bpmndi:BPMNEdge[@bpmnElement=$id]"/>

    <xsl:variable name="srcRef" select="//*[@id=$sourceRef]/../../@id"/>
    <xsl:variable name="srcElement" select="//bpmndi:BPMNShape[@bpmnElement=$srcRef]"/>
    <xsl:variable name="trgtElement" select="//bpmndi:BPMNShape[@bpmnElement=$targetRef]"/>

    <xsl:call-template name="flowOrAssociation">
      <xsl:with-param name="this" select="."/>
      <xsl:with-param name="id" select="$id"/>
      <xsl:with-param name="baseType">association</xsl:with-param>
      <xsl:with-param name="sourceRef" select="$sourceRef"/>
      <xsl:with-param name="targetRef" select="$targetRef"/>
      <xsl:with-param name="bpmnElement" select="$bpmnElement"/>
      <xsl:with-param name="srcElement" select="$srcElement"/>
      <xsl:with-param name="trgtElement" select="$trgtElement"/>
    </xsl:call-template>
  </xsl:template>
  
  <!-- EVENTS -->
  <xsl:template match="bpmn:startEvent|bpmn:boundaryEvent|bpmn:intermediateCatchEvent|bpmn:intermediateThrowEvent|bpmn:endEvent">
    <xsl:variable name="id" select="@id"/>
    <xsl:variable name="bpmnElement" select="//bpmndi:BPMNShape[@bpmnElement=$id]"/>
    
    <xsl:apply-templates select="bpmn:dataInputAssociation"/>
    <xsl:apply-templates select="bpmn:dataOutputAssociation"/>

    <xsl:element name="circle">
      <xsl:attribute name="id"><xsl:value-of select="@id"/></xsl:attribute>
      <xsl:attribute name="class">
        <xsl:text>event </xsl:text>
        <xsl:value-of select="local-name(.)"/>
      </xsl:attribute>
      <xsl:attribute name="data-type">
        <xsl:value-of select="local-name(.)"/>
      </xsl:attribute>

      <xsl:attribute name="cx">
        <xsl:value-of select="$bpmnElement/dc:Bounds/@x+($bpmnElement/dc:Bounds/@width div 2)"/>
      </xsl:attribute>
      <xsl:attribute name="cy">
        <xsl:value-of select="$bpmnElement/dc:Bounds/@y+($bpmnElement/dc:Bounds/@height div 2)"/>
      </xsl:attribute>
      <xsl:attribute name="fill">#fff</xsl:attribute>
      <xsl:attribute name="r">
        <xsl:value-of select="$bpmnElement/dc:Bounds/@width div 2"/>
      </xsl:attribute>
      <xsl:attribute name="stroke">
        <xsl:choose>
          <xsl:when test="local-name(.)='startEvent'">
            <xsl:text>#34bb16</xsl:text>
          </xsl:when>
          <xsl:when test="local-name(.)='endEvent'">
            <xsl:text>#ff0000</xsl:text>
          </xsl:when>
          <xsl:otherwise>
            <xsl:text>#666</xsl:text>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:attribute>
      <xsl:attribute name="stroke-width">
        <xsl:choose>
          <xsl:when test="local-name(.)='endEvent'">
            <xsl:text>4</xsl:text>
          </xsl:when>
	        <xsl:when test="local-name(.)='boundaryEvent' or local-name(.)='intermediateCatchEvent' or local-name(.)='intermediateThrowEvent'">
            <xsl:text>6</xsl:text>
          </xsl:when>
          <xsl:otherwise>
            <xsl:text>2</xsl:text>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:attribute>
      <xsl:if test="@cancelActivity='false'">
        <xsl:attribute name="stroke-dasharray">5,3</xsl:attribute>
      </xsl:if>
      
      <xsl:choose>
        <xsl:when test="timerEventDefinition/bpmn:timeCycle">
          <xsl:attribute name="data-timer-cycle">
            <xsl:value-of select="bpmn:timerEventDefinition/bpmn:timeCycle"/>
          </xsl:attribute>
        </xsl:when>
        <xsl:when test="bpmn:timerEventDefinition/bpmn:timeDate">
          <xsl:attribute name="data-timer-date">
            <xsl:value-of select="bpmn:timerEventDefinition/bpmn:timeDate"/>
          </xsl:attribute>
        </xsl:when>
        <xsl:when test="bpmn:timerEventDefinition/bpmn:timeDuration">
          <xsl:attribute name="data-timer-duration">
            <xsl:value-of select="bpmn:timerEventDefinition/bpmn:timeDuration"/>
          </xsl:attribute>
        </xsl:when>
      </xsl:choose>
    </xsl:element>

    <xsl:if test="local-name(.)='boundaryEvent' or local-name(.)='intermediateCatchEvent' or local-name(.)='intermediateThrowEvent'">
      <xsl:element name="circle">
        <xsl:attribute name="class">
          <xsl:value-of select="local-name(.)"/>
        </xsl:attribute>
        <xsl:attribute name="cx">
          <xsl:value-of select="$bpmnElement/dc:Bounds/@x+($bpmnElement/dc:Bounds/@width div 2)"/>
        </xsl:attribute>
        <xsl:attribute name="cy">
          <xsl:value-of select="$bpmnElement/dc:Bounds/@y+($bpmnElement/dc:Bounds/@height div 2)"/>
        </xsl:attribute>
        <xsl:attribute name="fill">none</xsl:attribute>
        <xsl:attribute name="r">
          <xsl:value-of select="$bpmnElement/dc:Bounds/@width div 2"/>
        </xsl:attribute>
        <xsl:attribute name="stroke">
          <xsl:text>#fff</xsl:text>
        </xsl:attribute>
        <xsl:attribute name="stroke-width">2</xsl:attribute>
      </xsl:element>
    </xsl:if>

    <xsl:call-template name="label">
      <xsl:with-param name="id" select="$id"></xsl:with-param>
      <xsl:with-param name="r">0</xsl:with-param>
      <xsl:with-param name="tx">0</xsl:with-param>
      <xsl:with-param name="ty">0</xsl:with-param>
    </xsl:call-template>

    <xsl:choose>
      <xsl:when test="bpmn:conditionalEventDefinition">
        <xsl:call-template name="eventIcon">
          <xsl:with-param name="class">conditional</xsl:with-param>
          <xsl:with-param name="icon" select="$conditionalEventIcon"/>
          <xsl:with-param name="id" select="$id"/>
          <xsl:with-param name="bpmnElement" select="."/>
        </xsl:call-template>
      </xsl:when>
      <xsl:when test="bpmn:errorEventDefinition">
        <xsl:call-template name="eventIcon">
          <xsl:with-param name="class">error</xsl:with-param>
          <xsl:with-param name="icon" select="$errorEventIcon"/>
          <xsl:with-param name="id" select="$id"/>
          <xsl:with-param name="bpmnElement" select="."/>
        </xsl:call-template>
      </xsl:when>
      <xsl:when test="bpmn:escalationEventDefinition">
        <xsl:call-template name="eventIcon">
          <xsl:with-param name="class">escalation</xsl:with-param>
          <xsl:with-param name="icon" select="$escalationEventIcon"/>
          <xsl:with-param name="id" select="$id"/>
          <xsl:with-param name="bpmnElement" select="."/>
        </xsl:call-template>
      </xsl:when>
      <xsl:when test="bpmn:linkEventDefinition">
        <xsl:call-template name="eventIcon">
          <xsl:with-param name="class">link</xsl:with-param>
          <xsl:with-param name="icon" select="$linkEventIcon"/>
          <xsl:with-param name="id" select="$id"/>
          <xsl:with-param name="bpmnElement" select="."/>
        </xsl:call-template>
      </xsl:when>
      <xsl:when test="(local-name(.)='intermediateThrowEvent' or local-name(.)='endEvent') and bpmn:messageEventDefinition">
        <xsl:call-template name="eventIcon">
          <xsl:with-param name="class">messageThrow</xsl:with-param>
          <xsl:with-param name="icon" select="$messageEventThrowIcon"/>
          <xsl:with-param name="id" select="$id"/>
          <xsl:with-param name="bpmnElement" select="."/>
        </xsl:call-template>
      </xsl:when>
      <xsl:when test="bpmn:messageEventDefinition">
        <xsl:call-template name="eventIcon">
          <xsl:with-param name="class">message</xsl:with-param>
          <xsl:with-param name="icon" select="$messageEventCatchIcon"/>
          <xsl:with-param name="id" select="$id"/>
          <xsl:with-param name="bpmnElement" select="."/>
        </xsl:call-template>
      </xsl:when>
      <xsl:when test="(local-name(.)='intermediateThrowEvent' or local-name(.)='endEvent') and bpmn:signalEventDefinition">
        <xsl:call-template name="eventIcon">
          <xsl:with-param name="class">signal</xsl:with-param>
          <xsl:with-param name="icon" select="$signalEventThrowIcon"/>
          <xsl:with-param name="id" select="$id"/>
          <xsl:with-param name="bpmnElement" select="."/>
        </xsl:call-template>
      </xsl:when>
      <xsl:when test="bpmn:signalEventDefinition">
        <xsl:call-template name="eventIcon">
          <xsl:with-param name="class">signal</xsl:with-param>
          <xsl:with-param name="icon" select="$signalEventCatchIcon"/>
          <xsl:with-param name="id" select="$id"/>
          <xsl:with-param name="bpmnElement" select="."/>
        </xsl:call-template>
      </xsl:when>
      <xsl:when test="bpmn:timerEventDefinition">
        <xsl:call-template name="eventIcon">
          <xsl:with-param name="class">timer</xsl:with-param>
          <xsl:with-param name="icon" select="$timerEventIcon"/>
          <xsl:with-param name="id" select="$id"/>
          <xsl:with-param name="bpmnElement" select="."/>
        </xsl:call-template>
      </xsl:when>
    </xsl:choose>
  </xsl:template>

  <!-- FLOW OBJECTS -->
  <xsl:template match="bpmn:messageFlow|bpmn:sequenceFlow">
    <xsl:variable name="id" select="@id"/>
    <xsl:variable name="sourceRef" select="@sourceRef"/>
    <xsl:variable name="targetRef" select="@targetRef"/>
    <xsl:variable name="bpmnElement" select="//bpmndi:BPMNEdge[@bpmnElement=$id]"/>

    <xsl:variable name="srcElement" select="//bpmndi:BPMNShape[@bpmnElement=$sourceRef]"/>
    <xsl:variable name="trgtElement" select="//bpmndi:BPMNShape[@bpmnElement=$targetRef]"/>

    <xsl:call-template name="flowOrAssociation">
      <xsl:with-param name="this" select="."/>
      <xsl:with-param name="id" select="$id"/>
      <xsl:with-param name="baseType">flow</xsl:with-param>
      <xsl:with-param name="sourceRef" select="$sourceRef"/>
      <xsl:with-param name="targetRef" select="$targetRef"/>
      <xsl:with-param name="bpmnElement" select="$bpmnElement"/>
      <xsl:with-param name="srcElement" select="$srcElement"/>
      <xsl:with-param name="trgtElement" select="$trgtElement"/>
    </xsl:call-template>

    <xsl:call-template name="label">
      <xsl:with-param name="id" select="$id"></xsl:with-param>
      <xsl:with-param name="r">0</xsl:with-param>
      <xsl:with-param name="tx">0</xsl:with-param>
      <xsl:with-param name="ty">0</xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <!-- GATEWAYS -->
  <xsl:template match="bpmn:complexGateway|bpmn:eventBasedGateway|bpmn:exclusiveGateway|bpmn:inclusiveGateway|bpmn:parallelGateway">
    <xsl:variable name="id" select="@id"/>
    <xsl:variable name="bpmnElement" select="//bpmndi:BPMNShape[@bpmnElement=$id]"/>

    <xsl:element name="path">
      <xsl:attribute name="id"><xsl:value-of select="$id"/></xsl:attribute>
      <xsl:attribute name="class">
        <xsl:text>gateway </xsl:text>
        <xsl:value-of select="local-name(.)"/>
      </xsl:attribute>
      <xsl:attribute name="data-type">
        <xsl:value-of select="local-name(.)"/>
      </xsl:attribute>
      <xsl:attribute name="d">
        <xsl:text>M </xsl:text>
        <xsl:value-of select="$bpmnElement/dc:Bounds/@x"/>
        <xsl:text> </xsl:text>
        <xsl:value-of select="$bpmnElement/dc:Bounds/@y+($bpmnElement/dc:Bounds/@height div 2)"/>
        <xsl:text> l </xsl:text>
        <xsl:value-of select="$bpmnElement/dc:Bounds/@width div 2"/>
	      <xsl:text>,-</xsl:text>
        <xsl:value-of select="$bpmnElement/dc:Bounds/@height div 2"/>
        <xsl:text> l </xsl:text>
        <xsl:value-of select="$bpmnElement/dc:Bounds/@width div 2"/>
	      <xsl:text>,</xsl:text>
        <xsl:value-of select="$bpmnElement/dc:Bounds/@height div 2"/>
        <xsl:text> l -</xsl:text>
        <xsl:value-of select="$bpmnElement/dc:Bounds/@width div 2"/>
	      <xsl:text>,</xsl:text>
        <xsl:value-of select="$bpmnElement/dc:Bounds/@height div 2"/>
        <xsl:text> l -</xsl:text>
        <xsl:value-of select="$bpmnElement/dc:Bounds/@width div 2"/>
	      <xsl:text>,-</xsl:text>
        <xsl:value-of select="$bpmnElement/dc:Bounds/@height div 2"/>
        <xsl:text> Z</xsl:text>
      </xsl:attribute>
      <xsl:attribute name="fill">#fff</xsl:attribute>
      <xsl:attribute name="stroke">#666</xsl:attribute>
      <xsl:attribute name="stroke-width">1.0</xsl:attribute>
      <xsl:if test="bpmn:documentation">
        <title><xsl:value-of select="bpmn:documentation/text()"/></title>
      </xsl:if>
    </xsl:element>

    <xsl:call-template name="label">
      <xsl:with-param name="id" select="$id"></xsl:with-param>
      <xsl:with-param name="r">0</xsl:with-param>
      <xsl:with-param name="tx">0</xsl:with-param>
      <xsl:with-param name="ty">0</xsl:with-param>
    </xsl:call-template>

    <xsl:if test="$debug='true'">
	    <xsl:comment>
	      <xsl:text>HEIGHT: </xsl:text>
	      <xsl:value-of select="//bpmndi:BPMNShape[@bpmnElement=$id]/dc:Bounds/@height"/>
	    </xsl:comment>
	  </xsl:if>
    <xsl:variable name="s">
      <xsl:choose>
        <xsl:when test="number(//bpmndi:BPMNShape[@bpmnElement=$id]/dc:Bounds/@height) &gt;= 48">
          <xsl:value-of select="0.024"/>
        </xsl:when>
        <xsl:when test="number(//bpmndi:BPMNShape[@bpmnElement=$id]/dc:Bounds/@height) &gt;= 42">
          <xsl:value-of select="0.021"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="0.019"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    
    <xsl:element name="g">
      <xsl:attribute name="class"><xsl:value-of select="local-name(.)"/></xsl:attribute>
      <xsl:attribute name="id"><xsl:value-of select="@id"/></xsl:attribute>
      <xsl:attribute name="transform">translate(<xsl:value-of select="//bpmndi:BPMNShape[@bpmnElement=$id]/dc:Bounds/@x"/>,<xsl:value-of select="//bpmndi:BPMNShape[@bpmnElement=$id]/dc:Bounds/@y"/>) scale(<xsl:value-of select="$s"/>)</xsl:attribute>
      <xsl:element name="path">
        <xsl:attribute name="d">
          <xsl:choose>
			      <xsl:when test="local-name(.)='complexGateway'">
              <xsl:value-of select="$complexGatewayIcon"/>
            </xsl:when>
            <xsl:when test="local-name(.)='exclusiveGateway' and //bpmndi:BPMNShape[@bpmnElement=$id]/@isMarkerVisible='true'">
		          <xsl:value-of select="$exclusiveGatewayIcon"/>
			      </xsl:when>
			      <xsl:when test="local-name(.)='eventBasedGateway'">
			        <xsl:value-of select="$eventBasedGatewayIcon"/>
			      </xsl:when>
			      <xsl:when test="local-name(.)='inclusiveGateway'">
			        <xsl:value-of select="$inclusiveGatewayIcon"/>
			      </xsl:when>
			      <xsl:when test="local-name(.)='parallelGateway'">
			        <xsl:value-of select="$parallelGatewayIcon"/>
			      </xsl:when>
			    </xsl:choose>
        </xsl:attribute>
      </xsl:element>
    </xsl:element>
  </xsl:template>
  
  <xsl:template match="bpmn:participant">
    <xsl:variable name="id" select="@id"/>

    <xsl:element name="rect">
      <xsl:attribute name="class">participant</xsl:attribute>
      <xsl:attribute name="data-name"><xsl:value-of select="@name"/></xsl:attribute>
      <xsl:attribute name="data-type"><xsl:value-of select="local-name(.)"/></xsl:attribute>
      <xsl:attribute name="fill">white</xsl:attribute>
      <xsl:attribute name="id"><xsl:value-of select="$id"/></xsl:attribute>
      <xsl:attribute name="height">
        <xsl:value-of select="//bpmndi:BPMNShape[@bpmnElement=$id]/dc:Bounds/@height"/>
      </xsl:attribute>
      <xsl:attribute name="stroke">#000</xsl:attribute>
      <xsl:attribute name="width">
        <xsl:value-of select="//bpmndi:BPMNShape[@bpmnElement=$id]/dc:Bounds/@width"/>
      </xsl:attribute>
      <xsl:attribute name="x">
        <xsl:value-of select="//bpmndi:BPMNShape[@bpmnElement=$id]/dc:Bounds/@x"/>
      </xsl:attribute>
      <xsl:attribute name="y">
        <xsl:value-of select="//bpmndi:BPMNShape[@bpmnElement=$id]/dc:Bounds/@y"/>
      </xsl:attribute>
    </xsl:element>

		<xsl:element name="line">
		  <xsl:attribute name="class">participantBorder</xsl:attribute>
      <xsl:attribute name="x1">
        <xsl:value-of select="//bpmndi:BPMNShape[@bpmnElement=$id]/dc:Bounds/@x+(2*($fontSize+$lineSpacing))"/>
      </xsl:attribute>
      <xsl:attribute name="stroke">#000</xsl:attribute>
      <xsl:attribute name="y1">
        <xsl:value-of select="//bpmndi:BPMNShape[@bpmnElement=$id]/dc:Bounds/@y"/>
      </xsl:attribute>
      <xsl:attribute name="x2">
        <xsl:value-of select="//bpmndi:BPMNShape[@bpmnElement=$id]/dc:Bounds/@x+(2*($fontSize+$lineSpacing))"/>
      </xsl:attribute>
      <xsl:attribute name="y2">
        <xsl:value-of select="//bpmndi:BPMNShape[@bpmnElement=$id]/dc:Bounds/@y+//bpmndi:BPMNShape[@bpmnElement=$id]/dc:Bounds/@height"/>
      </xsl:attribute>
    </xsl:element>

    <xsl:if test="$debug='true'">
	    <xsl:comment>
	      <xsl:text>Participant dimensions (x,y,height,width): </xsl:text>
	      <xsl:value-of select="//bpmndi:BPMNShape[@bpmnElement=$id]/dc:Bounds/@x"/>,
	      <xsl:value-of select="//bpmndi:BPMNShape[@bpmnElement=$id]/dc:Bounds/@y"/>_
	      <xsl:value-of select="//bpmndi:BPMNShape[@bpmnElement=$id]/dc:Bounds/@height"/>_
	      <xsl:value-of select="//bpmndi:BPMNShape[@bpmnElement=$id]/dc:Bounds/@width"/>
	    </xsl:comment>
	    <xsl:comment>
	      <xsl:text>Participant label dimensions (x,y,height,width): </xsl:text>
	      <xsl:value-of select="//bpmndi:BPMNShape[@bpmnElement=$id]/bpmndi:BPMNLabel/dc:Bounds/@x"/>,
	      <xsl:value-of select="//bpmndi:BPMNShape[@bpmnElement=$id]/bpmndi:BPMNLabel/dc:Bounds/@y"/>_
	      <xsl:value-of select="//bpmndi:BPMNShape[@bpmnElement=$id]/bpmndi:BPMNLabel/dc:Bounds/@height"/>_
	      <xsl:value-of select="//bpmndi:BPMNShape[@bpmnElement=$id]/bpmndi:BPMNLabel/dc:Bounds/@width"/>
	    </xsl:comment>
	  </xsl:if>

    <xsl:call-template name="label">
      <xsl:with-param name="id" select="$id"/>
      <xsl:with-param name="r">
	      <xsl:text>-90</xsl:text>
      </xsl:with-param>
			<xsl:with-param name="tx">
			  <xsl:choose>
			    <xsl:when test="//bpmndi:BPMNShape[@bpmnElement=$id]/bpmndi:BPMNLabel/dc:Bounds">
            <xsl:value-of select="-//bpmndi:BPMNShape[@bpmnElement=$id]/bpmndi:BPMNLabel/dc:Bounds/@height"/>
			    </xsl:when>
			    <xsl:otherwise>
			      <xsl:value-of select="-//bpmndi:BPMNShape[@bpmnElement=$id]/bpmndi:BPMNLabel/@height"/>
			    </xsl:otherwise>
			  </xsl:choose>
			</xsl:with-param>
			<xsl:with-param name="ty">0</xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <xsl:template match="bpmn:lane">
    <xsl:variable name="id" select="@id"/>

    <xsl:element name="rect">
      <xsl:attribute name="class"><xsl:if test="$showBounds='true'">bounds </xsl:if>lane</xsl:attribute>
      <xsl:attribute name="data-name"><xsl:value-of select="@name"/></xsl:attribute>
      <xsl:attribute name="data-type"><xsl:value-of select="local-name(.)"/></xsl:attribute>
      <xsl:attribute name="fill">
        <xsl:choose>
          <xsl:when test="$showBounds='true'">#5F5</xsl:when>
          <xsl:otherwise>white</xsl:otherwise>
        </xsl:choose>
      </xsl:attribute>
      <xsl:attribute name="id"><xsl:value-of select="$id"/></xsl:attribute>
      <xsl:attribute name="height">
        <xsl:value-of select="//bpmndi:BPMNShape[@bpmnElement=$id]/dc:Bounds/@height"/>
      </xsl:attribute>
      <!--xsl:attribute name="stroke">#666</xsl:attribute-->
      <xsl:attribute name="width">
        <xsl:value-of select="//bpmndi:BPMNShape[@bpmnElement=$id]/dc:Bounds/@width"/>
      </xsl:attribute>
      <xsl:attribute name="x">
        <xsl:value-of select="//bpmndi:BPMNShape[@bpmnElement=$id]/dc:Bounds/@x"/>
      </xsl:attribute>
      <xsl:attribute name="y">
        <xsl:value-of select="//bpmndi:BPMNShape[@bpmnElement=$id]/dc:Bounds/@y"/>
      </xsl:attribute>
    </xsl:element>

    <xsl:if test="$debug='true'">
      <xsl:comment>
        Lane Height: <xsl:value-of select="//bpmndi:BPMNShape[@bpmnElement=$id]/dc:Bounds/@x"/>
        Lane Y: <xsl:value-of select="//bpmndi:BPMNShape[@bpmnElement=$id]/dc:Bounds/@y"/>
        Lane Height: <xsl:value-of select="//bpmndi:BPMNShape[@bpmnElement=$id]/dc:Bounds/@width"/>
        Lane Height: <xsl:value-of select="//bpmndi:BPMNShape[@bpmnElement=$id]/dc:Bounds/@height"/>
        Label X: <xsl:value-of select="-//bpmndi:BPMNShape[@bpmnElement=$id]/bpmndi:BPMNLabel/dc:Bounds/@x"/>
        Label Y: <xsl:value-of select="-//bpmndi:BPMNShape[@bpmnElement=$id]/bpmndi:BPMNLabel/dc:Bounds/@y"/>
        Label Width: <xsl:value-of select="//bpmndi:BPMNShape[@bpmnElement=$id]/bpmndi:BPMNLabel/dc:Bounds/@width"/>
        Label Height: <xsl:value-of select="//bpmndi:BPMNShape[@bpmnElement=$id]/bpmndi:BPMNLabel/dc:Bounds/@height"/>
      </xsl:comment>
    </xsl:if>

    <xsl:call-template name="label">
      <xsl:with-param name="id" select="$id"/>
      <xsl:with-param name="r">
        <xsl:text>-90</xsl:text>
      </xsl:with-param>
			<xsl:with-param name="tx">
			  <xsl:value-of select="-//bpmndi:BPMNShape[@bpmnElement=$id]/bpmndi:BPMNLabel/dc:Bounds/@height"/>
			</xsl:with-param>
			<xsl:with-param name="ty">
        <xsl:value-of select="-//bpmndi:BPMNShape[@bpmnElement=$id]/bpmndi:BPMNLabel/dc:Bounds/@height"/>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <!-- TASKS -->
  <xsl:template match="bpmn:callActivity|bpmn:subProcess|bpmn:businessRuleTask|bpmn:manualTask|bpmn:receiveTask|bpmn:scriptTask|bpmn:sendTask|bpmn:serviceTask|bpmn:task|bpmn:userTask">
    <xsl:variable name="id" select="@id"/>
    <xsl:variable name="bpmnElement" select="//bpmndi:BPMNShape[@bpmnElement=$id]"/>

    <xsl:apply-templates select="bpmn:dataInputAssociation"/>
    <xsl:apply-templates select="bpmn:dataOutputAssociation"/>

    <xsl:element name="g">
      <xsl:if test="bpmn:documentation">
        <title><xsl:value-of select="bpmn:documentation/text()"/></title>
      </xsl:if>
	    <xsl:element name="rect">
	      <xsl:attribute name="id"><xsl:value-of select="@id"/></xsl:attribute>
	      <xsl:attribute name="class">task <xsl:value-of select="local-name(.)"/></xsl:attribute>
	      <xsl:attribute name="data-name"><xsl:value-of select="@name"/></xsl:attribute>
	      <xsl:if test="local-name(.)='userTask'">
		      <xsl:choose>
            <xsl:when test="@activiti:candidateGroups">
              <xsl:attribute name="data-resource"><xsl:value-of select="@activiti:candidateGroups"/></xsl:attribute>
            </xsl:when>
			      <xsl:when test="@activiti:candidateUsers">
			        <xsl:attribute name="data-resource"><xsl:value-of select="@activiti:candidateUsers"/></xsl:attribute>
			      </xsl:when>
	          <xsl:when test="bpmn:potentialOwner/bpmn:resourceAssignmentExpression/bpmn:formalExpression/text()">
	            <xsl:attribute name="data-resource">
	              <xsl:value-of select="bpmn:potentialOwner/bpmn:resourceAssignmentExpression/bpmn:formalExpression/text()"/>
	            </xsl:attribute>
	          </xsl:when>
			      <xsl:otherwise>
			        <xsl:attribute name="data-resource">n/a</xsl:attribute>
			      </xsl:otherwise>
			    </xsl:choose>
			  </xsl:if>
			  <xsl:if test="bpmn:script">
          <xsl:attribute name="data-script"><xsl:value-of select="bpmn:script"/></xsl:attribute>
        </xsl:if>
	      <xsl:if test="script">
          <xsl:attribute name="data-script"><xsl:value-of select="script"/></xsl:attribute>
        </xsl:if>
        <xsl:if test="@activiti:class">
	        <xsl:attribute name="data-service-type"><xsl:value-of select="@activiti:class"/></xsl:attribute>
	      </xsl:if>
	      <xsl:attribute name="data-type"><xsl:value-of select="local-name(.)"/></xsl:attribute>
        <xsl:attribute name="stroke">#000</xsl:attribute>
	      <xsl:choose>
		      <xsl:when test="local-name(.)='callActivity'">
		        <xsl:attribute name="stroke-width">3px</xsl:attribute>
		        <xsl:attribute name="data-called-element"><xsl:value-of select="@calledElement"/></xsl:attribute>
		      </xsl:when>
		      <xsl:otherwise>
            <xsl:attribute name="stroke-width">1px</xsl:attribute>
		      </xsl:otherwise>
		    </xsl:choose>
        <xsl:choose>
          <xsl:when test="(local-name(.)='subProcess' or local-name(.)='callActivity') and (//bpmndi:BPMNShape[@bpmnElement=$id]/@isExpanded='true')">
            <xsl:attribute name="fill">white</xsl:attribute>
          </xsl:when>
          <xsl:otherwise>
            <xsl:attribute name="fill">#0f98ce</xsl:attribute>
          </xsl:otherwise>
        </xsl:choose>
	      <xsl:attribute name="height">
	        <xsl:value-of select="//bpmndi:BPMNShape[@bpmnElement=$id]/dc:Bounds/@height"/>
	      </xsl:attribute>
	      <xsl:attribute name="rx">5</xsl:attribute>
	      <xsl:attribute name="ry">5</xsl:attribute>
	      <xsl:attribute name="width">
	        <xsl:value-of select="//bpmndi:BPMNShape[@bpmnElement=$id]/dc:Bounds/@width"/>
	      </xsl:attribute>
	      <xsl:attribute name="x">
	        <xsl:value-of select="//bpmndi:BPMNShape[@bpmnElement=$id]/dc:Bounds/@x"/>
	      </xsl:attribute>
	      <xsl:attribute name="y">
	        <xsl:value-of select="//bpmndi:BPMNShape[@bpmnElement=$id]/dc:Bounds/@y"/>
	      </xsl:attribute>
	    </xsl:element>

      <xsl:element name="path">
        <xsl:attribute name="class">icon <xsl:value-of select="local-name(.)"/></xsl:attribute>
        <xsl:attribute name="data-called-element"><xsl:value-of select="@calledElement"/></xsl:attribute>
        
        <xsl:choose>
          <xsl:when test="//bpmndi:BPMNShape[@bpmnElement=$id]/@isExpanded!='true' and (local-name(.)='callActivity' or local-name(.)='subProcess')">
            <xsl:attribute name="transform">translate(<xsl:value-of select="number(//bpmndi:BPMNShape[@bpmnElement=$id]/dc:Bounds/@x)+number(//bpmndi:BPMNShape[@bpmnElement=$id]/dc:Bounds/@width div 2)-10"/>,<xsl:value-of select="number(//bpmndi:BPMNShape[@bpmnElement=$id]/dc:Bounds/@y)+number(//bpmndi:BPMNShape[@bpmnElement=$id]/dc:Bounds/@height)-20"/>) scale(0.01)</xsl:attribute>
            <xsl:attribute name="d"><xsl:value-of select="$subProcessMarker"/></xsl:attribute>
          </xsl:when>

          <xsl:when test="local-name(.)='businessRuleTask'">
            <xsl:attribute name="transform">translate(<xsl:value-of select="//bpmndi:BPMNShape[@bpmnElement=$id]/dc:Bounds/@x"/>,<xsl:value-of select="//bpmndi:BPMNShape[@bpmnElement=$id]/dc:Bounds/@y"/>) scale(0.02)</xsl:attribute>
            <xsl:attribute name="d"><xsl:value-of select="$businessRuleTaskIcon"/></xsl:attribute>
          </xsl:when>
          <xsl:when test="local-name(.)='manualTask'">
            <xsl:attribute name="transform">translate(<xsl:value-of select="//bpmndi:BPMNShape[@bpmnElement=$id]/dc:Bounds/@x"/>,<xsl:value-of select="//bpmndi:BPMNShape[@bpmnElement=$id]/dc:Bounds/@y"/>) scale(0.02)</xsl:attribute>
            <xsl:attribute name="d"><xsl:value-of select="$manualTaskIcon"/></xsl:attribute>
          </xsl:when>
          <xsl:when test="local-name(.)='receiveTask'">
            <xsl:attribute name="transform">translate(<xsl:value-of select="//bpmndi:BPMNShape[@bpmnElement=$id]/dc:Bounds/@x"/>,<xsl:value-of select="//bpmndi:BPMNShape[@bpmnElement=$id]/dc:Bounds/@y"/>) scale(0.02)</xsl:attribute>
            <xsl:attribute name="d"><xsl:value-of select="$receiveTaskIcon"/></xsl:attribute>
          </xsl:when>
          <xsl:when test="local-name(.)='scriptTask'">
            <xsl:attribute name="transform">translate(<xsl:value-of select="number(//bpmndi:BPMNShape[@bpmnElement=$id]/dc:Bounds/@x)-6"/>,<xsl:value-of select="number(//bpmndi:BPMNShape[@bpmnElement=$id]/dc:Bounds/@y)-8"/>) scale(0.025)</xsl:attribute>
            <xsl:attribute name="d"><xsl:value-of select="$scriptTaskIcon"/></xsl:attribute>
          </xsl:when>
          <xsl:when test="local-name(.)='sendTask'">
            <xsl:attribute name="transform">translate(<xsl:value-of select="//bpmndi:BPMNShape[@bpmnElement=$id]/dc:Bounds/@x"/>,<xsl:value-of select="//bpmndi:BPMNShape[@bpmnElement=$id]/dc:Bounds/@y"/>) scale(0.02)</xsl:attribute>
            <xsl:attribute name="d"><xsl:value-of select="$sendTaskIcon"/></xsl:attribute>
          </xsl:when>
          <xsl:when test="local-name(.)='serviceTask'">
            <xsl:attribute name="transform">translate(<xsl:value-of select="//bpmndi:BPMNShape[@bpmnElement=$id]/dc:Bounds/@x"/>,<xsl:value-of select="number(//bpmndi:BPMNShape[@bpmnElement=$id]/dc:Bounds/@y)-5"/>) scale(0.025)</xsl:attribute>
            <xsl:attribute name="d"><xsl:value-of select="$serviceTaskIcon"/></xsl:attribute>
          </xsl:when>
          <xsl:when test="local-name(.)='userTask'">
            <xsl:attribute name="transform">translate(<xsl:value-of select="number(//bpmndi:BPMNShape[@bpmnElement=$id]/dc:Bounds/@x)-5"/>,<xsl:value-of select="number(//bpmndi:BPMNShape[@bpmnElement=$id]/dc:Bounds/@y)-5"/>) scale(0.02)</xsl:attribute>
            <xsl:attribute name="d"><xsl:value-of select="$userTaskIcon"/></xsl:attribute>
          </xsl:when>
        </xsl:choose>
      </xsl:element>
    
	    <xsl:call-template name="label">
	      <xsl:with-param name="id" select="$id"/>
	      <xsl:with-param name="tx">0</xsl:with-param>
	      <xsl:with-param name="ty">0</xsl:with-param>
	      <xsl:with-param name="r">0</xsl:with-param>
	      <xsl:with-param name="fill">#fff</xsl:with-param>
	    </xsl:call-template>

      <!-- Activity markers -->
      <xsl:if test="$debug='true'">
	      <xsl:comment>
	        <xsl:text> x </xsl:text><xsl:value-of select="//bpmndi:BPMNShape[@bpmnElement=$id]/dc:Bounds/@x"></xsl:value-of>
	        <xsl:text> y </xsl:text><xsl:value-of select="//bpmndi:BPMNShape[@bpmnElement=$id]/dc:Bounds/@y"></xsl:value-of>
	        <xsl:text> height </xsl:text><xsl:value-of select="//bpmndi:BPMNShape[@bpmnElement=$id]/dc:Bounds/@height"></xsl:value-of>
	        <xsl:text> width </xsl:text><xsl:value-of select="//bpmndi:BPMNShape[@bpmnElement=$id]/dc:Bounds/@width"></xsl:value-of>
	      </xsl:comment>
	    </xsl:if>
      <xsl:call-template name="activityMarker">
        <xsl:with-param name="this" select="."/>
        <xsl:with-param name="id" select="$id"/>
        <xsl:with-param name="yOffset">
		      <xsl:choose>
		        <xsl:when test="//bpmndi:BPMNShape[@bpmnElement=$id]/dc:Bounds/@height &gt; 70">-10</xsl:when>
		        <xsl:otherwise>-20</xsl:otherwise>
		      </xsl:choose>
		    </xsl:with-param>
		  </xsl:call-template>
    </xsl:element>

    <!--<xsl:for-each select="bpmn:outgoing">
      <xsl:variable name="edgeId" select="@id"/>
      <xsl:variable name="bpmnEdge" select="//bpmndi:BPMNEdge[@bpmnElement=$edgeId]"/>
      <xsl:element name="path">
        <xsl:attribute name="class">sequenceFlow</xsl:attribute>
        <xsl:attribute name="d">
          <xsl:text>M </xsl:text>
          <xsl:value-of select="$bpmnElement/dc:Bounds/@x"/>
          <xsl:text> </xsl:text>
          <xsl:value-of select="$bpmnElement/dc:Bounds/@y"/>
          <!- - TODO - ->
          <xsl:for-each select="$bpmnEdge/di:waypoint">
            <xsl:text> L </xsl:text>
            <xsl:value-of select="@x"/>
            <xsl:text>,</xsl-text>
            <xsl:value-of select="@y"/>
            <xsl:text> </xsl-text>
          </xsl:for-each>
        </xsl:attribute>
        <xsl:attribute name="stroke">#666</xsl:attribute>
        <xsl:attribute name="stroke-width">1.0</xsl:attribute>
      </xsl:element>
    </xsl:for-each>-->
    
    <xsl:element name="rect">
        <xsl:attribute name="id"><xsl:value-of select="@id"/>IssueBG</xsl:attribute>
        <xsl:attribute name="class">issue</xsl:attribute>
        <xsl:attribute name="fill">yellow</xsl:attribute>
        <xsl:attribute name="height">24</xsl:attribute>
        <xsl:attribute name="width">24</xsl:attribute>
        <xsl:attribute name="rx">2</xsl:attribute>
        <xsl:attribute name="ry">2</xsl:attribute>
        <xsl:attribute name="visibility">hidden</xsl:attribute>
        <xsl:attribute name="x"><xsl:value-of select="number(//bpmndi:BPMNShape[@bpmnElement=$id]/dc:Bounds/@x)-12"/></xsl:attribute>
        <xsl:attribute name="y"><xsl:value-of select="number(//bpmndi:BPMNShape[@bpmnElement=$id]/dc:Bounds/@y)-12"/></xsl:attribute>
      </xsl:element>
    <xsl:element name="g">
      <xsl:attribute name="id"><xsl:value-of select="@id"/>Issue</xsl:attribute>
      <xsl:attribute name="class">issue</xsl:attribute>
      <xsl:attribute name="transform">translate(<xsl:value-of select="number(//bpmndi:BPMNShape[@bpmnElement=$id]/dc:Bounds/@x)-10"/>,<xsl:value-of select="number(//bpmndi:BPMNShape[@bpmnElement=$id]/dc:Bounds/@y)-10"/>) scale(0.04)</xsl:attribute>
      <xsl:attribute name="visibility">hidden</xsl:attribute>
      <path fill="#ff6c06" d="M256 46.387l214.551 427.613h-429.103l214.552-427.613zM256 0c-11.035 0-22.070 7.441-30.442 22.324l-218.537 435.556c-16.743 29.766-2.5 54.12 31.652 54.12h434.654c34.15 0 48.396-24.354 31.65-54.12h0.001l-218.537-435.556c-8.371-14.883-19.406-22.324-30.441-22.324v0z"></path>
			<path fill="#ff6c06" d="M288 416c0 17.673-14.327 32-32 32s-32-14.327-32-32c0-17.673 14.327-32 32-32s32 14.327 32 32z"></path>
			<path fill="#ff6c06" d="M256 352c-17.673 0-32-14.327-32-32v-96c0-17.673 14.327-32 32-32s32 14.327 32 32v96c0 17.673-14.327 32-32 32z"></path>
			
    </xsl:element>

  </xsl:template>

  <!-- TEXT ANNOTATIONS -->
  <xsl:template match="bpmn:association">
    <xsl:variable name="id" select="@id"/>
    <xsl:variable name="sourceRef" select="@sourceRef"/>
    <xsl:variable name="targetRef" select="@targetRef"/>
    <xsl:variable name="bpmnElement" select="//bpmndi:BPMNEdge[@bpmnElement=$id]"/>

    <xsl:variable name="srcElement" select="//bpmndi:BPMNShape[@bpmnElement=$sourceRef]"/>
    <xsl:variable name="trgtRef" select="//*[@id=$targetRef]/../../@id"/>
    <xsl:variable name="trgtElement" select="//bpmndi:BPMNShape[@bpmnElement=$trgtRef]"/>

    <xsl:call-template name="flowOrAssociation">
      <xsl:with-param name="this" select="."/>
      <xsl:with-param name="id" select="$id"/>
      <xsl:with-param name="baseType">association</xsl:with-param>
      <xsl:with-param name="sourceRef" select="$sourceRef"/>
      <xsl:with-param name="targetRef" select="$targetRef"/>
      <xsl:with-param name="bpmnElement" select="$bpmnElement"/>
      <xsl:with-param name="srcElement" select="$srcElement"/>
      <xsl:with-param name="trgtElement" select="$trgtElement"/>
    </xsl:call-template>
  </xsl:template>

  <xsl:template match="bpmn:textAnnotation">
    <xsl:variable name="id" select="@id"/>
    <xsl:variable name="bpmnElement" select="//bpmndi:BPMNShape[@bpmnElement=$id]"/>

    <xsl:element name="path">
      <xsl:attribute name="id"><xsl:value-of select="@id"/></xsl:attribute>
      <xsl:attribute name="class">text-annotation</xsl:attribute>
      <xsl:attribute name="d">
        <xsl:text>M </xsl:text>
        <xsl:value-of select="$bpmnElement/dc:Bounds/@x+20"/>
        <xsl:text> </xsl:text>
        <xsl:value-of select="$bpmnElement/dc:Bounds/@y"/>
        <xsl:text> l -20,0</xsl:text>
        <xsl:text> l 0,</xsl:text>
        <xsl:value-of select="$bpmnElement/dc:Bounds/@height"/>
        <xsl:text> l 20,0</xsl:text>
      </xsl:attribute>
      <xsl:attribute name="fill">none</xsl:attribute>
      <xsl:attribute name="stroke">#666</xsl:attribute>
      <xsl:attribute name="stroke-width">1.0</xsl:attribute>
    </xsl:element>

    <xsl:element name="rect">
      <xsl:attribute name="id"><xsl:value-of select="@id"/></xsl:attribute>
      <xsl:attribute name="class">text-annotation</xsl:attribute>
      <xsl:attribute name="fill">#eee</xsl:attribute>
      <xsl:attribute name="height"><xsl:value-of select="$bpmnElement/dc:Bounds/@height"/></xsl:attribute>
      <xsl:attribute name="width"><xsl:value-of select="$bpmnElement/dc:Bounds/@width"/></xsl:attribute>
      <xsl:attribute name="x"><xsl:value-of select="$bpmnElement/dc:Bounds/@x"/></xsl:attribute>
      <xsl:attribute name="y"><xsl:value-of select="$bpmnElement/dc:Bounds/@y"/></xsl:attribute>
    </xsl:element>

    <xsl:call-template name="multiLineText">
      <xsl:with-param name="text" select="bpmn:text/text()"></xsl:with-param>
      <xsl:with-param name="class">textAnnotation</xsl:with-param>
      <xsl:with-param name="id" select="$id"/>
      <xsl:with-param name="diElement" select="//bpmndi:BPMNShape[@bpmnElement=$id]"/>
      <xsl:with-param name="x">
        <xsl:value-of select="//bpmndi:BPMNShape[@bpmnElement=$id]/dc:Bounds/@x+($fontSize div 2)"/>
      </xsl:with-param>
      <xsl:with-param name="y">
        <xsl:value-of select="//bpmndi:BPMNShape[@bpmnElement=$id]/dc:Bounds/@y+$fontSize+($fontSize div 2)"/>
      </xsl:with-param>
    </xsl:call-template>

    <xsl:apply-templates />
  </xsl:template>


  <xsl:template match="bpmndi:BPMNShape">
    <xsl:apply-templates select="//dc:Bounds"/>

    <ellipse cx="17" cy="139" fill="url(#g1)" rx="16" ry="16" stroke="#000000" stroke-width="1.0"/>
    
    <text fill="#fff" font-decoration="none" font-size="11" font-weight="normal" text-anchor="start" x="202" y="224">
      <xsl:variable name="bpmnElement" select="@bpmnElement"/>
      <xsl:value-of select="$bpmnElement"/>
    </text>

    <xsl:choose>
      <xsl:when test="1=1">

      </xsl:when>

    </xsl:choose>
  </xsl:template>


  <xsl:template match="bpmndi:BPMNLabel">
    <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="dc:Bounds">
    <xsl:param name="fill">#ccc</xsl:param>

	 <xsl:if test="$showBounds='true'">
	    <xsl:element name="rect">
	      <xsl:attribute name="id"><xsl:value-of select="@id"/></xsl:attribute>
	      <xsl:attribute name="class">bounds</xsl:attribute>
	      <xsl:attribute name="fill"><xsl:value-of select="$fill"/></xsl:attribute>
	      <xsl:attribute name="height"><xsl:value-of select="@height"/></xsl:attribute>
	      <xsl:attribute name="width"><xsl:value-of select="@width"/></xsl:attribute>
	      <xsl:attribute name="x"><xsl:value-of select="@x"/></xsl:attribute>
	      <xsl:attribute name="y"><xsl:value-of select="@y"/></xsl:attribute>
	    </xsl:element>
    </xsl:if>
  </xsl:template>

  <!-- NAMED TEMPLATES -->

  <xsl:template name="activityMarker">
    <xsl:param name="this"/>
    <xsl:param name="id"/>
    <xsl:param name="yOffset"/>

    <xsl:choose>
      <xsl:when test="bpmn:standardLoopCharacteristics">
        <xsl:element name="path">
          <xsl:attribute name="class">icon <xsl:value-of select="local-name($this)"/></xsl:attribute>
          <xsl:attribute name="transform">translate(<xsl:value-of select="number(//bpmndi:BPMNShape[@bpmnElement=$id]/dc:Bounds/@x)+number(//bpmndi:BPMNShape[@bpmnElement=$id]/dc:Bounds/@width div 2)-10"/>,<xsl:value-of select="number(//bpmndi:BPMNShape[@bpmnElement=$id]/dc:Bounds/@y)+number(//bpmndi:BPMNShape[@bpmnElement=$id]/dc:Bounds/@height)+$yOffset"/>) scale(0.01)</xsl:attribute>
          <xsl:attribute name="d"><xsl:value-of select="$standardLoopMarker"/></xsl:attribute>
        </xsl:element>
      </xsl:when>
      <xsl:when test="bpmn:multiInstanceLoopCharacteristics/@isSequential='true'">
        <xsl:element name="path">
          <xsl:attribute name="class">icon <xsl:value-of select="local-name($this)"/></xsl:attribute>
          <xsl:attribute name="transform">translate(<xsl:value-of select="number(//bpmndi:BPMNShape[@bpmnElement=$id]/dc:Bounds/@x)+number(//bpmndi:BPMNShape[@bpmnElement=$id]/dc:Bounds/@width div 2)-10"/>,<xsl:value-of select="number(//bpmndi:BPMNShape[@bpmnElement=$id]/dc:Bounds/@y)+number(//bpmndi:BPMNShape[@bpmnElement=$id]/dc:Bounds/@height)+$yOffset"/>) scale(0.01)</xsl:attribute>
          <xsl:attribute name="d"><xsl:value-of select="$sequentialMiMarker"/></xsl:attribute>
        </xsl:element>
      </xsl:when>
      <xsl:when test="bpmn:multiInstanceLoopCharacteristics">
        <xsl:element name="path">
          <xsl:attribute name="class">icon <xsl:value-of select="local-name($this)"/></xsl:attribute>
          <xsl:attribute name="transform">translate(<xsl:value-of select="number(//bpmndi:BPMNShape[@bpmnElement=$id]/dc:Bounds/@x)+number(//bpmndi:BPMNShape[@bpmnElement=$id]/dc:Bounds/@width div 2)-10"/>,<xsl:value-of select="number(//bpmndi:BPMNShape[@bpmnElement=$id]/dc:Bounds/@y)+number(//bpmndi:BPMNShape[@bpmnElement=$id]/dc:Bounds/@height)+$yOffset"/>) scale(0.01)</xsl:attribute>
          <xsl:attribute name="d"><xsl:value-of select="$parallelMiMarker"/></xsl:attribute>
        </xsl:element>
      </xsl:when>
      <xsl:otherwise>
        <xsl:if test="$debug='true'">
          <xsl:comment>No activity markers</xsl:comment>
        </xsl:if>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template name="flowOrAssociation">
    <xsl:param name="this"/>
    <xsl:param name="id"/>
    <xsl:param name="baseType"/>
    <xsl:param name="sourceRef"/>
    <xsl:param name="targetRef"/>
    <xsl:param name="bpmnElement"/>
    <xsl:param name="srcElement"/>
    <xsl:param name="trgtElement"/>
    
    <xsl:if test="$debug='true'">
      <xsl:comment>FLOW OR ASSOC <xsl:value-of select="$id"/><xsl:value-of select="$sourceRef/@id"/></xsl:comment>
	    <xsl:choose>
	      <xsl:when test="$srcElement/dc:Bounds/@x = $trgtElement/dc:Bounds/@x">
	        <xsl:comment>JOIN TOP</xsl:comment>
	      </xsl:when>
	      <xsl:when test="false">
	        <xsl:comment>JOIN RIGHT</xsl:comment>
	      </xsl:when>
	      <xsl:when test="false">
	        <xsl:comment>JOIN BOTTOM</xsl:comment>
	      </xsl:when>
	      <xsl:otherwise>
	        <xsl:comment>JOIN LEFT
	          <xsl:text>$baseType </xsl:text><xsl:value-of select="$baseType"/>

	          <xsl:text>$srcElement/@id </xsl:text><xsl:value-of select="$srcElement/@id"/>

	          <xsl:text>$srcElement/dc:Bounds/@x </xsl:text><xsl:value-of select="$srcElement/dc:Bounds/@x"/>
	          <xsl:text>$srcElement/dc:Bounds/@y </xsl:text><xsl:value-of select="$srcElement/dc:Bounds/@y"/>
	          <xsl:text>$srcElement/dc:Bounds/@height </xsl:text><xsl:value-of select="$srcElement/dc:Bounds/@height"/>
	          <xsl:text>$srcElement/dc:Bounds/@width </xsl:text><xsl:value-of select="$srcElement/dc:Bounds/@width"/>

	          <xsl:text>$srcElement/@x </xsl:text><xsl:value-of select="$srcElement/@x"/>
	          <xsl:text>$srcElement/@y </xsl:text><xsl:value-of select="$srcElement/@y"/>
	          <xsl:text>$srcElement/@height </xsl:text><xsl:value-of select="$srcElement/@height"/>
	          <xsl:text>$srcElement/@width </xsl:text><xsl:value-of select="$srcElement/@width"/>
	        </xsl:comment>
	      </xsl:otherwise>          
	    </xsl:choose>
    </xsl:if>

    <xsl:element name="path">
      <xsl:attribute name="id"><xsl:value-of select="$id"/></xsl:attribute>
      <xsl:attribute name="class">
        <xsl:if test="//bpmn:exclusiveGateway[@default=$id] or //exclusiveGateway[@default=$id]">default </xsl:if>
        <xsl:value-of select="$baseType"/>
        <xsl:text> </xsl:text>
        <xsl:value-of select="local-name($this)"/>
      </xsl:attribute>
      <xsl:attribute name="data-type"><xsl:value-of select="local-name($this)"/></xsl:attribute>
      <xsl:attribute name="d">
        <xsl:text>M </xsl:text>

        <xsl:for-each select="$bpmnElement/di:waypoint">
          <xsl:if test="position() &gt; 1">
            <xsl:text> L </xsl:text>
          </xsl:if>
          <xsl:value-of select="@x"/>
          <xsl:text> </xsl:text>
          <xsl:value-of select="@y"/>
        </xsl:for-each>

      </xsl:attribute>
      <xsl:attribute name="fill">white</xsl:attribute>
      <xsl:attribute name="stroke">#666</xsl:attribute>
      <xsl:attribute name="stroke-width">2.0</xsl:attribute>
      <xsl:choose>
        <xsl:when test="$baseType='flow'">
          <xsl:attribute name="style">
            <xsl:if test="//bpmn:exclusiveGateway[@default=$id] or //exclusiveGateway[@default=$id]">marker-start:url(#backslash);</xsl:if>
            <xsl:text> marker-end:url(#filled-arrow)</xsl:text>
          </xsl:attribute>
        </xsl:when>
        <xsl:when test="$baseType='association' and local-name($this)!='association'">
          <xsl:attribute name="style">marker-end:url(#open-arrow)</xsl:attribute>
        </xsl:when>
	    </xsl:choose>
      
      <xsl:choose>
        <xsl:when test="local-name(.)='dataInputAssociation'">
          <xsl:attribute name="stroke-dasharray">3,5</xsl:attribute>
        </xsl:when>
        <xsl:when test="local-name(.)='dataOutputAssociation'">
          <xsl:attribute name="stroke-dasharray">3,5</xsl:attribute>
        </xsl:when>
        <xsl:when test="local-name(.)='messageFlow'">
          <xsl:attribute name="stroke-dasharray">10,5</xsl:attribute>
        </xsl:when>
        <xsl:when test="local-name(.)='association'">
          <xsl:attribute name="stroke-dasharray">3,3</xsl:attribute>
        </xsl:when>
      </xsl:choose>
      
      <xsl:choose>
        <xsl:when test="conditionExpression|bpmn:conditionExpression">
          <xsl:attribute name="data-condition">
            <xsl:value-of select="conditionExpression"/>
            <xsl:value-of select="bpmn:conditionExpression"/>
          </xsl:attribute>
        </xsl:when>
      </xsl:choose>
      <xsl:if test="bpmn:documentation">
        <title><xsl:value-of select="bpmn:documentation/text()"/></title>
      </xsl:if>
    </xsl:element>
  </xsl:template>

  <xsl:template name="label">
    <xsl:param name="id"/>
    <xsl:param name="r"/>
    <xsl:param name="tx"/>
    <xsl:param name="ty">0</xsl:param>
    <xsl:param name="fill">#666</xsl:param>
    
    <xsl:variable name="len" select="string-length(@name)"/>
    <xsl:variable name="cutBefore" select="contains(substring(@name,0,$len div 2),' ')*(string-length(substring-before(substring(@name,0,$len div 2),' ')))"/>
    <xsl:variable name="cutAfter" select="contains(substring(@name,($len) div 2),' ')*(string-length(substring-before(substring(@name,($len) div 2),' ')))"/>
    <xsl:variable name="lenBefore" select="string-length(substring(@name,$len div 2)) - $cutBefore"/>
    <xsl:variable name="lenAfter" select="string-length(substring(@name,$len div 2)) + $cutAfter"/>

    <xsl:variable name="diElement" select="//bpmndi:*[@bpmnElement=$id]"/>
    
    <!-- Set label x,y to bounds if we have and otherwise make an educated guess -->
    <xsl:variable name="labelX">
      <xsl:choose>
        <xsl:when test="$diElement/bpmndi:BPMNLabel/dc:Bounds">
          <xsl:value-of select="$diElement/bpmndi:BPMNLabel/dc:Bounds/@x"/>
        </xsl:when>
        <xsl:when test="contains(local-name(.),'callActivity') or contains(local-name(.),'subProcess') or contains(local-name(.),'task') or contains(local-name(.),'Task')">
          <xsl:value-of select="$diElement/dc:Bounds/@x+$fontSize"/>
        </xsl:when>
        <xsl:when test="local-name($diElement)='BPMNEdge' and not(bpmndi:BPMNLabel/dc:Bounds)">
          <xsl:value-of select="$diElement/di:waypoint[position()=1]/@x"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="$diElement/dc:Bounds/@x"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:variable name="labelY">
      <xsl:choose>
        <xsl:when test="$diElement/bpmndi:BPMNLabel/dc:Bounds">
          <xsl:value-of select="$diElement/bpmndi:BPMNLabel/dc:Bounds/@y"/>
        </xsl:when>
        <xsl:when test="contains(local-name(.),'callActivity') or contains(local-name(.),'subProcess') or contains(local-name(.),'task') or contains(local-name(.),'Task')">
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

    <xsl:if test="$debug='true'">
	    <xsl:if test="local-name($diElement)='BPMNEdge' and not(bpmndi:BPMNLabel/dc:Bounds)">
	      <xsl:comment>EUREKA!!!
	      <xsl:value-of select="$diElement/di:waypoint[position()=1]/@y"/>
	      </xsl:comment>
	    </xsl:if>
	  </xsl:if>

    <xsl:apply-templates select="$diElement/bpmndi:BPMNLabel/dc:Bounds">
      <xsl:with-param name="fill">#5F5</xsl:with-param>
    </xsl:apply-templates>

    <xsl:if test="$debug='true'">
      <xsl:comment>
        <xsl:text>len: </xsl:text><xsl:value-of select="$len"/>
        <xsl:text>len div 2: </xsl:text><xsl:value-of select="$len div 2"/>
        <xsl:text>cutBefore: </xsl:text><xsl:value-of select="$cutBefore"/>
        <xsl:text>cutAfter: </xsl:text><xsl:value-of select="$cutAfter"/>
        <xsl:text>lenBefore: </xsl:text><xsl:value-of select="$lenBefore"/>
        <xsl:text>lenAfter: </xsl:text><xsl:value-of select="$lenAfter"/>
      </xsl:comment>
    </xsl:if>

    <!-- 
      Strategy here is to first honour line breaks and otherwise try a crude split into 2 lines. 
      The latter covers many cases so is judged 'good enough' -->
    <xsl:choose>
      <xsl:when test="not(contains(@name,'&#10;')) and $cutBefore!=0 and $cutAfter!=0 and number($diElement/dc:Bounds/@height) &gt;= (2.75*$fontSize)">
      <!-- xsl:when test="1=2"-->
        <xsl:element name="text">
          <xsl:attribute name="class">label <xsl:value-of select="local-name(.)"/></xsl:attribute>
          <xsl:attribute name="id"><xsl:value-of select="$id"/>_label</xsl:attribute>
          <xsl:attribute name="fill"><xsl:value-of select="$fill"/></xsl:attribute>
          <xsl:attribute name="stroke-width">0</xsl:attribute>
          <xsl:attribute name="transform">rotate(<xsl:value-of select="$r"/>) translate(<xsl:value-of select="$tx"/>,<xsl:value-of select="$ty"/>)</xsl:attribute>
          <xsl:attribute name="x">
            <xsl:value-of select="$labelX"/>
          </xsl:attribute>
          <xsl:attribute name="y">
            <xsl:value-of select="$labelY+$fontSize"/>
          </xsl:attribute>
          <xsl:value-of select="substring(@name,0,($len div 2) + $cutAfter)"/>
        </xsl:element>
        <xsl:element name="text">
          <xsl:attribute name="class">label <xsl:value-of select="local-name(.)"/></xsl:attribute>
          <xsl:attribute name="fill"><xsl:value-of select="$fill"/></xsl:attribute>
          <xsl:attribute name="stroke-width">0</xsl:attribute>
          <xsl:attribute name="transform">rotate(<xsl:value-of select="$r"/>) translate(<xsl:value-of select="$tx"/>,<xsl:value-of select="$ty"/>)</xsl:attribute>
          <xsl:attribute name="x">
            <xsl:value-of select="$labelX"/>
          </xsl:attribute>
          <xsl:attribute name="y">
            <xsl:value-of select="$labelY+(2.2*$fontSize)"/>
          </xsl:attribute>
          <xsl:value-of select="substring(@name,($len div 2) + $cutAfter)"/>
        </xsl:element>
      </xsl:when>
      <xsl:otherwise>
	      <xsl:call-template name="multiLineText">
		      <xsl:with-param name="class">label <xsl:value-of select="local-name(.)"/></xsl:with-param>
		      <xsl:with-param name="depth" select="0"/>
		      <xsl:with-param name="diElement" select="$diElement"/>
		      <xsl:with-param name="id" select="$id"/>
		      <xsl:with-param name="fill"><xsl:value-of select="$fill"/></xsl:with-param>
		      <xsl:with-param name="text">
		        <xsl:value-of select="@name"/>
		      </xsl:with-param>
		      <xsl:with-param name="r" select="$r"/>
		      <xsl:with-param name="x" select="$labelX"/>
		      <xsl:with-param name="y" select="$labelY+$fontSize"/>
		      <!--xsl:with-param name="y" select="$diElement/bpmndi:BPMNLabel/dc:Bounds/@y+($diElement/dc:Bounds/@height div 3)"/>-->
		    </xsl:call-template>
	    </xsl:otherwise>   
    </xsl:choose>   
  </xsl:template>

  <xsl:template name="maximum">
    <xsl:param name="sequence"/>
    <xsl:param name="margin">0</xsl:param>

    <xsl:for-each select="$sequence">
      <xsl:sort select="." data-type="number" order="descending"/>
      <xsl:if test="position()=1">
        <xsl:value-of select=".+$margin"/>
      </xsl:if>
    </xsl:for-each>
  </xsl:template>

  <xsl:template name="multiLineText">
    <xsl:param name="class"/>
    <xsl:param name="depth">0</xsl:param>
    <xsl:param name="diElement"/>
    <xsl:param name="id"/>
    <xsl:param name="fill">#666</xsl:param>
    <xsl:param name="text"/>
    <xsl:param name="r">0</xsl:param>
    <xsl:param name="x"/>
    <xsl:param name="y"/>

		<xsl:variable name="prevLanesHeight">
		  <xsl:choose>
		    <xsl:when test="$class='lane'">0</xsl:when>
		    <xsl:otherwise>0</xsl:otherwise>
		  </xsl:choose>
		</xsl:variable>

    <xsl:choose>
      <xsl:when test="contains($text,'&#10;')">
        <xsl:element name="text">
          <xsl:attribute name="class"><xsl:value-of select="$class"/></xsl:attribute>
          <xsl:attribute name="id"><xsl:value-of select="$id"/>_label</xsl:attribute>
          <xsl:attribute name="fill"><xsl:value-of select="$fill"/></xsl:attribute>
          <xsl:attribute name="transform">rotate(<xsl:value-of select="$r"/>) translate(0, <xsl:value-of select="$depth*($fontSize+$lineSpacing)"/>)</xsl:attribute>
          <xsl:attribute name="x"><xsl:value-of select="$x"/></xsl:attribute>
          <xsl:attribute name="y"><xsl:value-of select="$y"/></xsl:attribute>
          <xsl:value-of select="substring-before($text,'&#10;')"/>
        </xsl:element>

        <xsl:call-template name="multiLineText">
          <xsl:with-param name="class" select="$class"/>
          <xsl:with-param name="depth" select="$depth+1"/>
          <xsl:with-param name="diElement" select="$diElement"/>
          <xsl:with-param name="id" select="$id"/>
          <xsl:with-param name="fill" select="$fill"/>
          <xsl:with-param name="text">
            <xsl:value-of select="substring-after($text,'&#10;')"/>
          </xsl:with-param>
          <xsl:with-param name="r" select="$r"/>
          <xsl:with-param name="x" select="$x"/>
          <xsl:with-param name="y" select="$y"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:element name="text">
          <xsl:attribute name="class"><xsl:value-of select="$class"/></xsl:attribute>
          <xsl:attribute name="id"><xsl:value-of select="$id"/>_label</xsl:attribute>
          <xsl:attribute name="fill"><xsl:value-of select="$fill"/></xsl:attribute>
          <!-- We ALWAYS want text to have stroke width 0 else unreadable and regular sizes-->
          <xsl:attribute name="stroke-width">0</xsl:attribute>
          <xsl:choose>
            <xsl:when test="$r = -90"><!-- i.e. Participants and Lanes -->
              <!-- Guesstimate length of text in pixels -->
              <xsl:variable name="textLength"><xsl:value-of select="string-length($text)*3"/></xsl:variable>
              <xsl:attribute name="transform">rotate(<xsl:value-of select="$r"/>) translate(<xsl:value-of select="-($diElement/bpmndi:BPMNLabel/dc:Bounds/@y + $diElement/bpmndi:BPMNLabel/dc:Bounds/@height)"/>, <xsl:value-of select="$diElement/bpmndi:BPMNLabel/dc:Bounds/@x+(($depth+1)*($fontSize+$lineSpacing))"/>) </xsl:attribute>
              <xsl:attribute name="x">0</xsl:attribute>
              <xsl:attribute name="y">0</xsl:attribute>
            </xsl:when>
            <xsl:otherwise>
              <xsl:attribute name="transform">rotate(<xsl:value-of select="$r"/>) translate(0, <xsl:value-of select="$depth*($fontSize+$lineSpacing)"/>)</xsl:attribute>
		          <xsl:attribute name="x"><xsl:value-of select="$x"/></xsl:attribute>
		          <xsl:attribute name="y"><xsl:value-of select="$y"/></xsl:attribute>
            </xsl:otherwise>
          </xsl:choose>
          <xsl:value-of select="$text"/>
        </xsl:element>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template name="eventIcon">
    <xsl:param name="bpmnElement"/>
    <xsl:param name="class"/>
    <xsl:param name="icon"/>
    <xsl:param name="id"/>

    <!-- Guestimate how much to offset icon based on event size -->
	  <xsl:variable name="eventIconOffsetX">
	    <xsl:choose>
        <xsl:when test="//bpmndi:BPMNShape[@bpmnElement=$id]/dc:Bounds/@height &lt; 35">4</xsl:when>
        <xsl:when test="//bpmndi:BPMNShape[@bpmnElement=$id]/dc:Bounds/@height &gt; 50">17</xsl:when>
	      <xsl:otherwise>10</xsl:otherwise>
	    </xsl:choose>
	  </xsl:variable>
    <xsl:variable name="eventIconOffsetY">
      <xsl:choose>
        <xsl:when test="//bpmndi:BPMNShape[@bpmnElement=$id]/dc:Bounds/@height &lt; 35">3</xsl:when>
        <xsl:when test="//bpmndi:BPMNShape[@bpmnElement=$id]/dc:Bounds/@height &gt; 50">15</xsl:when>
        <xsl:otherwise>10</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
  
    <xsl:if test="$debug='true'">
	    <xsl:comment>
	      <xsl:text>HEIGHT: </xsl:text>
	      <xsl:value-of select="//bpmndi:BPMNShape[@bpmnElement=$id]/dc:Bounds/@height"/>
	      <xsl:text>, ICON OFFSET (x,y): </xsl:text>
	      <xsl:value-of select="$eventIconOffsetX"/>
	      <xsl:text>,</xsl:text>
	      <xsl:value-of select="$eventIconOffsetY"/>
	    </xsl:comment>
	  </xsl:if>
    <xsl:variable name="s">
      <xsl:choose>
        <xsl:when test="$class='compensation'"> 
          <xsl:value-of select="0.030"/>
        </xsl:when>
        <xsl:when test="$class='conditional'"> 
          <xsl:value-of select="0.028"/>
        </xsl:when>
        <xsl:when test="$class='escalation'"> 
          <xsl:value-of select="0.030"/>
        </xsl:when>
        <xsl:when test="$class='error'"> 
          <xsl:value-of select="0.030"/>
        </xsl:when>
        <xsl:when test="$class='link'"> 
          <xsl:value-of select="0.030"/>
        </xsl:when>
        <xsl:when test="$class='message'"> 
          <xsl:value-of select="0.030"/>
        </xsl:when>
        <xsl:when test="$class='messageThrow'"> 
          <xsl:value-of select="0.026"/>
        </xsl:when>
        <xsl:when test="$class='signal'"> 
          <xsl:value-of select="0.030"/>
        </xsl:when>
        <xsl:when test="$class='timer'"> 
          <xsl:value-of select="0.022"/>
        </xsl:when>
      </xsl:choose>
    </xsl:variable>
    <xsl:variable name="t">
      <xsl:choose>
        <xsl:when test="local-name($bpmnElement)='startEvent'">
          <xsl:value-of select="//bpmndi:BPMNShape[@bpmnElement=$id]/dc:Bounds/@x+$eventIconOffsetX"/>,<xsl:value-of select="//bpmndi:BPMNShape[@bpmnElement=$id]/dc:Bounds/@y+$eventIconOffsetY"/>
        </xsl:when>
        <xsl:when test="local-name($bpmnElement)='endEvent'">
          <xsl:value-of select="//bpmndi:BPMNShape[@bpmnElement=$id]/dc:Bounds/@x+$eventIconOffsetX"/>,<xsl:value-of select="//bpmndi:BPMNShape[@bpmnElement=$id]/dc:Bounds/@y+$eventIconOffsetY"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="//bpmndi:BPMNShape[@bpmnElement=$id]/dc:Bounds/@x+$eventIconOffsetX"/>,<xsl:value-of select="//bpmndi:BPMNShape[@bpmnElement=$id]/dc:Bounds/@y+$eventIconOffsetY"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>

    <xsl:element name="g">
      <xsl:attribute name="class"><xsl:value-of select="$class"/> <xsl:value-of select="local-name($bpmnElement)"/></xsl:attribute>
      <xsl:attribute name="id">
        <xsl:value-of select="$id"/>
      </xsl:attribute>
      <xsl:attribute name="transform">translate(<xsl:value-of select="$t"/>) scale(<xsl:value-of select="$s"/>)</xsl:attribute>
      <xsl:element name="path">
        <xsl:attribute name="d">
          <xsl:value-of select="$icon"/>
        </xsl:attribute>
	      <xsl:attribute name="fill">
	        <xsl:choose>
	          <xsl:when test="local-name($bpmnElement)='startEvent'">
	            <xsl:text>#34bb16</xsl:text>
	          </xsl:when>
	          <xsl:when test="local-name($bpmnElement)='endEvent'">
	            <xsl:text>#ff0000</xsl:text>
	          </xsl:when>
	          <xsl:otherwise>
	            <xsl:text>#666</xsl:text>
	          </xsl:otherwise>
	        </xsl:choose>
	      </xsl:attribute>      
      </xsl:element>
    </xsl:element>
  </xsl:template>

  <!-- END NAMED TEMPLATES -->


  <!-- ICONS -->

  <xsl:variable name="businessRuleTaskIcon">
    m 357.73047,473.9082 0,759.8789 1012.22073,0 0,-759.8789 z m 43.10547,263.10547 926.01176,0 0,206.39063 -687.76762,0 0,-205.88086 -43.10547,0 0,205.88086 -195.13867,0 z m 0,249.4961 195.13867,0 0,204.17183 -195.13867,0 z m 238.24414,0 687.76762,0 0,204.17183 -687.76762,0 z
  </xsl:variable>
  
  <xsl:variable name="conditionalEventIcon">
    m 78,98.031314 0,22.611216 0,679.35746 103.55469,0 412.71287,0 103.7442,0 0,-701.968676 z m 49.88672,45.057216 520.04684,0 0,611.84545 -53.666,0 -412.71287,0 -53.66797,0 z m 53.66797,68.87762 0,44.89024 412.71287,0 0,-44.89024 z m 0,135.52665 0,45.06424 412.71287,0 0,-45.06424 z m 0,156.61403 0,44.89901 412.71287,0 0,-44.89901 z m 0,141.48136 0,44.90071 412.71287,0 0,-44.90071 z
  </xsl:variable>

  <xsl:variable name="compensationEventIcon">
    M 893.29135,622.21057 C 750.36687,723.12006 607.44239,824.02955 464.51791,924.93904 c 142.93434,100.96066 285.84746,201.95136 428.77344,302.92386 0,-97.5716 0,-195.1433 0,-292.71486 138.01705,97.65226 276.15795,195.12986 414.22265,292.71486 0,-201.8841 0,-403.76821 0,-605.65233 -138.0742,97.51042 -276.1484,195.02085 -414.22265,292.53127 0,-97.51042 0,-195.02085 0,-292.53127 z
  </xsl:variable>

  <xsl:variable name="complexGatewayIcon">
    m 1005.0781,617.76172 0,0.41601 c 0,0.01 -8.36696,1.54883 -8.37112,1.54883 -0.01,0 -6.8522,4.64453 -6.85742,4.64453 -0.01,0 -4.76342,7.0625 -4.76758,7.0625 0,0.01 -1.61028,8.41797 -1.61328,8.41797 l 0,286.24219 -202.64063,-202.63867 0.01,0 c -0.01,0 -7.19583,-4.64453 -7.19922,-4.64453 -0.012,0 -8.01684,-1.62688 -8.07813,-1.63867 -0.0775,0.0122 -8.31518,1.54296 -8.31836,1.54296 -0.01,-0.01 -7.06584,4.74024 -7.06835,4.74024 l -26.63282,26.61719 c 0,0 -4.86045,7.16015 -4.86133,7.16015 -0.01,0 -1.5041,8.22266 -1.5039,8.22266 2e-4,0.01 1.61378,8.12695 1.61523,8.12695 2.1e-4,0.01 4.61243,7.16016 4.61524,7.16016 l 202.53906,202.54297 -286.45703,0 -0.0566,-0.0684 c 0,0.01 -8.33159,1.83789 -8.33594,1.83789 -0.01,0.01 -6.85092,4.64453 -6.85547,4.64453 l 0,-0.0488 c -0.01,0.01 -4.76313,7.06445 -4.76758,7.06445 -0.01,0 -1.61194,8.41607 -1.61523,8.41607 l 0,37.666 c 0,0 1.60063,8.5156 1.6582,8.5156 0,0.019 4.74561,6.8691 4.7461,6.8691 0.01,0 6.89425,4.6446 6.89648,4.6446 0.01,0.019 8.36732,1.8379 8.37109,1.8379 l 286.35938,0 -202.36719,202.3671 0,-0.076 c -0.01,0.01 -4.86007,7.1601 -4.86133,7.1601 -0.01,0.01 -1.50586,8.2227 -1.50586,8.2227 2.1e-4,0.019 1.61603,8.1269 1.61719,8.1269 0,0 4.63062,7.1602 4.61524,7.1602 l 26.63671,26.6172 c 0.01,0.01 7.27806,4.7402 7.28125,4.7402 0,0 8.10404,1.6446 8.10743,1.6446 0.012,0 8.21254,-1.5469 8.21679,-1.5469 0,0 7.17132,-4.8379 7.17383,-4.8379 l 202.66797,-202.668 0,286.8613 -0.0527,-0.057 c 0,0.01 1.66004,8.5137 1.6582,8.5137 0,0.01 4.74414,6.8691 4.74414,6.8691 0.01,0.01 6.89474,4.6445 6.89648,4.6445 0,0 8.30856,1.7412 8.37106,1.8379 l 37.6543,-0.01 c 0.01,0 8.5313,-1.7403 8.5371,-1.7403 0,0 6.8507,-4.6445 6.8555,-4.6445 0.01,-0.01 4.7413,-6.8691 4.7461,-6.8691 0,0 1.6329,-8.5157 1.6367,-8.5157 l 0,-286.6972 202.5235,202.5136 c -0.01,0.019 7.2754,4.7403 7.2793,4.7403 0.01,0.01 8.1064,1.5488 8.1093,1.5488 0.012,0 8.212,-1.5488 8.2168,-1.5488 0,0 7.171,-4.8379 7.1758,-4.8379 l 26.625,-26.6348 c 0.01,0 4.7193,-7.0644 4.7793,-7.0644 -2e-4,-0.019 1.6403,-8.3193 1.5996,-8.416 -2e-4,-0.01 -1.6172,-8.1289 -1.6191,-8.1289 -0.012,0.01 -4.7135,-7.2559 -4.7754,-7.2559 l -202.0156,-202.0293 285.957,0 c 0.01,0.01 8.5313,-1.7422 8.5351,-1.7422 0.01,0 6.8526,-4.6445 6.8555,-4.6445 0.01,-0.019 4.7403,-6.8692 4.7461,-6.8692 0,0 1.6329,-8.5136 1.6367,-8.5136 l 0,-37.6563 c 0,-0.01 -1.6259,-8.32121 -1.623,-8.41796 -0.01,-0.01 -4.7213,-7.0625 -4.7793,-7.0625 -0.01,-0.01 -6.8955,-4.64453 -6.8984,-4.64453 0,0.01 -8.5079,-1.83699 -8.5118,-1.74024 l -286.8242,0 202.9258,-202.91992 c 0.01,-0.01 4.7174,-7.06445 4.7773,-7.06445 -2e-4,-0.0194 1.6426,-8.32032 1.6426,-8.32032 -2e-4,-0.01 -1.6162,-8.12695 -1.6172,-8.12695 -0.01,-0.01 -4.8457,-7.16016 -4.8476,-7.16016 L 1297.76,723.13416 c -0.01,0 -7.042,-4.74024 -7.0449,-4.74024 -0.01,-0.01 -8.1065,-1.54687 -8.1113,-1.54687 -0.012,0 -8.3467,1.54687 -8.3496,1.54687 -0.01,0.01 -7.0665,4.74024 -7.0684,4.74024 l -202.6719,202.66992 0,-286.26367 c 0,-0.01 -1.6259,-8.32122 -1.623,-8.41797 -0.01,-0.0194 -4.7207,-7.0625 -4.7207,-7.0625 -0.01,-0.01 -6.8946,-4.64453 -6.8965,-4.64453 -0.01,0 -8.5313,-1.64454 -8.5332,-1.64454 z m -239.51951,99.41016 c 3.1e-4,-5e-5 0.0332,-0.006 0.0332,-0.006 l -0.0625,0 c 1e-5,0 0.0291,0.006 0.0293,0.006 z
  </xsl:variable>

  <xsl:variable name="dataInputIcon">
    m 767.35199,-91.28812 c -286.2304,6.5e-4 -572.46091,9.6e-4 -858.69136,0.002 l 0,1584.21092 1213.51177,0 0,-1219.85545 C 1003.8989,151.61683 885.62549,30.16436 767.35199,-91.28812 z m -104.207,90 0,371.64844 369.02741,0 0,1032.56448 -1033.51177,0 0,-1404.21096 c 221.49479,-6.5e-4 442.98959,-9.6e-4 664.48436,-0.002 z m 90,24.41211 250.50391,257.23633 -250.50391,0 0,-257.23633 z m -454.56053,42.60547 0,46.23047 0,139.07227 -212.03516,0 0,159.54687 212.03516,0 0,185.30469 L 580.73489,330.80563 298.58446,65.72946 z m 40,92.46289 183.73243,172.61328 -183.73243,172.61329 0,-132.83985 -212.03516,0 0,-79.54687 212.03516,0 0,-132.83985 z
  </xsl:variable>
  
  <xsl:variable name="dataInputIconFill">
    m -1.2965639e-5,701.4841 0,-701.4841 L 328.92555,0 l 328.92553,0 0,187.95746 0,187.95746 184.6011,0 184.60112,0 0,513.52664 0,513.52664 -513.52667,0 -513.526642965639,0 0,-701.4841 z M 445.68514,463.70366 C 521.95588,391.99677 583.52558,331.4353 582.50678,329.1226 581.48788,326.80991 517.21868,265.36211 439.68619,192.57193 L 298.7181,60.22614 l -1.86251,94.07311 -1.86249,94.07311 -107.21996,0 -107.219953,0 0,83.90958 0,83.90958 107.404263,0 107.40426,0 0,91.18174 c 0,50.14997 2.62111,90.17484 5.82469,88.94416 3.20358,-1.23067 68.22801,-60.90687 144.49874,-132.61376 z
  </xsl:variable>

  <xsl:variable name="dataObjectIcon">
    m 767.352,-91.28807 c -286.23045,6.5e-4 -572.46091,0.001 -858.691365,0.002 0,528.07032 0,1056.14066 0,1584.21097 404.503925,0 809.007865,0 1213.511765,0 0,-406.6185 0,-813.237 0,-1219.8555 C 1003.8989,151.61692 885.6255,30.16445 767.352,-91.28803 z m -104.207,90 c 0,123.88281 0,247.76563 0,371.64844 123.0091,0 246.0183,0 369.0274,0 0,344.18818 0,688.37623 0,1032.56443 -344.5039,0 -689.00784,0 -1033.5117645,0 0,-468.07025 0,-936.14059 0,-1404.21091 C 220.15543,-1.28676 441.65021,-1.28711 663.145,-1.28811 z m 90,24.41211 c 83.5013,85.74544 167.0026,171.49089 250.5039,257.23633 -83.5013,0 -167.0026,0 -250.5039,0 0,-85.74544 0,-171.49089 0,-257.23633 z
  </xsl:variable>
  
  <xsl:variable name="dataObjectIconFill">
    M -4.512677e-6,701.48415 -4.512677e-6,0 328.92558,0 657.8512,0 l 0,187.95747 0,187.95748 184.6011,0 184.6011,0 0,513.52668 0,513.52667 -513.52672,0 -513.5266845126768,0 0,-701.48415 z
  </xsl:variable>
  
  <xsl:variable name="dataOutputIcon">
    m 767.352,-91.28807 c -286.2304,6.5e-4 -572.46091,9.6e-4 -858.691365,0.002 l 0,1584.21097 1213.511765,0 0,-1219.8555 C 1003.8989,151.61688 885.6255,30.16441 767.352,-91.28807 z m -104.207,90 0,371.64844 369.0274,0 0,1032.56453 -1033.5117645,0 0,-1404.21101 C 220.15543,-1.28676 441.65023,-1.28707 663.145,-1.28811 z m 90,24.41211 250.5039,257.23633 -250.5039,0 z m -454.56053,42.60547 0,185.30274 -212.035165,0 0,159.54687 212.035165,0 0,185.30469 282.15043,-265.07813 z
  </xsl:variable>
  
  <xsl:variable name="dataOutputIconFill">
    M -4.512677e-6,701.48415 -4.512677e-6,0 328.92558,0 657.8512,0 l 0,187.95747 0,187.95748 184.6011,0 184.6011,0 0,513.52668 0,513.52667 -513.52672,0 -513.5266845126768,0 0,-701.48415 z M 445.68518,463.7037 C 521.9559,391.9968 583.5256,331.43532 582.5068,329.12263 581.488,326.80994 517.2187,265.36213 439.68624,192.57194 l -140.96811,-132.3458 -1.8625,94.07312 -1.8625,94.07312 -107.21997,0 -107.219965,0 0,83.90958 0,83.90959 107.404285,0 107.40427,0 0,91.18175 c 0,50.14997 2.62111,90.17484 5.82469,88.94416 3.20358,-1.23067 68.22801,-60.90686 144.49874,-132.61376 z
  </xsl:variable>
  
  <xsl:variable name="dataStoreIcon">
    M 680.67576,0 C 522.8631,0 365.30963,13.01049 241.91406,39.92187 180.21628,53.37756 127.08085,70.10271 85.42383,92.05468 45.29971,113.19887 12.64856,140.47003 2.58008,179.88085 1.88677,181.9393 1.38657,184.05781 1.08594,186.20898 L 0,191.47851 l 0.73633,3.57031 c -0.51535,315.04128 0,614.82949 0,938.87108 l 0.7207,3.4961 c 8.633,41.8917 42.30977,70.4074 83.9668,92.3594 41.65702,21.952 94.79245,38.6752 156.49023,52.1309 123.39557,26.9113 280.94904,39.9238 438.7617,39.9238 157.8127,0 315.3662,-13.0125 438.76174,-39.9238 61.6978,-13.4557 114.8333,-30.1789 156.4903,-52.1309 41.657,-21.952 75.3357,-50.4677 83.9687,-92.3594 l 0.7188,-3.4961 c 0,-309.85813 0.01,-647.33317 0,-940.84764 0.096,-1.4965 0.096,-2.99764 0,-4.49414 l 0,-0.66797 -0.1192,-0.57422 c -0.3037,-2.86398 -0.9597,-5.67953 -1.9531,-8.38281 -10.3403,-38.91405 -42.8049,-65.9196 -82.6152,-86.89844 C 1234.2708,70.10271 1181.1353,53.37756 1119.4375,39.92187 996.04196,13.01049 838.48846,0 680.67576,0 Z m 0,70 c 153.9084,0 308.076,13.06619 423.84574,38.31445 57.8849,12.62413 106.1932,28.50022 138.7715,45.66797 26.0001,13.70124 39.6105,27.31335 45.1797,37.49609 -5.5695,10.18265 -19.1804,23.79528 -45.1797,37.4961 -32.5783,17.16775 -80.8866,33.04188 -138.7715,45.66601 -115.76974,25.24826 -269.93734,38.31641 -423.84574,38.31641 -153.90834,0 -308.07597,-13.06815 -423.84568,-38.31641 C 198.94522,262.01649 150.63686,246.14236 118.05859,228.97461 92.05934,215.27379 78.44843,201.66116 72.87891,191.47851 78.4481,181.29577 92.05852,167.68366 118.05859,153.98242 150.63686,136.81467 198.94522,120.93858 256.83008,108.31445 372.59979,83.06619 526.76742,70 680.67576,70 Z M 70.73633,282.64257 c 4.76495,2.85244 9.66013,5.6105 14.6875,8.25977 41.65702,21.95198 94.79245,38.67517 156.49023,52.13086 123.39557,26.91138 280.94904,39.92383 438.7617,39.92383 157.8127,0 315.3662,-13.01245 438.76174,-39.92383 61.6978,-13.45569 114.8333,-30.17888 156.4903,-52.13086 5.0273,-2.64927 9.9225,-5.40733 14.6875,-8.25977 l 0,82.0625 c -3.4205,10.40901 -16.9417,26.30677 -47.3223,42.31641 -32.5783,17.16775 -80.8866,33.04189 -138.7715,45.66602 -115.76974,25.24825 -269.93734,38.3164 -423.84574,38.3164 -153.90834,0 -308.07597,-13.06815 -423.84568,-38.3164 C 198.94522,440.06337 150.63686,424.18923 118.05859,407.02148 87.67802,391.01184 74.15677,375.11408 70.73633,364.70507 Z m 0,178.04688 c 4.76495,2.85243 9.66013,5.61049 14.6875,8.25976 41.65702,21.95198 94.79245,38.67517 156.49023,52.13086 123.39557,26.91139 280.94904,39.92383 438.7617,39.92383 157.8127,0 315.3662,-13.01244 438.76174,-39.92383 61.6978,-13.45569 114.8333,-30.17888 156.4903,-52.13086 5.0273,-2.64927 9.9225,-5.40733 14.6875,-8.25976 l 0,82.0625 c -3.4205,10.409 -16.9417,26.30677 -47.3223,42.31641 -32.5783,17.16775 -80.8866,33.04188 -138.7715,45.66601 -115.76974,25.24826 -269.93734,38.31641 -423.84574,38.31641 -153.90834,0 -308.07597,-13.06815 -423.84568,-38.31641 C 198.94522,618.11024 150.63686,602.23611 118.05859,585.06836 87.67802,569.05872 74.15677,553.16095 70.73633,542.75195 Z m 0,178.04883 c 4.76494,2.85243 9.66014,5.60854 14.6875,8.25781 41.65702,21.95198 94.79245,38.67517 156.49023,52.13086 123.39557,26.91138 280.94904,39.92383 438.7617,39.92383 157.8127,0 315.3662,-13.01245 438.76174,-39.92383 61.6978,-13.45569 114.8333,-30.17888 156.4903,-52.13086 5.0273,-2.64927 9.9225,-5.40538 14.6875,-8.25781 l 0,486.79302 c -3.4205,10.409 -16.9417,26.3067 -47.3223,42.3164 -32.5783,17.1677 -80.8866,33.0438 -138.7715,45.6679 -115.76974,25.2483 -269.93734,38.3145 -423.84574,38.3145 -153.90834,0 -308.07597,-13.0662 -423.84568,-38.3145 -57.88486,-12.6241 -106.19322,-28.5002 -138.77149,-45.6679 -30.38057,-16.0097 -43.90182,-31.9074 -47.32226,-42.3164 z
  </xsl:variable>

  <xsl:variable name="escalationEventIcon">
    M 409,0.8046875 C 312.21561,270.13528 250.78496,550.66962 154,820.00003 242.79422,731.72804 320.21084,632.84122 409,544.56442 494.00004,636.37634 579.00004,728.18814 664.00004,820.00003 577.38254,547.44012 495.61734,273.36461 409,0.8046875 z
  </xsl:variable>

  <xsl:variable name="errorEventIcon">
    M 709.0586,0 C 638.9798,142.05924 568.9011,284.11847 498.8223,426.17771 411.6869,312.83918 324.55145,199.50064 237.41602,86.16211 158.27735,320.21614 79.13867,554.27021 0,788.32421 88.72461,674.28191 177.44922,560.23961 266.17383,446.19731 357.6947,550.92121 449.2155,655.64521 540.7363,760.36911 596.8437,506.91271 652.9512,253.45637 709.0586,0 z
  </xsl:variable>
  
  <xsl:variable name="eventBasedGatewayIcon">
    m 1024.002,564.95508 -14.7032,10.67187 -426.27927,309.45313 168.43945,517.96682 545.08202,0 168.4395,-517.96682 z m 0,61.77539 382.1778,277.43945 -145.9766,448.90428 -472.39842,0 -145.97656,-448.90428 z
  </xsl:variable>

  <xsl:variable name="exclusiveGatewayIcon">
    m 725.68555,669.78711 c -0.0138,7.2e-4 -9.61179,1.83789 -9.61914,1.83789 -0.01,0 -8.14488,5.51367 -8.15039,5.51367 l -30.73243,30.73828 c -0.01,0 -5.61057,8.22552 -5.61328,8.32227 0,0.01 -1.737,9.48047 -1.73633,9.48047 -6.9e-4,0.01 1.86771,9.38476 1.8711,9.38476 0,0.01 5.33756,8.32227 5.34375,8.32227 l 280.70703,280.69918 -280.57227,280.5743 0,-0.088 c 0,0.01 -5.61057,8.3203 -5.61328,8.3203 0,0.01 -1.73633,9.4824 -1.73633,9.4824 0,0.019 1.86829,9.3848 1.8711,9.3848 0,0 5.33843,8.2235 5.34375,8.3203 l 30.73437,30.7285 c 0.01,0.01 8.41062,5.5156 8.41797,5.5156 0.01,0 9.3452,1.8379 9.35352,1.8379 0.01,0 9.47878,-1.7402 9.48633,-1.7402 0.01,0 8.27964,-5.6133 8.28515,-5.6133 l 280.57613,-280.582 280.6368,280.6406 c 0.01,0.01 8.4121,5.5156 8.4179,5.5156 0.01,0 9.3458,1.8379 9.3535,1.8379 0.01,0 9.4806,-1.7422 9.4883,-1.7422 0.01,0 8.2755,-5.6113 8.2813,-5.6113 l 30.7344,-30.7285 c 0.01,-0.01 5.4756,-8.127 5.4804,-8.127 0,-0.01 1.8711,-9.579 1.8711,-9.6758 0,-0.01 -1.8692,-9.3847 -1.873,-9.3847 0,0 -5.4718,-8.418 -5.4785,-8.418 L 1090.209,1023.9219 1370.8164,743.31836 c 0.01,0 5.4737,-8.12695 5.4785,-8.12695 0,-0.01 1.8711,-9.57813 1.8711,-9.57813 0,-0.0194 -1.8682,-9.38476 -1.8711,-9.38476 0,-0.01 -5.6055,-8.32227 -5.6113,-8.32227 l -30.7344,-30.73828 c -0.01,0 -8.1436,-5.51367 -8.1504,-5.51367 -0.01,0 -9.3448,-1.83985 -9.3535,-1.83985 -0.01,0 -9.6124,1.83985 -9.6191,1.83985 -0.01,0 -8.1456,5.51367 -8.1504,5.51367 L 1024.0625,957.78125 743.32422,677.0332 l 0,-0.0879 c -0.01,0 -8.27819,-5.32031 -8.28516,-5.32031 -0.01,0 -9.34027,-1.83715 -9.35156,-1.83789 z
  </xsl:variable>
  
  <xsl:variable name="inclusiveGatewayIcon">
    m 1024,594 c -237.23182,0 -430,192.77939 -430,430.0078 0,237.2284 192.76818,430 430,430 237.2318,0 430,-192.7716 430,-430 C 1454,786.77939 1261.2318,594 1024,594 z m 0,47.68945 c 211.4073,0 382.3223,170.91329 382.3223,382.31835 0,211.4051 -170.915,382.3301 -382.3223,382.3301 -211.40721,0 -382.32227,-170.925 -382.32227,-382.3301 0,-211.40506 170.91506,-382.31835 382.32227,-382.31835 z
  </xsl:variable>
  
  <xsl:variable name="linkEventIcon">
    m 8.7407755,1040.0406 c 0,0.7088 0,1.4176 0,2.1265 -1.5939247,0 -3.1878494,0 -4.7817741,0 0,1.3385 0,2.6771 0,4.0157 1.5939247,0 3.1878494,0 4.7817741,0 0,0.7087 0,1.4175 0,2.1262 1.162238,-1.378 2.3244765,-2.756 3.4867145,-4.1341 -1.162238,-1.3781 -2.3244765,-2.7562 -3.4867145,-4.1343 z m 0.582468,1.5458 c 0.7199345,0.8562 1.4398675,1.7124 2.1598005,2.5686 -0.719933,0.8562 -1.439866,1.7124 -2.1598005,2.5686 0,-0.3854 0,-0.7708 0,-1.1563 -1.571248,0 -3.1424963,0 -4.7137447,0 0,-0.9346 0,-1.8692 0,-2.8038 1.5712484,0 3.1424967,0 4.7137447,0 0,-0.3923 0,-0.7847 0,-1.1771 z
  </xsl:variable>

  <xsl:variable name="manualTaskIcon">
    m 695.49414,409.66797 c -13.39574,0.32208 -25.80322,5.36295 -36.43555,12.21875 l -0.0176,0.0117 -0.0176,0.0117 C 599.9566,460.15923 397.74821,600.48895 353.29883,630.99805 l -0.002,0.002 -0.002,0 c -40.15266,27.56805 -67.19996,69.17719 -82.75,118.47461 l -0.002,0.01 -0.002,0.008 c -16.18824,51.4705 -14.11161,107.53949 -13.95508,154.49804 l 0.002,0.0137 0,0.0117 c 0.15894,35.35859 1.001,66.21493 10.17188,103.6367 6.2e-4,0 0.003,0.01 0.004,0.01 13.25657,54.4603 38.10466,97.7532 73.88281,127.084 35.77666,29.3296 81.74294,43.8716 133.70899,44.0332 182.31672,0.6844 364.74146,0.9292 547.15627,0 l 0.016,0 0.016,0 c 19.6375,-0.1288 38.6829,-7.644 50.9062,-22.1758 12.2224,-14.5307 17.1798,-33.6885 17.4375,-54.8672 0.1722,-13.4811 -1.6209,-26.1934 -5.8379,-37.539 l 19.6035,0 c 20.1422,0 38.4971,-6.6026 51.1426,-18.9453 12.6413,-12.3386 19.0251,-28.4813 22.4375,-45.25788 3e-4,-10e-4 -2e-4,-0.003 0,-0.004 4.9473,-24.23919 2.3279,-48.32773 -7.5781,-68.52343 22.7684,-2.27725 41.9265,-14.63427 52.625,-31.29883 11.7772,-18.3447 15.4421,-39.97549 15.5098,-61.49805 0.071,-22.47833 -5.314,-44.29739 -17.6153,-61.98828 -12.3012,-17.69089 -33.2618,-30.67067 -57.4746,-30.70508 l -0.01,0 -0.01,0 c -8.835,-0.007 -18.3255,-0.0349 -28.6406,-0.0469 6.4993,-14.19039 9.3369,-30.23737 9.2851,-46.84571 l 0,-0.0352 0,-0.0371 c -0.1421,-22.31858 -5.8991,-43.21469 -18.9746,-59.30274 -13.0752,-16.08776 -33.8563,-25.83753 -56.5762,-25.83789 -136.34709,-0.24436 -254.15236,1.057 -384.70312,0.70313 -4.75835,-0.014 -7.23386,-0.062 -11.4375,-0.084 6.16194,-6.19657 12.15647,-12.24923 18.74024,-18.79687 20.26075,-20.14956 39.7335,-39.13715 49.79297,-52.1543 24.04936,-30.95283 27.63893,-72.88917 7.39258,-103.73633 -10.67442,-16.30735 -26.32848,-27.34017 -43.13477,-29.63671 -2.10145,-0.28716 -4.18335,-0.44929 -6.24414,-0.49805 -0.9016,-0.0213 -1.80031,-0.0215 -2.69336,0 z m 1.68555,43.66406 c 0.47909,0.005 0.92618,0.0392 1.33984,0.0957 3.30925,0.4522 6.8528,1.63351 12.51953,10.29493 l 0.01,0.0176 0.01,0.0156 c 7.91668,12.05514 7.56693,36.36726 -5.3789,53.01367 l -0.0215,0.0293 -0.0215,0.0273 c -4.70726,6.09405 -25.85704,27.8311 -46.04492,47.90821 -20.1879,20.0771 -39.38105,38.60748 -48.87696,50.17578 -9.19496,11.20172 -5.74708,20.31685 -3.33593,26.11328 2.41114,5.79642 5.05791,12.72532 17.88867,15.77344 3.75284,0.89151 3.95955,0.63571 5.16211,0.71874 1.20255,0.083 2.4462,0.13998 3.9082,0.19336 145.74499,1.65188 299.16752,-0.25155 433.40427,-0.17382 l 0.02,0 0.019,0 c 11.581,0 17.5407,3.37544 22.6915,9.71289 5.147,6.33297 9.0952,17.14961 9.1953,32.01172 0.041,15.42815 -3.8972,27.98599 -9.291,35.39453 -5.3983,7.41456 -10.9988,10.92704 -22.2598,10.95898 -102.31984,-0.0547 -204.63934,-0.27833 -306.95896,-0.51758 l 0,43.86524 c 133.8269,0.21333 267.65356,0.5556 401.48046,0.67968 10.1674,0.0145 15.9103,3.66039 21.6875,11.96876 5.7771,8.30836 9.8466,21.86109 9.7988,36.92187 l 0,0.002 c -0.051,16.11428 -3.4929,30.10758 -8.5898,38.04688 -5.097,7.93929 -9.7577,11.6192 -23.1329,11.71094 -139.1349,-0.0583 -273.22726,-0.3578 -401.24601,-0.19336 l 0,43.83984 c 114.25945,0.004 228.51814,0.16462 342.77731,0.0449 11.6128,0.30295 18.9308,5.39886 24.918,15.54883 6.1567,10.43751 9.1047,26.55943 5.5996,43.73047 l 0,0.01 0,0.008 c -2.3514,11.56719 -6.1175,18.78829 -10.1524,22.72659 -4.0349,3.9383 -8.8914,6.5273 -20.6426,6.5273 l -342.56046,0 c 0.0424,14.6801 0.0883,29.361 0.13282,44.041 86.66175,0.032 173.32711,-0.5961 259.98434,-0.062 l 0.01,0 0.01,0 c 9.7538,0.064 13.9349,2.49 17.8125,7.3789 3.8776,4.889 7.3879,14.3123 7.1953,29.2949 l 0,0.01 0,0.01 c -0.1771,14.5988 -3.6476,23.0815 -7.1933,27.2968 -3.5437,4.2129 -7.6095,6.5479 -17.7598,6.6172 -182.22213,0.9282 -364.51271,0.6842 -546.75387,0 l -0.008,0 -0.008,0 c -44.10818,-0.1346 -78.95518,-11.8165 -106.18165,-34.1367 -27.22646,-22.3201 -47.57067,-56.1048 -59.14257,-103.65037 l -0.006,-0.0176 -0.004,-0.0195 c -8.02801,-32.74318 -8.769,-58.64094 -8.92578,-93.44727 l 0,-0.0254 c -0.15947,-47.76219 -1.07527,-99.83224 11.94336,-141.22461 13.34354,-42.29217 34.92301,-74.38541 65.81445,-95.5957 l 0.002,-0.002 C 422.99868,636.11665 626.41263,495.04961 682.7207,458.58398 l 0.002,-0.002 c 5.28709,-3.40872 9.66808,-4.85905 12.92382,-5.17969 0.54272,-0.0534 1.05411,-0.0756 1.53321,-0.0703 z
  </xsl:variable>

  <xsl:variable name="messageEventCatchIcon">
    m 0,155.99997 c 0,213.36589 8.677966,331.27411 8.677966,544.64001 279.750004,0 420.652544,0 700.402544,0 0,-213.3659 8.67797,-322.59615 8.67797,-535.96204 -279.75,0 -438.00848,-8.67797 -717.75848,-8.67797 z m 125.68783,56.61408 c 155.43814,0 310.87631,0 466.31441,0 -82.5961,50.82447 -165.1995,162.39825 -247.7853,174.57169 C 271.37659,328.99182 198.53197,270.80323 125.68783,212.61405 z m 535.58158,6.80525 c 0,127.07227 0,297.53431 0,424.60661 -213.1641,0 -382.9383,0 -596.102365,0 0,-127.0625 0,-297.51482 0,-424.57732 C 171.71782,304.60095 243.55192,372.40335 350.10747,457.54975 456.72557,372.39355 554.65611,304.5814 661.26941,219.4193 z
  </xsl:variable>

  <xsl:variable name="messageEventThrowIcon">
    m 35.433071,207.56236 c 0,191.38997 0,382.77992 0,574.16992 280.001319,0 560.002649,0 840.003949,0 0,-187.4876 0,-374.97526 0,-562.46289 -137.5944,111.6237 -275.1888,223.24739 -412.7832,334.87109 C 320.2469,438.61448 177.83999,323.0884 35.433071,207.56236 Z m 77.580069,-65.83008 c 116.54689,94.54687 233.09379,189.09375 349.64068,283.64062 116.5469,-94.54687 233.0937,-189.09375 349.6406,-283.64062 -233.0938,0 -466.18752,0 -699.28128,0 z
  </xsl:variable>

  <xsl:variable name="receiveTaskIcon">
    m 353.77539,495.84961 0,694.62499 1052.97461,0 0,-694.62499 z m 108.98242,45.44336 835.00779,0 -417.50388,274.67187 z m -63.54101,12.58984 481.04492,316.47657 481.04488,-316.47657 0,591.15039 -962.0898,0 z
  </xsl:variable>
  
  <xsl:variable name="parallelGatewayIcon">
    m 1002.1543,555.2207 c 0,0.01 -9.65727,1.83789 -9.66211,1.83789 -0.01,0 -7.90826,5.32227 -7.91407,5.32227 -0.01,0.01 -5.49711,8.12695 -5.50195,8.12695 0,0.01 -1.85941,9.67578 -1.86328,9.67578 l 0,396.85157 -397.02539,0 -0.0664,-0.0781 c 0,0.01 -9.61821,2.12891 -9.62304,2.12891 -0.01,0.01 -7.90727,5.32226 -7.91211,5.32226 l 0.008,-0.0976 c -0.01,0.01 -5.49712,8.12695 -5.50195,8.12695 -0.01,0 -1.86137,9.67582 -1.86524,9.67582 l 0,43.4707 c 0,0 1.84827,9.7819 1.91406,9.8496 0,0.01 5.47852,7.9336 5.47852,7.9336 0.0101,0.01 7.95705,5.3223 7.95898,5.3223 0.01,0.01 9.65629,2.1269 9.66016,2.1269 l 396.97852,0 0,396.7852 -0.0625,-0.059 c 0,0.01 1.91599,9.8496 1.91406,9.8496 0,0.01 5.47656,7.9336 5.47656,7.9336 0.0101,0.01 7.959,5.3203 7.96094,5.3203 0,0 9.59054,2.0321 9.66214,2.1289 l 43.4609,-0.01 c 0.011,0 9.8463,-2.0312 9.8516,-2.0312 0,0 7.9082,-5.3203 7.914,-5.3203 0.01,-0.01 5.4708,-7.9356 5.4766,-7.9356 0,0 1.8867,-9.8203 1.8906,-9.8203 l 0,-396.8027 396.8789,0 c 0.011,0.01 9.8467,-2.0313 9.8516,-2.0313 0.01,0 7.9092,-5.3222 7.914,-5.3222 0.01,-0.01 5.4708,-7.9336 5.4766,-7.9336 0,0 1.8858,-9.8204 1.8906,-9.8204 l 0,-43.4609 c 0,-0.01 -1.8779,-9.57904 -1.875,-9.67579 -0.01,-0.01 -5.4498,-8.12695 -5.5175,-8.12695 -0.01,-0.01 -7.9581,-5.32032 -7.961,-5.32032 0,0.01 -9.8194,-2.12995 -9.8242,-2.0332 l -396.8379,0 0,-396.8418 c 0,-0.01 -1.8779,-9.57707 -1.875,-9.67383 -0.01,-0.01 -5.4492,-8.1289 -5.4492,-8.1289 -0.01,-0.01 -7.9571,-5.32031 -7.959,-5.32031 -0.01,0 -9.8474,-1.93555 -9.8516,-1.93555 z
  </xsl:variable>
  
  <xsl:variable name="scriptTaskIcon">
    m 609.02539,514.59375 -4.62695,2.74805 -1.51172,0.89648 c -65.42017,38.76909 -110.44116,74.32034 -140.16016,109.03516 -29.76015,34.76286 -44.14948,69.76512 -44.55859,103.50781 -0.81416,67.15047 48.71676,117.47208 92.66211,162.47852 43.91192,44.97215 83.08282,87.38448 86.76953,122.48433 1.87909,17.8903 -2.35053,35.8588 -21.85742,60.4629 -19.42482,24.5007 -54.67916,53.8468 -111.04297,86.7032 l -83.21289,48.5039 513.57031,0 6.07813,-3.5352 0.006,-0.01 c 59.73132,-34.8195 99.97333,-66.9878 125.61533,-99.33 25.7183,-32.4387 36.1804,-66.3775 32.8339,-98.2383 -6.6307,-63.1295 -58.0554,-109.13068 -101.28907,-153.40822 -43.26718,-44.31189 -78.33879,-86.10665 -77.86133,-125.48632 0.24109,-19.88014 7.8822,-42.05777 32.05078,-70.28907 24.12719,-28.18307 64.80779,-61.15655 127.17192,-98.11328 l 0,-0.002 81.6914,-48.41211 z m 14.47461,52.04687 316.2832,0 c -27.85557,20.76926 -49.88108,40.8347 -66.83008,60.63282 -29.76012,34.76286 -44.14751,69.76511 -44.55664,103.50781 -0.81411,67.15104 48.71894,117.47234 92.66407,162.47852 43.91168,44.97187 83.07885,87.38381 86.76565,122.48433 1.8791,17.8904 -2.3487,35.8588 -21.8555,60.4629 -18.70677,23.5949 -52.34894,51.7499 -105.18945,83.1602 l -317.0918,0 c 21.99697,-17.1554 39.47182,-33.9655 52.83789,-50.8242 25.71836,-32.4387 36.18243,-66.3756 32.83594,-98.2364 C 642.7325,947.1772 591.30589,901.17402 548.07227,856.89648 504.8051,812.5846 469.73545,770.7918 470.21289,731.41211 c 0.24104,-19.8801 7.88224,-42.05776 32.05078,-70.28906 23.31699,-27.2366 62.32854,-59.01093 121.23633,-94.48243 z m -74.00586,88.76954 0,24.04101 247.24609,0 0,-24.04101 z m -9.1875,129.49218 0,24.04102 255.64063,0 0,-24.04102 z m 115.83398,129.48828 0,24.04102 247.57813,0 0,-24.04102 z m 35.4004,129.49218 0,24.0391 256.53906,0 0,-24.0391 z 
  </xsl:variable>

  <xsl:variable name="serviceTaskIcon">
    m 502.79102,430.86719 c -0.029,23.16793 0.007,46.33803 0.0664,69.50586 -19.75822,5.58977 -37.81291,13.3895 -54.86328,22.94531 l -49.83398,-49.23438 -93.17969,93.72071 49.82812,49.22461 c -9.62553,17.25239 -17.14126,35.5876 -22.38671,54.63086 l -70.58594,0.1289 0,131.99219 71.32226,-0.25586 c 6.5223,25.86049 20.71775,49.10841 34.85547,69.89649 l 0,-113.85547 -62.3457,0.22265 0,-44.24804 62.03515,-0.11133 3.48243,-17.64063 c 5.07807,-25.72661 15.13518,-50.25154 29.58398,-72.19531 l 9.88477,-15.01172 -43.58594,-43.05664 31.36914,-31.55078 43.66406,43.13867 14.85938,-9.91601 c 22.70793,-14.98185 47.5452,-25.02345 72.3125,-30.25391 l 17.50781,-3.63477 -0.1582,-60.60547 45.06445,0 -0.34766,60.28907 17.80078,0 100.44532,0 c -11.09189,-13.23987 -57.05937,-30.46883 -74.20899,-35.26953 l 0.39844,-68.85352 c -49.09303,-0.001 -87.58233,0.002 -132.98437,-0.002 z M 627.5,557.14258 l 0.18164,69.50195 c -19.75895,5.58977 -37.81442,13.3904 -54.86523,22.94727 l -49.83399,-49.23438 -93.18164,93.7207 49.83008,49.22461 c -9.62596,17.25334 -17.14323,35.58672 -22.38867,54.63086 l -70.58399,0.12696 0,131.99414 71.32227,-0.25586 c 5.61403,19.5727 13.39624,37.44691 22.93359,54.32031 l -51.02929,50.83006 94.21875,92.5996 50.84765,-50.6113 c 17.41694,9.6713 35.9307,17.1805 55.125,22.4063 l 0.0332,71.832 c 45.33069,0.3969 93.59252,0.1916 132.33398,0.1875 l 0,-72.6817 c 19.77359,-5.5754 37.88508,-13.4324 54.93945,-22.9843 l 50.80664,50.0937 93.24219,-93.6035 -50.91211,-50.17188 c 9.64765,-17.29877 17.16457,-35.6625 22.41602,-54.72656 l 69.11716,-0.42773 0,-131.86524 -69.89255,0.42383 c -5.6184,-19.59148 -13.28919,-37.34286 -22.95313,-54.32812 L 957.5,692.67578 863.33398,599.91797 815.10352,648.31445 c -17.34555,-9.59252 -35.7847,-17.0838 -54.9043,-22.3164 l 0.39648,-68.85547 -133.09375,0 z m 43.94727,43.83398 45.06445,0 -0.34766,60.29102 17.79883,3.49219 c 25.84421,5.0715 50.61607,15.11631 72.64844,29.47656 l 14.91797,9.72461 42.08984,-42.23242 31.69727,31.2207 -42.15039,42.25781 10.14257,14.96875 c 14.88113,22.46311 24.75154,46.74626 30.32422,71.75 l 3.66602,17.53906 60.91601,-0.36914 0,44.20118 -60.57617,0.37304 -3.47851,17.54102 c -5.10956,25.75548 -15.14566,50.27988 -29.61914,72.26172 l -9.89454,15.03125 44.6211,43.97269 -31.36328,31.4843 -44.625,-44 -14.83594,9.8555 c -22.71504,14.8447 -47.35354,25.1247 -72.21289,30.1895 l -17.625,3.5195 0,64.0058 c -12.04845,0.03 -25.19108,0.01 -44.68945,-0.017 l -0.0293,-63.5254 -17.70313,-3.4571 c -25.88034,-5.0538 -50.60932,-15.0522 -72.60351,-29.4726 l -14.875,-9.7539 -44.79688,44.5918 -31.70117,-31.1563 44.85938,-44.68551 -10.23633,-15.03125 C 511.93509,952.6096 502.0822,928.30086 496.5,903.32617 l -3.66406,-17.48633 -62.34571,0.22266 0,-44.24609 62.03516,-0.11329 3.48242,-17.63867 c 5.07811,-25.7268 15.13538,-50.25378 29.58399,-72.19726 l 9.88476,-15.01172 -43.58594,-43.05664 31.36719,-31.55078 43.66602,43.13867 14.85937,-9.91406 c 22.73674,-14.88428 46.86454,-24.76034 72.3125,-30.25391 l 17.50782,-3.63477 z m 23.27148,161.05664 c -54.25711,0 -98.71094,44.45573 -98.71094,98.71289 0,54.25717 44.45383,98.70899 98.71094,98.70899 54.25711,0 98.71094,-44.45182 98.71094,-98.70899 0,-54.25716 -44.45383,-98.71289 -98.71094,-98.71289 z m 0,43.83399 c 30.56804,0 54.87891,24.31093 54.87891,54.8789 0,30.56798 -24.31087,54.87696 -54.87891,54.87696 -30.56804,0 -54.87695,-24.30898 -54.87695,-54.87696 0,-30.56797 24.30891,-54.8789 54.87695,-54.8789 z
  </xsl:variable>

  <xsl:variable name="sendTaskIcon">
    m 346.85742,519.5957 499.92774,283 499.92574,-283 z m 1.2461,104.72266 0,558.52144 999.85348,0 0,-558.52144 -501.17184,279.25976 z
  </xsl:variable>

  <xsl:variable name="signalEventCatchIcon">
    M 393.46484,0 C 262.30972,235.41657 131.15497,470.8333 0,706.25 c 262.30988,0 524.61974,0 786.92964,0 C 655.77474,470.8333 524.61974,235.41667 393.46484,0 z m 0,102.73633 c 102.7917,184.50456 205.5833,369.00907 308.375,553.51367 -205.5833,0 -411.16667,0 -616.75,0 102.79167,-184.5046 205.58333,-369.00911 308.375,-553.51367 z
  </xsl:variable>

  <xsl:variable name="signalEventThrowIcon">
    M 393.4375,0 C 262.55091,235.60222 131.10501,470.894 0,706.375 c 262.29167,0 524.5833,0 786.875,0 C 655.7292,470.9167 524.5833,235.45833 393.4375,0 Z
  </xsl:variable>

  <xsl:variable name="timerEventIcon">
    m 491.54975,113.21091 c -171.52852,1.21202 -335.10028,113.88462 -398.318318,273.28516 -65.7902,155.80082 -29.419078,347.56962 88.884758,468.44142 115.0263,123.8297 304.55446,169.46411 463.30666,111.4277 166.3595,-56.6747 288.5728,-221.2307 292.9765,-397.1894 8.8625,-170.94524 -93.8743,-339.96923 -248.5585,-412.56644 -59.0266,-28.5789 -124.5452,-43.4576 -190.1172,-43.3711 -2.7264,-0.0372 -5.4512,-0.0466 -8.1739,-0.0273 z m 15.6426,79.97266 c 156.7062,0.70105 303.7146,116.51969 340.2578,269.17578 40.6988,149.01854 -27.8705,318.93594 -161.3613,397.01564 -136.3004,85.5433 -328.47043,62.7447 -440.39453,-53.1094 -118.11394,-113.9435 -139.14739,-311.08537 -48.53125,-447.73437 64.1785,-101.9465 182.05034,-166.46657 302.56058,-165.29297 2.4918,-0.0472 4.9813,-0.0658 7.4687,-0.0547 z m 105.9512,102.07226 c -39.9674,72.5091 -79.935,145.01824 -119.9024,217.52734 -37.03237,3.9378 -45.82998,60.12112 -11.6621,75.10552 22.5699,15.6663 40.2472,-10.3922 60.793,-9.4942 l 183.293,0 0,-50 -183.9375,0 c 38.4017,-69.66799 76.8034,-139.33589 115.2051,-209.00389 -14.5964,-8.0449 -29.1927,-16.08987 -43.7891,-24.13477 z
  </xsl:variable>

  <xsl:variable name="userTaskIcon">
    M 991.75,365 C 814.47261,365 687.65259,501.28069 687.29688,656.51367 l 0,0.0449 0,0.043 c 0.0112,46.94485 12.71406,96.48986 32.56054,141.17774 14.33025,32.26709 32.14702,61.93151 53.85742,85.69531 C 644.68593,927.61788 493.52222,1000.2699 417.11719,1143.5293 L 413,1151.25 l 0,463.75 1157.5,0 0,-463.75 -4.1172,-7.7207 c -75.3061,-141.1989 -223.1988,-213.74978 -350.9805,-258.07618 62.3794,-63.43263 80.7808,-145.30578 80.8008,-228.85156 l 0,-0.043 0,-0.0449 C 1295.8474,501.2807 1169.0274,365 991.75,365 z M 870.70898,530.29883 c 8.19443,0.0206 17.19117,0.29432 27.10938,0.8789 79.02308,4.65771 105.61894,18.88222 126.06254,32.37305 20.4435,13.49083 34.8553,26.33317 88.8828,27.9082 l 0.016,0 0.018,0 c 42.1017,-1.5748 62.3491,-9.0811 76.8691,-17.58398 5.8837,-3.44545 10.8241,-7.02722 15.8867,-10.44727 13.3814,28.68227 20.5671,60.38893 20.6485,93.20313 -0.028,93.43999 -16.0919,158.87652 -101.7676,212.42383 l 8.3809,63.16992 c 17.8327,5.41537 36.2658,11.35733 54.9746,17.87305 2.6128,10.90011 5.6316,25.19633 7.6621,40.39453 2.1271,15.92241 2.9637,32.48881 1.6523,44.92771 -1.3114,12.4389 -5.1734,19.1539 -5.8515,19.8321 -43.5469,43.5468 -120.8464,68.9433 -199.252,68.9433 -78.40557,0 -155.70507,-25.3965 -199.25195,-68.9433 -0.67811,-0.6782 -4.54017,-7.3932 -5.85157,-19.8321 -1.31139,-12.4389 -0.47481,-29.0053 1.65235,-44.92771 2.04162,-15.28211 5.08445,-29.66261 7.70703,-40.58399 18.52194,-6.44112 36.76978,-12.32071 54.42969,-17.68359 l 4.99609,-67.6875 c -4.068,-5.21708 -8.21297,-8.67324 -13.16016,-12.38477 -19.12128,-14.34532 -42.30994,-45.90379 -58.68945,-82.78515 -16.37367,-36.86821 -26.51791,-79.00946 -26.5332,-112.74414 0.10174,-40.44431 10.98577,-79.211 30.97656,-112.69727 3.57689,-1.33139 7.34871,-2.73853 11.58789,-4.13281 14.90781,-4.90324 35.33655,-9.58342 70.8457,-9.49414 z M 719.36133,979.85547 c -0.0629,0.45883 -0.13379,0.90664 -0.19531,1.36719 -2.58689,19.36354 -4.10696,40.44054 -1.88282,61.53714 2.22414,21.0966 7.79068,43.8102 25.96875,61.9882 62.04773,62.0478 155.95092,89.4473 248.74805,89.4473 92.7971,0 186.7003,-27.3995 248.748,-89.4473 18.1781,-18.178 23.7447,-40.8916 25.9688,-61.9882 2.2241,-21.0967 0.7041,-42.1736 -1.8828,-61.53714 -0.051,-0.38161 -0.1102,-0.75238 -0.1621,-1.13282 94.3337,41.36896 185.5148,100.61546 235.8281,189.07426 l 0,375.8359 -167.5,0 0,-265 -70,0 0,265 -544,0 0,-265 -70,0 0,265 -166,0 0,-375.8359 c 50.40811,-88.6256 141.83798,-147.9292 236.36133,-189.30863 z
  </xsl:variable>

  <!-- END ICONS -->

  <!-- ACTIVITY MARKERS -->
  <xsl:variable name="adHocMarker">
    m 300.00001,92.232481 c 58.33243,-138.228514 134.89425,-282.230651 266.546,-360.085751 97.7838,-58.39135 218.70131,-22.41896 308.42844,34.81898 138.18715,85.20714 246.29115,211.842759 382.60565,299.507821 82.3352,48.264229 184.7331,8.717415 244.7474,-58.0565283 C 1574.729,-76.028426 1657.5431,-155.60546 1700,-261.56396 c 0,110.01254 0,220.025092 0,330.037636 C 1638.6687,190.14392 1559.7687,316.62637 1433.608,375.64299 1330.3799,420.08262 1210.4599,393.43191 1121.0842,329.05734 990.06485,241.07844 893.59804,105.3675 751.22975,34.277737 682.05792,-1.7268886 593.85297,6.7321249 535.89898,60.900295 431.41199,153.40413 371.53434,284.22024 300.00001,402.36216 c 0,-103.37656 0,-206.75312 0,-310.129679 z
  </xsl:variable>

  <xsl:variable name="compensationMarker">
    m 1003.1719,-427.54214 c -39.60796,5.794 -68.12868,38.56118 -102.1447,57.51603 -186.37683,125.49923 -373.21723,250.34472 -559.3042,376.25275 -35.93279,27.59579 -23.84863,86.43052 16.0756,103.58172 206.32507,138.4236 412.21958,277.524 618.81387,415.5246 41.83143,24.8132 96.65733,-16.1808 88.32893,-63.0759 0,-105.8146 0,-211.6291 0,-317.4437 188.9638,126.7544 377.4965,254.1864 566.7296,380.5177 41.8315,24.8133 96.6575,-16.1808 88.329,-63.076 -0.2007,-278.6032 0.4013,-557.22646 -0.3009,-835.81704 -2.7279,-48.53139 -67.3754,-71.26567 -101.6214,-38.12699 -184.3787,123.91483 -368.7575,247.82966 -553.1363,371.74449 -0.7888,-113.15884 1.5844,-226.51615 -1.1984,-339.55004 -5.2041,-28.0178 -32.0958,-49.35538 -60.5711,-48.04762 z m -58.23049,172.58399 c 0,204.92837 0,409.85671 0,614.78511 C 792.48112,257.36276 640.02084,154.89866 487.56055,52.43446 640.02084,-50.02974 792.48112,-152.49395 944.94141,-254.95815 Z M 1600,-254.9562 c 0,204.92709 0,409.85416 0,614.78126 C 1447.5397,257.36156 1295.0794,154.89796 1142.6191,52.43446 1295.0794,-50.02909 1447.5397,-152.49265 1600,-254.9562 Z  
  </xsl:variable>

  <xsl:variable name="parallelMiMarker">
    M 500 400 L 500 1600 L 700 1600 L 700 400 L 500 400 z M 900 400 L 900 1600 L 1100 1600 L 1100 400 L 900 400 z M 1300 400 L 1300 1600 L 1500 1600 L 1500 400 L 1300 400 z
  </xsl:variable>

  <xsl:variable name="sequentialMiMarker">
    M 400 500 L 400 700 L 1600 700 L 1600 500 L 400 500 z M 400 900 L 400 1100 L 1600 1100 L 1600 900 L 400 900 z M 400 1300 L 400 1500 L 1600 1500 L 1600 1300 L 400 1300 z
  </xsl:variable>

  <xsl:variable name="standardLoopMarker">
    m 1057.0703,-536.8019 c -251.96066,-3.53538 -493.62301,172.22866 -565.93613,414.14756 -55.58419,173.97657 -23.10548,373.061 85.52208,520.0263 -89.69531,-17.3633 -179.39063,-34.7266 -269.08594,-52.0899 -7.60156,39.2715 -15.20313,78.543 -22.80469,117.8145 161.28841,31.2213 322.57683,62.4427 483.86524,93.664 31.22917,-159.9954 62.45833,-319.9909 93.6875,-479.9863 -39.25977,-7.6628 -78.51953,-15.3255 -117.7793,-22.9883 -18.95378,97.1055 -37.90755,194.2109 -56.86133,291.3164 -138.7329,-165.6011 -136.72949,-427.77296 4.36682,-591.37897 137.41756,-171.71599 399.20335,-221.00758 590.73365,-114 183.2317,94.56792 284.8875,318.59636 234.8961,518.74577 -44.7698,208.9108 -247.4038,367.3398 -460.604,363.0532 -55.485,-3.9353 -83.37472,76.1957 -37.4367,107.5608 40.1043,24.9864 90.8465,7.3636 134.8085,4.4747 248.1809,-37.7478 457.5194,-249.4518 489.5199,-498.9095 36.9941,-238.02524 -91.3838,-488.93562 -304.8033,-600.24159 -86.1681,-46.76847 -184.0681,-71.36408 -282.0884,-71.20867 z
  </xsl:variable>

  <xsl:variable name="subProcessMarker">
    m 300,300 0,50 0,1350 1400,0 0,-1400 z m 88,88 1224,0 0,1224 -1224,0 z m 522,212 0,310 -310,0 0,180 310,0 0,310 180,0 0,-310 310,0 0,-180 -310,0 0,-310 z
  </xsl:variable>
  <!--  END ACTIVITY MARKERS -->

</xsl:stylesheet>
