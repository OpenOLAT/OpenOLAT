<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="2.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:qti="http://www.imsglobal.org/xsd/imsqti_v2p1"
  xmlns:qw="http://www.ph.ed.ac.uk/qtiworks"
  xmlns="http://www.w3.org/1999/xhtml"
  exclude-result-prefixes="qti qw xs">

  <xsl:template match="qti:gapMatchInteraction">
    <input name="qtiworks_presented_{@responseIdentifier}" type="hidden" value="1"/>
    <xsl:variable name="orderedVisibleGapTexts" select="qw:get-visible-ordered-choices(., qti:gapText)"/>
    <xsl:variable name="visibleGaps" select="qw:filter-visible(.//qti:gap)"/>
    <div class="{local-name()}">
      <xsl:if test="qti:prompt">
        <div class="prompt">
          <xsl:apply-templates select="qti:prompt"/>
        </div>
      </xsl:if>
      <xsl:if test="qw:is-invalid-response(@responseIdentifier)">
        <xsl:call-template name="qw:generic-bad-response-message"/>
      </xsl:if>
      <xsl:apply-templates select="*[not(self::qti:gapText or self::qti:prompt)]"/>
      <table>
        <tr>
          <td></td>
          <xsl:for-each select="$orderedVisibleGapTexts">
            <td id="qtiworks_id_{../@responseIdentifier}_{@identifier}">
              <xsl:apply-templates/>
            </td>
          </xsl:for-each>
        </tr>
        <xsl:variable name="gmi" select="." as="element(qti:gapMatchInteraction)"/>
        <xsl:for-each select="$visibleGaps">
          <xsl:variable name="gapIdentifier" select="@identifier" as="xs:string"/>
          <tr>
            <td>
              GAP <xsl:value-of select="position()"/>
            </td>
            <xsl:for-each select="$orderedVisibleGapTexts">
              <td>
                <xsl:variable name="responseValue" select="concat(@identifier, ' ', $gapIdentifier)" as="xs:string"/>
                <input type="checkbox" name="qtiworks_response_{$gmi/@responseIdentifier}" value="{$responseValue}">
                  <xsl:if test="$isItemSessionEnded">
                    <xsl:attribute name="disabled">disabled</xsl:attribute>
                  </xsl:if>
                  <xsl:if test="qw:value-contains(qw:get-response-value(/, $gmi/@responseIdentifier), $responseValue)">
                    <xsl:attribute name="checked" select="'checked'"/>
                  </xsl:if>
                </input>
              </td>
            </xsl:for-each>
          </tr>
        </xsl:for-each>
      </table>
      <script type='text/javascript'>
        QtiWorksRendering.registerGapMatchInteraction('<xsl:value-of select="@responseIdentifier"/>',
        {<xsl:for-each select="$orderedVisibleGapTexts"><xsl:if test="position() > 1">, </xsl:if><xsl:value-of select="@identifier"/>:<xsl:value-of select="@matchMax"/></xsl:for-each>},
        {<xsl:for-each select="$visibleGaps"><xsl:if test="position() > 1">, </xsl:if><xsl:value-of select="@identifier"/>:<xsl:value-of select="boolean(@required)"/></xsl:for-each>});
      </script>
    </div>
  </xsl:template>

  <xsl:template match="qti:gap">
    <xsl:variable name="gmi" select="ancestor::qti:gapMatchInteraction" as="element(qti:gapMatchInteraction)"/>
    <xsl:variable name="gaps" select="$gmi//qti:gap" as="element(qti:gap)+"/>
    <xsl:variable name="thisGap" select="." as="element(qti:gap)"/>
    <span class="gap" id="qtiworks_id_{$gmi/@responseIdentifier}_{@identifier}">
      <!-- (Print index of this gap wrt all gaps in the interaction) -->
      GAP <xsl:value-of select="for $i in 1 to count($gaps) return
        if ($gaps[$i]/@identifier = $thisGap/@identifier) then $i else ()"/>
    </span>
  </xsl:template>

</xsl:stylesheet>
