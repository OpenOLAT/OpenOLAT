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
    <xsl:variable name="minStrings" select="if (@minStrings) then xs:integer(@minStrings) else 1" as="xs:integer"/>
    <xsl:variable name="maxStrings" select="if (@maxStrings) then xs:integer(@maxStrings) else ()" as="xs:integer?"/>
    <xsl:variable name="is-bad-response" select="qw:is-bad-response(@responseIdentifier)" as="xs:boolean"/>
    <xsl:variable name="is-invalid-response" select="qw:is-invalid-response(@responseIdentifier)" as="xs:boolean"/>
    <input name="qtiworks_presented_{@responseIdentifier}" type="hidden" value="1"/>
    <div class="{local-name()}">
      <xsl:variable name="responseDeclaration" select="qw:get-response-declaration(/, @responseIdentifier)" as="element(qti:responseDeclaration)?"/>
      <xsl:variable name="responseValue" select="qw:get-response-value(/, @responseIdentifier)" as="element(qw:responseVariable)?"/>
      <xsl:variable name="responseInput" select="qw:get-response-input(@responseIdentifier)" as="element(qw:responseInput)?"/>

      <!-- Create JavaScript to check each field -->
      <xsl:variable name="checks" as="xs:string*">
        <xsl:choose>
          <!--
          NB: We don't presently do any JS checks for numeric values bound to records, as the JS isn't currently
          clever enough to handle all numeric formats (e.g. 4e12)
          -->
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
      <xsl:if test="$is-bad-response">
        <div class="badResponse">
          <!-- TODO: The message below isn't very suitable for list values -->
          Your input must be a valid
          <xsl:choose>
            <xsl:when test="$responseDeclaration/@cardinality='record'">
              <xsl:value-of select="'number'"/>
            </xsl:when>
            <xsl:otherwise>
              <xsl:value-of select="$responseDeclaration/@baseType"/>!
            </xsl:otherwise>
          </xsl:choose>
        </div>
      </xsl:if>
      <xsl:if test="qw:is-invalid-response(@responseIdentifier)">
        <div class="badResponse">
          <!-- This will happen if either a pattern is wrong or the wrong number of choices
          were made -->
          <xsl:variable name="requiredQuantity" as="xs:string"
            select="if ($minStrings=$maxStrings) then 'all' else concat('at least ', $minStrings)"/>
          <xsl:choose>
            <xsl:when test="@patternMask and $minStrings &gt; 0">
              You must fill in <xsl:value-of select="$requiredQuantity"/> box(es)
              and use the correct format for your input in each box.
            </xsl:when>
            <xsl:when test="$minStrings &gt; 0">
              You must fill in <xsl:value-of select="$requiredQuantity"/> box(es).
            </xsl:when>
            <xsl:when test="@patternMask">
              You must use the correct format for your input in each box.
            </xsl:when>
          </xsl:choose>
        </div>
      </xsl:if>
      <xsl:choose>
        <xsl:when test="$responseDeclaration/@cardinality=('single','record')">
          <xsl:apply-templates select="." mode="singlebox">
            <xsl:with-param name="responseInputString" select="qw:extract-single-cardinality-response-input($responseInput)"/>
            <xsl:with-param name="checkJavaScript" select="$checkJavaScript"/>
          </xsl:apply-templates>
        </xsl:when>
        <xsl:otherwise>
          <xsl:choose>
            <xsl:when test="exists($maxStrings)">
              <xsl:apply-templates select="." mode="multibox">
                <xsl:with-param name="responseInput" select="$responseInput"/>
                <xsl:with-param name="checkJavaScript" select="$checkJavaScript"/>
                <xsl:with-param name="stringsCount" select="$maxStrings"/>
              </xsl:apply-templates>
            </xsl:when>
            <xsl:otherwise>
              <xsl:apply-templates select="." mode="multibox">
                <xsl:with-param name="checkJavaScript" select="$checkJavaScript"/>
                <xsl:with-param name="responseInput" select="$responseInput"/>
                <xsl:with-param name="stringsCount" select="if (exists($responseValue)) then max(($minStrings, qw:get-cardinality-size($responseValue))) else $minStrings"/>
                <xsl:with-param name="allowCreate" select="true()"/>
              </xsl:apply-templates>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:otherwise>
      </xsl:choose>
    </div>
  </xsl:template>

  <xsl:template match="qti:extendedTextInteraction" mode="singlebox">
    <xsl:param name="responseInputString" as="xs:string?"/>
    <xsl:param name="checkJavaScript" as="xs:string?"/>
    <xsl:param name="allowCreate" select="false()" as="xs:boolean"/>
    <xsl:variable name="is-bad-response" select="qw:is-bad-response(@responseIdentifier)" as="xs:boolean"/>
    <xsl:variable name="is-invalid-response" select="qw:is-invalid-response(@responseIdentifier)" as="xs:boolean"/>
    <textarea cols="72" rows="6" name="qtiworks_response_{@responseIdentifier}">
      <xsl:if test="$isItemSessionEnded">
        <xsl:attribute name="disabled">disabled</xsl:attribute>
      </xsl:if>
      <xsl:if test="$is-bad-response or $is-invalid-response">
        <xsl:attribute name="class" select="'badResponse'"/>
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
      <xsl:if test="$allowCreate">
        <xsl:attribute name="onkeyup" select="'QtiWorksRendering.addNewTextBox(this)'"/>
      </xsl:if>
      <xsl:value-of select="$responseInputString"/>
    </textarea>
  </xsl:template>

  <xsl:template match="qti:extendedTextInteraction" mode="multibox">
    <xsl:param name="responseInput" as="element(qw:responseInput)?"/>
    <xsl:param name="checkJavaScript" as="xs:string?"/>
    <xsl:param name="stringsCount" as="xs:integer"/>
    <xsl:param name="allowCreate" select="false()" as="xs:boolean"/>
    <xsl:variable name="interaction" select="." as="element(qti:extendedTextInteraction)"/>
    <xsl:for-each select="1 to $stringsCount">
      <xsl:variable name="i" select="." as="xs:integer"/>
      <xsl:apply-templates select="$interaction" mode="singlebox">
        <xsl:with-param name="responseInputString" select="$responseInput/qw:string[position()=$i]"/>
        <xsl:with-param name="checkJavaScript" select="$checkJavaScript"/>
        <xsl:with-param name="allowCreate" select="$allowCreate and $i=$stringsCount"/>
      </xsl:apply-templates>
      <br />
    </xsl:for-each>
  </xsl:template>

</xsl:stylesheet>
