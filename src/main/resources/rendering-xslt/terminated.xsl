<?xml version="1.0" encoding="UTF-8"?>
<!--

Renders a terminated assessment

Input document: doesn't matter

-->
<xsl:stylesheet version="2.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:qw="http://www.ph.ed.ac.uk/qtiworks"
  xmlns="http://www.w3.org/1999/xhtml"
  xpath-default-namespace="http://www.w3.org/1999/xhtml"
  exclude-result-prefixes="xs qw">

  <!-- ************************************************************ -->

  <xsl:import href="qti-common.xsl"/>

  <!-- Optional URL for exiting session -->
  <xsl:param name="exitSessionUrl" as="xs:string?" required="no"/>

  <!-- ************************************************************ -->

  <xsl:template match="/" as="element(html)">
    <html lang="en">
      <head>
        <title>Assessment Completed</title>
        <link rel="stylesheet" href="{$webappContextPath}/rendering/css/assessment.css" type="text/css" media="screen"/>
      </head>
      <body><div class="qtiworks">
        <p>
          This assessment is now closed and you can no longer interact with it.
        </p>
        <xsl:if test="exists($exitSessionUrlAbsolute)">
          <p>
            <a href="{$exitSessionUrlAbsolute}">Exit and return</a>
          </p>
        </xsl:if>
      </div></body>
    </html>
  </xsl:template>

</xsl:stylesheet>
