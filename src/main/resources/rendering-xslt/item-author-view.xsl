<?xml version="1.0" encoding="UTF-8"?>
<!--

Renders the author/debug view of a standalone assessmentItem

Input document: doesn't matter

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

  <xsl:import href="author-view-common.xsl"/>

  <!-- State of item being rendered -->
  <xsl:param name="itemSessionState" as="element(qw:itemSessionState)"/>

  <!-- ************************************************************ -->

  <xsl:template match="/" as="element(html)">
    <html>
      <head>
        <title>Author Debug View</title>
        <xsl:call-template name="includeQtiWorksJsAndCss"/>
      </head>
      <body class="page authorInfo">
        <div class="container_12">
          <header class="pageHeader">
            <h1>QTIWorks</h1>
          </header>
          <h2>QTI standalone item author's feedback</h2>

          <xsl:call-template name="errorStatusPanel"/>
          <xsl:call-template name="buttonBar"/>

          <xsl:apply-templates select="$itemSessionState">
            <xsl:with-param name="includeNotifications" select="true()"/>
          </xsl:apply-templates>
        </div>
      </body>
    </html>
  </xsl:template>

  <xsl:template name="buttonBar">
    <div class="buttonBar">
      <ul class="controls">
        <li>
          <form action="{$webappContextPath}{$sourceUrl}" method="get" target="oaa0" class="showXmlInDialog" title="Item Source XML">
            <input type="submit" value="View Item source XML" class="btn btn-default"/>
          </form>
        </li>
        <li>
          <form action="{$webappContextPath}{$stateUrl}" method="get" target="oaa0" class="showXmlInDialog" title="Item State XML">
            <input type="submit" value="View Item state XML" class="btn btn-default"/>
          </form>
        </li>
        <li>
          <form action="{$webappContextPath}{$resultUrl}" method="get" target="oaa0" class="showXmlInDialog" title="Item Result XML">
            <input type="submit" value="View Item &lt;assessmentResult&gt; XML" class="btn btn-default"/>
          </form>
        </li>
      </ul>
    </div>
  </xsl:template>

</xsl:stylesheet>
