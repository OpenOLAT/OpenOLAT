<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="2.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:qti="http://www.imsglobal.org/xsd/imsqti_v2p1"
  xmlns:qw="http://www.ph.ed.ac.uk/qtiworks"
  xmlns="http://www.w3.org/1999/xhtml"
  exclude-result-prefixes="qti qw xs">

  <xsl:template match="qti:graphicAssociateInteraction">
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
      <xsl:variable name="hotspots" select="qw:filter-visible(qti:associableHotspot)" as="element(qti:associableHotspot)*"/>
      <xsl:variable name="responseValue" select="qw:get-response-value(/, @responseIdentifier)" as="element(qw:responseVariable)?"/>
      <h4>graphic assoc</h4>
      <div id="{$appletContainerId}" class="appletContainer">
      
      	<div id="{$appletContainerId}_container" style="width:{$object/@width}px; height:{$object/@height}px; position:relative; background-image: url('{qw:convert-link-full($object/@data)}') " data-openolat="">
			<canvas id="{$appletContainerId}_canvas" width="{$object/@width}" height="{$object/@height}" style="position:absolute; top:0;left:0; "></canvas>
			<img id="{$appletContainerId}" width="{$object/@width}" height="{$object/@height}" src="{qw:convert-link-full($object/@data)}" usemap="#{$appletContainerId}_map" style="position:absolute; top:0; left:0; opacity:0;"></img>
			<map name="{$appletContainerId}_map">
			<xsl:for-each select="$hotspots">
				<!-- @label, @matchGroup, @matchMax -->
				<area id="{@identifier}" shape="{@shape}" coords="{@coords}" href="#" data-maphilight=''></area>
          	</xsl:for-each>
          	</map>
		</div>
		
		<script type="text/javascript">
		jQuery(function() {
		<xsl:choose>
			<xsl:when test="qw:is-not-null-value($responseValue)">
			graphicAssociationDrawResponse('<xsl:value-of select="$appletContainerId"/>','<xsl:value-of select="$responseValue/qw:value" separator=","/>');
			</xsl:when>
			<xsl:otherwise>	
			graphicAssociationItem('<xsl:value-of select="$appletContainerId"/>',<xsl:value-of select="@maxAssociations"/>,'<xsl:value-of select="@responseIdentifier"/>');
			</xsl:otherwise>
		</xsl:choose>
		});
		</script>
      	<!-- 
        <object type="application/x-java-applet" height="{$object/@height + 40}" width="{$object/@width}">
          <param name="code" value="BoundedGraphicalApplet"/>
          <param name="codebase" value="{$appletCodebase}"/>
          <param name="identifier" value="{@responseIdentifier}"/>
          <param name="baseType" value="pair"/>
          <param name="operation_mode" value="graphic_associate_interaction"/>
          <param name="number_of_responses" value="{if (@maxAssociations &gt; 0) then @maxAssocations else -1}"/>
          <param name="background_image" value="{qw:convert-link-full($object/@data)}"/>
          <xsl:variable name="hotspots" select="qw:filter-visible(qti:associableHotspot)" as="element(qti:associableHotspot)*"/>
          <param name="hotspot_count" value="{count($hotspots)}"/>
          <xsl:for-each select="$hotspots">
            <param name="hotspot{position()-1}">
              <xsl:attribute name="value"><xsl:value-of select="@identifier"/>::::<xsl:value-of select="@shape"/>::<xsl:value-of select="@coords"/><xsl:if test="@label">::hotSpotLabel:<xsl:value-of select="@label"/></xsl:if><xsl:if test="@matchGroup">::<xsl:value-of select="translate(normalize-space(@matchGroup), ' ', '::')"/></xsl:if><xsl:if test="@matchMax">::maxAssociations:<xsl:value-of select="@matchMax"/></xsl:if></xsl:attribute>
            </param>
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
        -->
      </div>
    </div>
  </xsl:template>
</xsl:stylesheet>
