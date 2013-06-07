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
    
  <!-- Note: An imported style sheet has lower precedence than the importing style sheet. -->
  <xsl:import href="ActivitiSupportRules.xsl"/>
  
  <xsl:template match="/">
		<xsl:apply-templates/>
	</xsl:template>

</xsl:stylesheet>