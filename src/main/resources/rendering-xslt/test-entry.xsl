<?xml version="1.0" encoding="UTF-8"?>
<!--

Test Entry page (shown when there are multiple testParts)

-->
<xsl:stylesheet version="2.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:qti="http://www.imsglobal.org/xsd/imsqti_v2p1"
  xmlns:m="http://www.w3.org/1998/Math/MathML"
  xmlns:qw="http://www.ph.ed.ac.uk/qtiworks"
  xmlns="http://www.w3.org/1999/xhtml"
  xpath-default-namespace="http://www.w3.org/1999/xhtml"
  exclude-result-prefixes="xs qti qw m">

  <!-- ************************************************************ -->

  <xsl:import href="qti-fallback.xsl"/>
  <xsl:import href="test-common.xsl"/>
  <xsl:import href="utils.xsl"/>

  <!-- ************************************************************ -->

  <xsl:template match="qti:assessmentTest" as="element(html)">
    <html>
      <xsl:if test="@lang">
        <xsl:copy-of select="@lang"/>
        <xsl:attribute name="xml:lang" select="@lang"/>
      </xsl:if>
      <head>
        <title><xsl:value-of select="@title"/></title>
        <xsl:call-template name="includeAssessmentJsAndCss"/>
      </head>
      <body class="qtiworks assessmentTest testEntry">
        <xsl:call-template name="maybeAddAuthoringLink"/>

        <h2>Test Entry Page</h2>

        <p>
          This test consists of
          <xsl:if test="exists(.//qti:preCondition)"><xsl:text> up to </xsl:text></xsl:if>
          <xsl:value-of select="count(qti:testPart)"/>
          parts.
        </p>

        <!-- Test session control -->
        <xsl:call-template name="qw:test-controls"/>
       </body>
    </html>
  </xsl:template>

  <xsl:template name="qw:test-controls">
    <ul class="sessionControl">
      <li>
        <form action="{$webappContextPath}{$advanceTestPartUrl}" method="post" target="oaa0">
          <input type="submit" value="Enter Test" class="btn btn-default"/>
        </form>
      </li>
    </ul>
  </xsl:template>

</xsl:stylesheet>

