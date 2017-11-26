<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns="http://www.w3.org/1999/xhtml">
  <xsl:template match="page">
    <html>
      <body>
        <p>
          Current version of the application is
          <xsl:value-of select="version/name"/>
        </p>
      </body>
    </html>
  </xsl:template>
</xsl:stylesheet>