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
				var canvas = document.getElementById('<xsl:value-of select="$appletContainerId"/>_canvas');
				var c = canvas.getContext('2d');
				c.clearRect(0, 0, jQuery(canvas).width(), jQuery(canvas).height());
				
				var areaIds = '<xsl:value-of select="$responseValue/qw:value" separator=","/>'.split(',');
				for(var i=areaIds.length; i-->0; ) {
					var areaEl = jQuery('#' + areaIds[i]);
					var position = areaEl.attr('coords').split(',');
					var cx = position[0];
					var cy = position[1];
					
					c.font = "16px Arial";
					c.fillText("" + (i+1), cx, cy);
				}
			});
			
			</xsl:when>
			<xsl:otherwise>
			
		jQuery(function() {
			jQuery('#<xsl:value-of select="$appletContainerId"/>_container area').on("click", function(e) {
				var r = 8;
				var maxChoices = <xsl:value-of select="count($hotspotChoices)"/>;

				var areaId = jQuery(this).attr('id');
				var position = jQuery(this).attr('coords').split(',');
				var cx = position[0];
				var cy = position[1];

				var data = jQuery("#<xsl:value-of select="$appletContainerId"/>_container").data("openolat") || {};
				if(data.listOfPoints == undefined) {
					data.listOfPoints = [];
					jQuery("#<xsl:value-of select="$appletContainerId"/>_container").data('openolat', data);
				}
					
				var remove = false;
				var newListOfPoints = [];
				for(var i=data.listOfPoints.length; i-->0;) {
					var p = data.listOfPoints[i];
					var rc = ((p.x - cx)*(p.x - cx)) + ((p.y - cy)*(p.y - cy));
					if(r*r > rc) {
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
					data.listOfPoints.push({'x': cx, 'y': cy, 'areaId': areaId});
				}

				var canvas = document.getElementById('<xsl:value-of select="$appletContainerId"/>_canvas');
				var c = canvas.getContext('2d');
				c.clearRect(0, 0, jQuery(canvas).width(), jQuery(canvas).height());
					
				var divContainer = jQuery('#<xsl:value-of select="$appletContainerId"/>_container');
				divContainer.find("input[type='hidden']").remove();
					
				for(var i=data.listOfPoints.length; i-->0;) {
					var p = data.listOfPoints[i];
					c.font = "16px Arial";
					c.fillText("" + (i+1), p.x, p.y);

					var inputElement = jQuery('<input type="hidden"/>')
						.attr('name', 'qtiworks_response_RESPONSE')
						.attr('value', p.areaId);
					divContainer.prepend(inputElement);
				}
			});
		});
			
			</xsl:otherwise>
		</xsl:choose>

		</script>
      </div>
    </div>
  </xsl:template>
</xsl:stylesheet>
