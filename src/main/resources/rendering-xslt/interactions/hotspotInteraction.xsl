<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="2.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:qti="http://www.imsglobal.org/xsd/imsqti_v2p1"
  xmlns:qw="http://www.ph.ed.ac.uk/qtiworks"
  xmlns="http://www.w3.org/1999/xhtml"
  exclude-result-prefixes="qti qw xs">

  <xsl:template match="qti:hotspotInteraction">
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

      <xsl:variable name="object" select="qti:object" as="element(qti:object)"/>
      <xsl:variable name="appletContainerId" select="concat('qtiworks_id_appletContainer_', @responseIdentifier)" as="xs:string"/>
      <div id="{$appletContainerId}" class="appletContainer v2">
        <object type="application/x-java-applet" height="{$object/@height + 40}" width="{$object/@width}">
          <param name="code" value="BoundedGraphicalApplet"/>
          <param name="codebase" value="{$appletCodebase}"/>
          <param name="identifier" value="{@responseIdentifier}"/>
          <param name="operation_mode" value="hotspot_interaction"/>
          <!-- (BoundedGraphicalApplet uses -1 to represent 'unlimited') -->
          <param name="number_of_responses" value="{if (@maxChoices &gt; 0) then @maxChoices else -1}"/>
          <param name="background_image" value="{qw:convert-link-full($object/@data)}"/>
          <xsl:variable name="hotspotChoices" select="qw:filter-visible(qti:hotspotChoice)" as="element(qti:hotspotChoice)*"/>
          <param name="hotspot_count" value="{count($hotspotChoices)}"/>
          <xsl:for-each select="qti:hotspotChoice">
            <param name="hotspot{position()-1}"
              value="{@identifier}::::{@shape}::{@coords}{if (@label) then concat('::hotSpotLabel',@label) else ''}{if (@matchGroup) then concat('::', translate(normalize-space(@matchGroup), ' ', '::')) else ''}"/>
          </xsl:for-each>

          <xsl:variable name="responseValue" select="qw:get-response-value(/, @responseIdentifier)" as="element(qw:responseVariable)?"/>
          <xsl:if test="qw:is-not-null-value($responseValue)">
            <param name="feedback">
              <xsl:attribute name="value">
                <xsl:value-of select="$responseValue/qw:value" separator=","/>
              </xsl:attribute>
            </param>
          </xsl:if>
        </object>
        <script type="text/javascript">
          jQuery(document).ready(function() {
            QtiWorksRendering.registerAppletBasedInteractionContainer('<xsl:value-of
              select="$appletContainerId"/>', ['<xsl:value-of select="@responseIdentifier"/>']);
          });
        </script>
      </div>
    </div>
  </xsl:template>
</xsl:stylesheet>
