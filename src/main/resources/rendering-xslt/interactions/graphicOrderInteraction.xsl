<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="2.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:qti="http://www.imsglobal.org/xsd/imsqti_v2p1"
  xmlns:qw="http://www.ph.ed.ac.uk/qtiworks"
  xmlns="http://www.w3.org/1999/xhtml"
  exclude-result-prefixes="qti qw xs">

  <xsl:template match="qti:graphicOrderInteraction">
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
      <xsl:variable name="hotspotChoices" select="qw:filter-visible(qti:hotspotChoice)" as="element(qti:hotspotChoice)*"/>
      <xsl:variable name="responseValue" select="qw:get-response-value(/, @responseIdentifier)" as="element(qw:responseVariable)?"/>
      <div id="{$appletContainerId}_container" class="appletContainer" style="width:{$object/@width}px; height:{$object/@height}px; position:relative; background-image: url('{qw:convert-link-full($object/@data)}') " data-openolat="">

		<canvas id="{$appletContainerId}_canvas_alt" width="{$object/@width}" height="{$object/@height}" style="position:absolute; top:0;left:0; opacity:1; " ></canvas>
		<canvas id="{$appletContainerId}_canvas" width="{$object/@width}" height="{$object/@height}" style="position:absolute; top:0;left:0; "></canvas>
		<img id="{$appletContainerId}" width="{$object/@width}" height="{$object/@height}" src="{qw:convert-link-full($object/@data)}" usemap="#{$appletContainerId}_map" style="position:absolute; top:0; left:0; opacity:0;"></img>
		<map name="{$appletContainerId}_map">
			<xsl:for-each select="$hotspotChoices">
				<area id="{@identifier}" shape="{@shape}" coords="{@coords}" href="#" ></area>
			</xsl:for-each>
		</map>
      
		<script type="text/javascript">
		<xsl:choose>
			<xsl:when test="qw:is-not-null-value($responseValue)">
			
			jQuery(function() {
				graphicOrderDrawResponse('<xsl:value-of select="$appletContainerId"/>','<xsl:value-of select="$responseValue/qw:value" separator=","/>');
			});
			
			</xsl:when>
			<xsl:otherwise>
			
		jQuery(function() {
			graphicOrderItem('<xsl:value-of select="$appletContainerId"/>', <xsl:value-of select="count($hotspotChoices)"/>,'<xsl:value-of select="@responseIdentifier"/>');
		});
			
			</xsl:otherwise>
		</xsl:choose>

		</script>
      </div>
    </div>
  </xsl:template>
</xsl:stylesheet>
