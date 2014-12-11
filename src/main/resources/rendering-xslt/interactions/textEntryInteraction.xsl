<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="2.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:qti="http://www.imsglobal.org/xsd/imsqti_v2p1"
  xmlns:qw="http://www.ph.ed.ac.uk/qtiworks"
  xmlns="http://www.w3.org/1999/xhtml"
  exclude-result-prefixes="xs qti qw">

  <xsl:template match="qti:textEntryInteraction">
    <xsl:variable name="is-bad-response" select="qw:is-bad-response(@responseIdentifier)" as="xs:boolean"/>
    <xsl:variable name="is-invalid-response" select="qw:is-invalid-response(@responseIdentifier)" as="xs:boolean"/>
    <input name="qtiworks_presented_{@responseIdentifier}" type="hidden" value="1"/>
    <span class="{local-name()}">
      <xsl:variable name="responseDeclaration" select="qw:get-response-declaration(/, @responseIdentifier)" as="element(qti:responseDeclaration)?"/>
      <xsl:variable name="responseValue" select="qw:get-response-value(/, @responseIdentifier)" as="element(qw:responseVariable)?"/>
      <xsl:variable name="responseInput" select="qw:get-response-input(@responseIdentifier)" as="element(qw:responseInput)?"/>
      <xsl:variable name="responseInputString" select="qw:extract-single-cardinality-response-input($responseInput)" as="xs:string?"/>
      <xsl:variable name="checks" as="xs:string*">
        <xsl:choose>
          <xsl:when test="$responseDeclaration/@baseType='float'"><xsl:sequence select="'float'"/></xsl:when>
          <xsl:when test="$responseDeclaration/@baseType='integer'"><xsl:sequence select="'integer'"/></xsl:when>
        </xsl:choose>
        <xsl:if test="@patternMask">
          <xsl:sequence select="('regex', @patternMask)"/>
        </xsl:if>
      </xsl:variable>
      <xsl:variable name="checkJavaScript" select="concat('QtiWorksRendering.validateInput(this, ',
        qw:to-javascript-arguments($checks),
        ')')" as="xs:string"/>

      <input type="text" name="qtiworks_response_{@responseIdentifier}">
        <xsl:if test="$isItemSessionEnded">
          <xsl:attribute name="disabled">disabled</xsl:attribute>
        </xsl:if>
        <xsl:if test="$is-bad-response or $is-invalid-response">
          <xsl:attribute name="class" select="'badResponse'"/>
        </xsl:if>
        <xsl:if test="@expectedLength">
          <xsl:attribute name="size" select="@expectedLength"/>
        </xsl:if>
        <xsl:choose>
          <xsl:when test="$is-bad-response">
            <!-- Response won't have been bound to variable, so show raw input -->
            <xsl:attribute name="value" select="$responseInputString"/>
          </xsl:when>
          <xsl:when test="exists($responseValue)">
            <!-- Response has been bound, so show current variable value -->
            <xsl:attribute name="value" select="qw:extract-single-cardinality-value($responseValue)"/>
          </xsl:when>
        </xsl:choose>
        <xsl:if test="exists($checks)">
          <xsl:attribute name="onchange" select="$checkJavaScript"/>
        </xsl:if>
      </input>
      <xsl:if test="$is-bad-response">
        <span class="badResponse">
          You must enter a valid <xsl:value-of select="$responseDeclaration/@baseType"/>!
        </span>
      </xsl:if>
      <xsl:if test="$is-invalid-response">
        <!-- (This must be a regex issue) -->
        <span class="badResponse">
          Your input is not of the required format!
        </span>
      </xsl:if>
    </span>
  </xsl:template>

</xsl:stylesheet>
