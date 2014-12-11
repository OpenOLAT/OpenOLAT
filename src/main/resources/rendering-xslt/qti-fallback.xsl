<?xml version="1.0" encoding="UTF-8"?>
<!--

Contains QTI-related templates common to both item and test
rendering.

-->
<xsl:stylesheet version="2.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:qti="http://www.imsglobal.org/xsd/imsqti_v2p1"
  xmlns="http://www.w3.org/1999/xhtml"
  xpath-default-namespace="http://www.w3.org/1999/xhtml"
  exclude-result-prefixes="qti">

  <!-- Catch-all for QTI elements not handled elsewhere. -->
  <xsl:template match="qti:*" priority="-10">
    <xsl:message terminate="yes">
      QTI element <xsl:value-of select="local-name()"/> was not handled by a template
    </xsl:message>
  </xsl:template>

</xsl:stylesheet>
