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
				var r = 8;
				var points = '<xsl:value-of select="$responseValue/qw:value" separator=":"/>'.split(':');
				var canvas = document.getElementById('<xsl:value-of select="$appletContainerId"/>_canvas');
				var c = canvas.getContext('2d');
				c.clearRect(0, 0, jQuery(canvas).width(), jQuery(canvas).height());
				for(i=points.length; i-->0; ) {
					var p = points[i].split(' ');
					c.beginPath();
					c.arc(p[0], p[1], r, 0, Math.PI * 2, false);
					c.stroke();
					c.closePath();
				}
			});
        </xsl:when>
        <xsl:otherwise>

			jQuery(function() {
				jQuery('#<xsl:value-of select="$appletContainerId"/>_canvas').on("click", function(e, t) {
					var r = 8;
					var maxChoices = <xsl:value-of select="@maxChoices"/>;
				
					var offset_t = jQuery(this).offset().top - jQuery(window).scrollTop();
					var offset_l = jQuery(this).offset().left - jQuery(window).scrollLeft();

					var cx = Math.round( (e.clientX - offset_l) );
					var cy = Math.round( (e.clientY - offset_t) );
					
					var data = jQuery("#<xsl:value-of select="$appletContainerId"/>").data("openolat") || {};
					if(data.listOfPoints == undefined) {
						data.listOfPoints = [];
						jQuery("#<xsl:value-of select="$appletContainerId"/>").data('openolat', data);
					}
					
					var remove = false;
					var newListOfPoints = [];
					for(i=data.listOfPoints.length; i-->0;) {
						var p = data.listOfPoints[i];
						var rc = ((p.x - cx)*(p.x - cx)) + ((p.y - cy)*(p.y - cy));
						if(Math.pow(r,2) > rc) {
							remove = true;
						} else {
							newListOfPoints.push(p);
						}
					}
					
					if(remove) {
						data.listOfPoints = newListOfPoints;
					} else if(data.listOfPoints.length >= maxChoices) {
						return false;
					} else {
						data.listOfPoints.push({'x': cx, 'y': cy});
					}

					var canvas = document.getElementById('<xsl:value-of select="$appletContainerId"/>_canvas');
					var c = canvas.getContext('2d');
					c.clearRect(0, 0, jQuery(canvas).width(), jQuery(canvas).height());
					
					var divContainer = jQuery('#<xsl:value-of select="$appletContainerId"/>');
					divContainer.find("input[type='hidden']").remove();
					
					for(i=data.listOfPoints.length; i-->0;) {
						var p = data.listOfPoints[i];
						c.beginPath();
						c.arc(p.x, p.y, r, 0, Math.PI * 2, false);
						c.stroke();
						c.closePath();
						
						var inputElement = jQuery('<input type="hidden"/>')
							.attr('name', 'qtiworks_response_<xsl:value-of select="@responseIdentifier"/>')
							.attr('value', p.x + " " + p.y);
						divContainer.append(inputElement);
					}
				});
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
