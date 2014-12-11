<?xml version="1.0" encoding="UTF-8"?>
<!--

DM: I don't have anything to test this out with!

-->
<xsl:stylesheet version="2.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:qti="http://www.imsglobal.org/xsd/imsqti_v2p1"
  xmlns:qw="http://www.ph.ed.ac.uk/qtiworks"
  xmlns="http://www.w3.org/1999/xhtml"
  exclude-result-prefixes="qti qw xs">

  <xsl:template match="qti:extendedTextInteraction">
    <input name="qtiworks_presented_{@responseIdentifier}" type="hidden" value="1"/>
    <div class="{local-name()}">
      <xsl:variable name="responseDeclaration" select="qw:get-response-declaration(/, @responseIdentifier)" as="element(qti:responseDeclaration)?"/>
      <xsl:variable name="responseValue" select="qw:get-response-value(/, @responseIdentifier)" as="element(qw:responseVariable)?"/>
      <xsl:variable name="responseInput" select="qw:get-response-input(@responseIdentifier)" as="element(qw:responseInput)?"/>
      <xsl:variable name="rawInput" select="qw:extract-single-cardinality-response-input($responseInput)" as="xs:string?"/>

      <!-- Create JavaScript to check each field -->
      <xsl:variable name="checks" as="xs:string*">
        <xsl:choose>
          <xsl:when test="$responseDeclaration/@baseType='float'"><xsl:sequence select="'float'"/></xsl:when>
          <xsl:when test="$responseDeclaration/@baseType='integer'"><xsl:sequence select="'integer'"/></xsl:when>
        </xsl:choose>
        <xsl:if test="@patternMask">
          <xsl:sequence select="('regex', @patternMask)"/>
        </xsl:if>
      </xsl:variable>
      <xsl:variable name="checkJavaScript" select="if (exists($checks))
        then concat('QtiWorksRendering.validateInput(this, ',
          qw:to-javascript-arguments($checks), ')')
        else ()" as="xs:string?"/>

      <xsl:if test="qti:prompt">
        <div class="prompt">
          <xsl:apply-templates select="qti:prompt"/>
        </div>
      </xsl:if>
      <xsl:if test="qw:is-invalid-response(@responseIdentifier)">
        <div class="badResponse">
          <!-- This will happen if either a pattern is wrong or the wrong number of choices
          were made -->
          <xsl:variable name="quantity" as="xs:string"
            select="if (@minStrings=@maxStrings) then 'all' else concat('at least ', @minStrings)"/>
          <xsl:choose>
            <xsl:when test="@patternMask and @minStrings &gt; 0">
              You must fill in <xsl:value-of select="$quantity"/> boxes
              and use the correct format for your input in each box.
            </xsl:when>
            <xsl:when test="@minStrings &gt; 0">
              You must fill in <xsl:value-of select="$quantity"/> boxes.
            </xsl:when>
            <xsl:when test="@patternMask">
              You must use the correct format for your input in each box.
            </xsl:when>
          </xsl:choose>
        </div>
      </xsl:if>
      <xsl:choose>
        <xsl:when test="$responseDeclaration/@cardinality='single'">
          <xsl:call-template name="singlebox">
            <xsl:with-param name="responseInput" select="$responseInput"/>
            <xsl:with-param name="checkJavaScript" select="$checkJavaScript"/>
          </xsl:call-template>
        </xsl:when>
        <xsl:otherwise>
          <xsl:choose>
            <xsl:when test="@maxStrings">
              <xsl:call-template name="multibox">
                <xsl:with-param name="responseInput" select="$responseInput"/>
                <xsl:with-param name="checkJavaScript" select="$checkJavaScript"/>
                <xsl:with-param name="stringsCount" select="xs:integer(@maxStrings)"/>
              </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
              <xsl:choose>
                <xsl:when test="@minStrings">
                  <xsl:call-template name="multibox">
                    <xsl:with-param name="checkJavaScript" select="$checkJavaScript"/>
                    <xsl:with-param name="responseInput" select="$responseInput"/>
                    <xsl:with-param name="stringsCount" select="if (exists($responseValue)) then max((xs:integer(@minStrings), qw:get-cardinality-size($responseValue))) else xs:integer(@minStrings)"/>
                    <xsl:with-param name="allowCreate" select="true()"/>
                  </xsl:call-template>
                </xsl:when>
                <xsl:otherwise>
                  <xsl:call-template name="multibox">
                    <xsl:with-param name="responseInput" select="$responseInput"/>
                    <xsl:with-param name="checkJavaScript" select="$checkJavaScript"/>
                    <xsl:with-param name="stringsCount" select="if (exists($responseValue)) then qw:get-cardinality-size($responseValue) else 1"/>
                    <xsl:with-param name="allowCreate" select="true()"/>
                  </xsl:call-template>
                </xsl:otherwise>
              </xsl:choose>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:otherwise>
      </xsl:choose>
    </div>
  </xsl:template>

  <xsl:template name="singlebox">
    <xsl:param name="responseInput" as="element(qw:responseInput)?"/>
    <xsl:param name="checkJavaScript" as="xs:string?"/>
    <xsl:variable name="responseInputString" select="qw:extract-single-cardinality-response-input($responseInput)" as="xs:string?"/>
    <textarea cols="40" rows="6" name="qtiworks_response_{@responseIdentifier}">
      <xsl:if test="$isItemSessionEnded">
        <xsl:attribute name="disabled">disabled</xsl:attribute>
      </xsl:if>
      <xsl:if test="@expectedLines">
        <xsl:attribute name="rows" select="@expectedLines"/>
      </xsl:if>
      <xsl:if test="@expectedLines and @expectedLength">
        <xsl:attribute name="cols" select="ceiling(@expectedLength div @expectedLines)"/>
      </xsl:if>
      <xsl:if test="$checkJavaScript">
        <xsl:attribute name="onchange" select="$checkJavaScript"/>
      </xsl:if>
      <xsl:value-of select="$responseInputString"/>
    </textarea>
  </xsl:template>

  <xsl:template name="multibox">
    <xsl:param name="responseInput" as="element(qw:responseInput)?"/>
    <xsl:param name="checkJavaScript" as="xs:string?"/>
    <xsl:param name="stringsCount" as="xs:integer"/>
    <xsl:param name="allowCreate" select="false()" as="xs:boolean"/>
    <xsl:variable name="interaction" select="." as="element(qti:extendedTextInteraction)"/>
    <xsl:for-each select="1 to $stringsCount">
      <xsl:variable name="i" select="." as="xs:integer"/>
      <xsl:variable name="responseInputString" select="$responseInput/qw:value[position()=$i]" as="xs:string?"/>
      <input type="text" name="qtiworks_response_{$interaction/@responseIdentifier}">
        <xsl:if test="$interaction/@expectedLength">
          <xsl:attribute name="size" select="$interaction/@expectedLength"/>
        </xsl:if>
        <xsl:if test="exists($responseInputString)">
          <xsl:attribute name="value" select="$responseInputString"/>
        </xsl:if>
        <xsl:if test="$checkJavaScript">
          <xsl:attribute name="onchange" select="$checkJavaScript"/>
        </xsl:if>
        <xsl:if test="$allowCreate and $i=$stringsCount">
          <xsl:attribute name="onkeyup" select="'QtiWorksRendering.addNewTextBox(this)'"/>
        </xsl:if>
      </input>
      <br/>
    </xsl:for-each>
  </xsl:template>

</xsl:stylesheet>

