<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="2.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:qti="http://www.imsglobal.org/xsd/imsqti_v2p1"
  xmlns:qw="http://www.ph.ed.ac.uk/qtiworks"
  xmlns="http://www.w3.org/1999/xhtml"
  exclude-result-prefixes="qti qw xs">

  <xsl:template match="qti:sliderInteraction">
    <input name="qtiworks_presented_{@responseIdentifier}" type="hidden" value="1"/>
    <div class="{local-name()}">
      <xsl:if test="qti:prompt">
        <div class="prompt">
          <xsl:apply-templates select="qti:prompt"/>
        </div>
      </xsl:if>
      <xsl:if test="qw:is-invalid-response(@responseIdentifier)">
        <xsl:call-template name="qw:generic-bad-response-message"/>
      </xsl:if>

      <xsl:variable name="value" select="qw:get-response-value(/, @responseIdentifier)"/>
      <xsl:variable name="is-discrete" select="qw:get-response-declaration(/, @responseIdentifier)/@baseType='integer'" as="xs:boolean"/>
      <xsl:variable name="min" select="if ($is-discrete) then string(floor(@lowerBound)) else string(@lowerBound)" as="xs:string"/>
      <xsl:variable name="max" select="if ($is-discrete) then string(ceiling(@upperBound)) else string(@upperBound)" as="xs:string"/>
      <xsl:variable name="step" select="if (@step) then @step else if ($is-discrete) then '1' else '0.01'" as="xs:string"/>
      <xsl:variable name="orientation" select="if (@orientation) then @orientation else 'horizontal'"/>

      <div class="sliderInteraction">
        <xsl:choose>
          <xsl:when test="$orientation='horizontal'">
            <div class="sliderHorizontal">
              <div class="sliderWidget" id="qtiworks_id_slider_{@responseIdentifier}"></div>
              <div class="sliderValue"><span id="qtiworks_id_slidervalue_{@responseIdentifier}"><xsl:value-of select="$value"/></span></div>
            </div>
          </xsl:when>
          <xsl:otherwise>
            <div class="sliderVertical">
              <div class="sliderValue"><span id="qtiworks_id_slidervalue_{@responseIdentifier}"><xsl:value-of select="$value"/></span></div>
              <div class="sliderWidget" id="qtiworks_id_slider_{@responseIdentifier}"></div>
            </div>
          </xsl:otherwise>
        </xsl:choose>
        <input type="hidden" name="qtiworks_response_{@responseIdentifier}" value="{$value}"/>
        <script type="text/javascript">
          jQuery(document).ready(function() {
            QtiWorksRendering.registerSliderInteraction('<xsl:value-of
                select="@responseIdentifier"/>', {
              min: <xsl:value-of select="$min"/>,
              max: <xsl:value-of select="$max"/>,
              step: <xsl:value-of select="$step"/>,
              orientation: '<xsl:value-of select="if (@orientation) then @orientation else 'horizontal'"/>',
              isReversed: <xsl:value-of select="if (@reverse) then @reverse else 'false'"/>,
              isDiscrete: <xsl:value-of select="$is-discrete"/>
            });
          });
        </script>
      </div>
    </div>
  </xsl:template>
</xsl:stylesheet>
