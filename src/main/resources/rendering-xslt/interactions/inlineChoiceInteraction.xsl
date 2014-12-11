<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="2.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:qti="http://www.imsglobal.org/xsd/imsqti_v2p1"
  xmlns:qw="http://www.ph.ed.ac.uk/qtiworks"
  xmlns="http://www.w3.org/1999/xhtml"
  exclude-result-prefixes="qti qw xs">

  <xsl:template match="qti:inlineChoiceInteraction">
    <input name="qtiworks_presented_{@responseIdentifier}" type="hidden" value="1"/>
    <span class="{local-name()}">
      <xsl:if test="qw:is-invalid-response(@responseIdentifier)">
        <span class="badResponse">
          You must select one of the following options
        </span>
      </xsl:if>
      <select name="qtiworks_response_{@responseIdentifier}">
        <option value="">(Select)</option>
        <xsl:apply-templates select="qw:get-visible-ordered-choices(., qti:inlineChoice)"/>
      </select>
    </span>
  </xsl:template>

  <xsl:template match="qti:inlineChoice">
    <option value="{@identifier}">
      <xsl:if test="$isItemSessionEnded">
        <xsl:attribute name="disabled">disabled</xsl:attribute>
      </xsl:if>
      <xsl:if test="qw:value-contains(qw:get-response-value(/, ../@responseIdentifier), @identifier)">
        <xsl:attribute name="selected" select="'selected'"/>
      </xsl:if>
      <!--
      Note that the content of <option/> is PCDATA so we need to filter the content. This isn't so
      bad as the only HTML content we might get is added by substituting <printedVariable/>.
      -->
      <xsl:variable name="content" as="item()*">
        <xsl:apply-templates/>
      </xsl:variable>
      <xsl:value-of select="for $x in $content return string($x)"/>
    </option>
  </xsl:template>

</xsl:stylesheet>
