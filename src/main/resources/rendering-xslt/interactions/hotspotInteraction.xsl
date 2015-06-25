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
      <xsl:variable name="responseValue" select="qw:get-response-value(/, @responseIdentifier)" as="element(qw:responseVariable)?"/>
      
      <div id="{$appletContainerId}" class="appletContainer v2">
        <img id="{$appletContainerId}_img" width="{$object/@width}" height="{$object/@height}" src="{qw:convert-link-full($object/@data)}" usemap="#{$appletContainerId}_map"></img>
        <map name="{$appletContainerId}_map">
        	<xsl:for-each select="qti:hotspotChoice">
            	<!-- Match group, label -->
          		<area id="{@identifier}" shape="{@shape}" coords="{@coords}" href="javascript:clickArea('{@identifier}')" data-maphilight=''></area>
          	</xsl:for-each>
		</map>
	
		<script type="text/javascript">
			jQuery(function() {
				jQuery('#<xsl:value-of select="$appletContainerId"/>_img').maphilight({
					fillColor: '888888',
					strokeColor: '0000ff',
					strokeWidth: 3
				});
			});
			
			<xsl:choose>
				<xsl:when test="qw:is-not-null-value($responseValue)">
			jQuery(function() {
				highlighHotspotAreas('<xsl:value-of select="$responseValue/qw:value" separator=","/>');
			});
			
			function clickArea(spot) { };
		        </xsl:when>
		        <xsl:otherwise>
			function clickArea(spot) {
				clickHotspotArea(spot, '<xsl:value-of select="$appletContainerId"/>','<xsl:value-of select="@responseIdentifier"/>')
			};
		        </xsl:otherwise>
			</xsl:choose>
		</script>
      </div>
    </div>
  </xsl:template>
</xsl:stylesheet>
