<!--
  Copyright 2011-2018 Tim Stephenson and contributors
  
   Licensed under the Apache License, Version 2.0 (the "License"); you may not
   use this file except in compliance with the License.  You may obtain a copy
   of the License at
  
     http://www.apache.org/licenses/LICENSE-2.0
  
   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
   WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
   License for the specific language governing permissions and limitations under
   the License.
-->
<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
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

  <xsl:output method="html" encoding="utf-8" indent="yes" />
  
  <xsl:param name="baseUrl">https://api.omny.link/</xsl:param>
  <xsl:param name="tenantId"/>

  <xsl:template match="/">
    <xsl:text disable-output-escaping='yes'>&lt;!DOCTYPE html&gt;</xsl:text>
		<html>
		<head>
		  <meta http-equiv="x-ua-compatible" content="IE=Edge"/> 
		  <!-- as the **very** first line just after head-->  
		  <meta charset='utf-8'/>
      <title>Omny Link Message API</title>
		  <link href="/webjars/bootstrap/3.3.5/css/bootstrap.min.css" rel="stylesheet"/>
		  <link href="/webjars/bootstrap/3.3.5/css/bootstrap-theme.min.css" rel="stylesheet"/>
		  <link href="/css/omny-1.0.0.css" rel="stylesheet"/>
		  <link href="/css/bpmn-icons/css/bpmn.css" rel="stylesheet"/>
		  <link rel="icon" type="image/png" href="/images/icon/omny-icon-16x16.png" />
		</head>
		<body>
			<div class="clearfix" id="messages"></div>
		  <div class="container" id="container"></div>
		  <script id='template' type='text/ractive'>
		    {{>profileArea}}
		    {{>titleArea}}
   		  
   		  <xsl:apply-templates select="//bpmn:message"/>
    		  
		    {{>poweredBy}}
		    {{>sidebar { active: 'messages.html' }}}
		  </script>
		
		  <script src="/webjars/jquery/1.11.1/jquery.min.js"></script>
		  <script src="/webjars/bootstrap/3.3.5/js/bootstrap.min.js"></script>
		  <script src="/webjars/Bootstrap-3-Typeahead/3.1.1/bootstrap3-typeahead.js"></script>
		  <script src="/webjars/ractive/0.7.3/ractive.min.js"></script>
		
		  <script src="/js/autoNumeric.min.js"></script>
		  <script src="/js/md5.min.js"></script>
		  <script src="/js/activity-1.0.0.js"></script>
		  <script src="/js/string-functions-0.1.0.js"></script>
		  <script src="/js/login-1.0.0.js"></script>
		  <script src="/js/i18n.js"></script>
		  <script src="/js/messages-1.0.0.js"></script>
		  <script src="/js/omny-1.0.0.js"></script>
		</body>
		</html>
  </xsl:template>
  
  <xsl:template match="bpmn:ioSpecification">
    <tr>
      <th>Payload: </th>
      <td>
        <xsl:for-each select="bpmn:dataInput">
          <xsl:value-of select="@name"/>
          <br/>       
        </xsl:for-each>
      </td>
	  </tr>
  </xsl:template>
  
  <xsl:template match="bpmn:message">
    <table class="table">
      <tbody>
        <tr>
          <th>Message: </th>
          <td><xsl:value-of select="@id"/></td>
        </tr>
        <tr>
          <th>Documentation: </th>
          <td><xsl:value-of select="//bpmn:process/bpmn:documentation[@textFormat='text/plain']"/></td>
        </tr>
        <tr>
          <th>Endpoint: </th>
          <td><xsl:value-of select="$baseUrl"/>/messages/<xsl:value-of select="@id"/></td>
        </tr>
        <tr>
          <th>Method: </th>
          <td>POST</td>
        </tr>
        <xsl:apply-templates select="//bpmn:process/bpmn:ioSpecification"/>
        <tr>
          <th>Return codes: </th>
          <td>
            <code>201</code> - Message received ok. Location response header contains a reference that you may wish to keep in case of follow-ups.<br/>
            <code>400</code> - Bad request, probably the JSON is not as expected, more details may be found in the response body.<br/>
            <code>404</code> - The process handling the message has been suspended or deleted, please contact Support.<br/>
            <code>406</code> - The Accept header cannot be fulfilled. Only <code>application/json</code> is currently supported.<br/>
            <code>415</code> - The Content-Type header cannot be fulfilled. Only <code>application/json</code> is currently supported.
          </td>
        </tr>
        <tr>
          <th>Example using curl: </th>
          <td><code><xsl:value-of select="//bpmn:process/bpmn:documentation[@textFormat='text/curl']"/></code></td>
        </tr>
      </tbody>
    </table>
  </xsl:template>
  

</xsl:stylesheet>
