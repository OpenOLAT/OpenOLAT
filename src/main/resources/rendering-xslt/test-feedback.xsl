<?xml version="1.0" encoding="UTF-8"?>
<!--

Renders the final test feedback.

(This is currently only shown in multi-part tests. Single part tests
combine the feedback for the test and the testPart.)

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

  <!-- This test -->
  <xsl:variable name="assessmentTest" select="/*[1]" as="element(qti:assessmentTest)"/>

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
      <body><div class="qtiworks assessmentTest testFeedback">
        <xsl:call-template name="maybeAddAuthoringLink"/>

        <h1>Test Complete</h1>

        <!-- Show 'atEnd' feedback for the test -->
        <xsl:apply-templates select="qti:testFeedback[@access='atEnd']"/>

        <!-- Test session control -->
        <xsl:call-template name="qw:test-controls"/>
       </div></body>
    </html>
  </xsl:template>

  <xsl:template name="qw:test-controls">
    <ul class="sessionControl">
      <li>
        <form action="{$webappContextPath}{$exitTestUrl}" method="post" target="oaa0"
          onsubmit="return confirm({qw:to-javascript-string($exitTestAlertMessage)})">
          <input type="submit" value="Exit Test" class="btn btn-default"/>
        </form>
      </li>
    </ul>
  </xsl:template>

</xsl:stylesheet>
