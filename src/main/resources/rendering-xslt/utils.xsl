<?xml version="1.0" encoding="UTF-8"?>
<!--

General utility templates

-->
<xsl:stylesheet version="2.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:qti="http://www.imsglobal.org/xsd/imsqti_v2p1"
  xmlns:qw="http://www.ph.ed.ac.uk/qtiworks"
  xmlns="http://www.w3.org/1999/xhtml"
  xpath-default-namespace="http://www.w3.org/1999/xhtml"
  exclude-result-prefixes="xs qti qw">

  <!-- ************************************************************ -->
  <!-- JavaScript string helpers -->

  <xsl:function name="qw:escape-for-javascript-string" as="xs:string">
    <xsl:param name="input" as="xs:string?"/>
    <xsl:sequence select="replace(replace($input, '[&#x0d;&#x0a;]', ''), '('')', '\\$1')"/>
  </xsl:function>

  <xsl:function name="qw:to-javascript-string" as="xs:string">
    <xsl:param name="input" as="xs:string"/>
    <xsl:sequence select="concat('''', qw:escape-for-javascript-string($input), '''')"/>
  </xsl:function>

  <xsl:function name="qw:to-javascript-arguments" as="xs:string">
    <xsl:param name="inputs" as="xs:string*"/>
    <xsl:sequence select="string-join(for $string in $inputs return qw:to-javascript-string($string), ', ')"/>
  </xsl:function>

  <!-- ************************************************************ -->
  <!-- QTI 2.0 to 2.1 -->

  <xsl:function name="qw:to-qti21" as="document-node()">
    <xsl:param name="input" as="document-node()"/>
    <xsl:variable name="root-element" select="$input/*[1]" as="element()"/>
    <xsl:variable name="root-namespace" select="namespace-uri($root-element)" as="xs:anyURI"/>
    <xsl:choose>
      <xsl:when test="$root-namespace='http://www.imsglobal.org/xsd/imsqti_v2p1'">
        <xsl:sequence select="$input"/>
      </xsl:when>
      <xsl:when test="$root-namespace='http://www.imsglobal.org/xsd/imsqti_v2p0'">
        <xsl:sequence select="()"/>
        <!-- Convert QTI 2.0 to QTI 2.1 -->
        <xsl:document>
          <xsl:apply-templates select="$root-element" mode="qti20-to-21"/>
        </xsl:document>
      </xsl:when>
      <xsl:otherwise>
        <xsl:message terminate="yes">
          Unexpected namespace URI '<xsl:value-of select="$root-namespace"/>' for root element
        </xsl:message>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:function>

  <xsl:template match="*" mode="qti20-to-21">
    <xsl:element name="{local-name()}" namespace="http://www.imsglobal.org/xsd/imsqti_v2p1">
      <xsl:copy-of select="@*"/>
      <xsl:apply-templates mode="qti20-to-21"/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="text()" mode="qti20-to-21">
    <xsl:copy/>
  </xsl:template>

</xsl:stylesheet>
