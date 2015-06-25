<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="2.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:qti="http://www.imsglobal.org/xsd/imsqti_v2p1"
  xmlns:qw="http://www.ph.ed.ac.uk/qtiworks"
  xmlns="http://www.w3.org/1999/xhtml"
  exclude-result-prefixes="xs qti qw">

  <xsl:template match="qti:positionObjectStage">
    <xsl:for-each select="qti:positionObjectInteraction">
      <input name="qtiworks_presented_{@responseIdentifier}" type="hidden" value="1"/>
    </xsl:for-each>
    <div class="{local-name()}">
      <xsl:if test="qti:prompt">
        <div class="prompt">
          <xsl:apply-templates select="qti:prompt"/>
        </div>
      </xsl:if>
      <!-- TODO: This probably looks awful! -->
      <xsl:for-each select="qti:positionObjectInteraction">
        <xsl:if test="qw:is-invalid-response(@responseIdentifier)">
          <xsl:call-template name="qw:generic-bad-response-message"/>
        </xsl:if>
      </xsl:for-each>

      <xsl:variable name="object" select="qti:object"/>
      <xsl:variable name="appletContainerId" select="concat('qtiworks_id_appletContainer_', qti:positionObjectInteraction[1]/@responseIdentifier)" as="xs:string"/>
		<div id="{$appletContainerId}" class="appletContainer" style="width:{$object/@width}px; position:relative; ">
      		<img id="{$appletContainerId}_img" width="{$object/@width}" height="{$object/@height}" src="{qw:convert-link-full($object/@data)}"></img>
			<div class="items_container" style="width:{$object/@width}px; height:50px; background-color:#cccccc;">
			
			<xsl:for-each select="qti:positionObjectInteraction">
            	<xsl:variable name="interaction" select="." as="element(qti:positionObjectInteraction)"/>
            	<xsl:variable name="responseValue" select="qw:get-response-value(/, @responseIdentifier)" as="element(qw:responseVariable)?"/>
          
 		<script type="text/javascript">
		jQuery(function() {
		<xsl:choose>
        	<xsl:when test="qw:is-not-null-value($responseValue)">
			positionObjectDrawResponse('<xsl:value-of select="$appletContainerId"/>','<xsl:value-of select="$interaction/@responseIdentifier"/>','<xsl:value-of select="$responseValue/qw:value" separator=":"/>');
			</xsl:when>
            <xsl:otherwise>
			positionObjectItem('<xsl:value-of select="$appletContainerId"/>','<xsl:value-of select="$interaction/@responseIdentifier"/>');
			</xsl:otherwise>
		</xsl:choose>
		});
		</script>                   
 
            	<xsl:for-each select="1 to @maxChoices">
              		<!-- {$interaction/qti:object/@type} -->
            		<div class="o_item o_{$interaction/@responseIdentifier}" style="width:{$interaction/qti:object/@width}px; height:{$interaction/qti:object/@height}px; background-image:url('{qw:convert-link($interaction/qti:object/@data)}');"> </div>
            	</xsl:for-each>
			</xsl:for-each>
			</div>
		

      
      	<!-- 
        <object type="application/x-java-applet" height="{$object/@height + 40}" width="{$object/@width}">
          <param name="code" value="rhotspotV2"/>
          <param name="codebase" value="{$appletCodebase}"/>
          <param name="NoOfMainImages" value="1"/>
          <param name="background_image" value="{qw:convert-link-full($object/@data)}"/>
          <param name="Mainimageno1" value="{qw:convert-link($object/@data)}::0::0::{$object/@width}::{$object/@height}"/>
          <param name="baseType" value="point"/>
          <param name="noOfTargets" value="0"/>
          <param name="identifiedTargets" value="FALSE"/>
          <param name="interactions" value="{string-join(for $i in qti:positionObjectInteraction return $i/@responseIdentifier, '::')}"/>

          <xsl:for-each select="qti:positionObjectInteraction">
            <xsl:variable name="interaction" select="." as="element(qti:positionObjectInteraction)"/>
            <param name="maxChoices:{@responseIdentifier}" value="{@maxChoices}"/>
            <xsl:for-each select="1 to @maxChoices">
              <param name="labelNo{.}:{$interaction/@responseIdentifier}"
                value="::{$interaction/qti:object/@type}::{qw:convert-link($interaction/qti:object/@data)}::{$interaction/qti:object/@width}::{$interaction/qti:object/@height}::{$interaction/@maxChoices}"/>
            </xsl:for-each>

            <xsl:variable name="responseValue" select="qw:get-response-value(/, @responseIdentifier)" as="element(qw:responseVariable)?"/>
            <param name="feedbackState:{@responseIdentifier}">
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
          </xsl:for-each>

          <!-param name="noOfMarkers" value="3"/>
            <xsl:attribute name="value"><xsl:value-of select="@maxChoices"/></xsl:attribute>
          </param ->
          <param name="markerType" value="LABELS"/>
        </object>
        <script type="text/javascript">
          jQuery(document).ready(function() {
            QtiWorksRendering.registerAppletBasedInteractionContainer('<xsl:value-of
              select="$appletContainerId"/>', [<xsl:value-of
              select="qw:to-javascript-arguments(for $i in qti:positionObjectInteraction return $i/@responseIdentifier)"/>]);
          });
        </script>
        -->
      </div>
    </div>
  </xsl:template>

</xsl:stylesheet>
