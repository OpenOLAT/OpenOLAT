<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="2.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:qti="http://www.imsglobal.org/xsd/imsqti_v2p1"
  xmlns:qw="http://www.ph.ed.ac.uk/qtiworks"
  xmlns="http://www.w3.org/1999/xhtml"
  exclude-result-prefixes="qti qw xs">

  <xsl:template match="qti:selectPointInteraction">
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
      
      <div id="{$appletContainerId}" class="appletContainer" data-openolat="">
		<canvas id="{$appletContainerId}_canvas" width="{$object/@width}" height="{$object/@height}" style="background-image:url('{qw:convert-link($object/@data)}');"></canvas>

	  <script type="text/javascript">
      <xsl:choose>
        <xsl:when test="qw:is-not-null-value($responseValue)">  
			jQuery(function() {
				selectPointDrawResponse('<xsl:value-of select="$appletContainerId"/>','<xsl:value-of select="$responseValue/qw:value" separator=":"/>');
			});
        </xsl:when>
        <xsl:otherwise>
			jQuery(function() {
				selectPointItem('<xsl:value-of select="$appletContainerId"/>',<xsl:value-of select="@maxChoices"/>,'<xsl:value-of select="@responseIdentifier"/>');	
			});			
        </xsl:otherwise>
      </xsl:choose>
      </script>
      
      	<!--
        <object type="application/x-java-applet" height="{$object/@height + 40}" width="{$object/@width}">
          <param name="code" value="rhotspotV2"/>
          <param name="codebase" value="{$appletCodebase}"/>
          <param name="identifier" value="{@responseIdentifier}"/>
          <param name="NoOfMainImages" value="1"/>
          <param name="Mainimageno1" value="{qw:convert-link($object/@data)}::0::0::{$object/@width}::{$object/@height}"/>
          <param name="markerName" value="images/marker.gif"/>
          <param name="baseType" value="point"/>
          <param name="noOfTargets" value="0"/>
          <param name="identifiedTargets" value="FALSE"/>
          <param name="noOfMarkers" value="{@maxChoices}"/>
          <param name="markerType" value="STANDARD"/>

          <xsl:variable name="responseValue" select="qw:get-response-value(/, @responseIdentifier)" as="element(qw:responseVariable)?"/>
          <param name="feedbackState">
            <xsl:attribute name="value">
              <xsl:choose>
                <xsl:when test="qw:is-not-null-value($responseValue)">
                  <xsl:text>Yes:</xsl:text>
                  <xsl:value-of select="$responseValue/qw:value" separator=":"/>
                </xsl:when>
                <xsl:otherwise>
                  <xsl:text>No</xsl:text>
                </xsl:otherwise>
              </xsl:choose>
            </xsl:attribute>
          </param>
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
