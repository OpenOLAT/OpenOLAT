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
		<![CDATA[
		function toCoords(area) {
			var coords = area.attr('coords').split(',');
			for (i=coords.length; i-->0; ) {
				coords[i] = parseFloat(coords[i]);
			}
			return coords;
		};
		
		function draw_shape(context, shape, coords, x_shift, y_shift) {
			x_shift = x_shift || 0;
			y_shift = y_shift || 0;

			context.beginPath();
			if(shape == 'rect') {
				// x, y, width, height
				context.rect(coords[0] + x_shift, coords[1] + y_shift, coords[2] - coords[0], coords[3] - coords[1]);
			} else if(shape == 'poly') {
				context.moveTo(coords[0] + x_shift, coords[1] + y_shift);
				for(i=2; i < coords.length; i+=2) {
					context.lineTo(coords[i] + x_shift, coords[i+1] + y_shift);
				}
			} else if(shape == 'circ' || shape == 'circle') {
				// x, y, radius, startAngle, endAngle, anticlockwise
				context.arc(coords[0] + x_shift, coords[1] + y_shift, coords[2] - 2, 0, Math.PI * 2, false);
			}
			context.closePath();
      		context.lineWidth = 4;
			context.strokeStyle = '#003300';
      		context.stroke();
			context.fillStyle = 'green';
      		context.fill();
		};
		
		function drawArea(c, areaId) {
			var areaEl = jQuery('#' + areaId);
			var shape = areaEl.attr('shape');
			var coords = toCoords(areaEl);
			draw_shape(c, shape, coords, 0, 0);
		};
		]]>
		<xsl:choose>
			<xsl:when test="qw:is-not-null-value($responseValue)">

			jQuery(function() {
				var canvas = document.getElementById('<xsl:value-of select="$appletContainerId"/>_canvas');
				var c = canvas.getContext('2d');
				c.clearRect(0, 0, jQuery(canvas).width(), jQuery(canvas).height());
				
				var drawedSpots = [];
				var pairs = '<xsl:value-of select="$responseValue/qw:value" separator=","/>'.split(',');
				for(var i=pairs.length; i-->0; ) {
					var pair = pairs[i].split(' ');
					for(var j=pair.length; j-->0; ) {
						if(0 > drawedSpots.indexOf(pair[j])) {
							drawArea(c, pair[j]);
							drawedSpots.push(pair[j]);
						}
					}

					var coords1 = toCoords(jQuery('#' + pair[1]));
					var coords2 = toCoords(jQuery('#' + pair[0]));
					
					c.beginPath();
					c.moveTo(coords1[0], coords1[1]);
					c.lineTo(coords2[0], coords2[1]);
					c.lineWidth = 3;
					c.stroke();
				}
			});
			
			</xsl:when>
			<xsl:otherwise>
			
		jQuery(function() {
			jQuery('#<xsl:value-of select="$appletContainerId"/>_container area').on("click", function(e) {
				var r = 8;
				var maxChoices = 3;
				
				var data = jQuery("#<xsl:value-of select="$appletContainerId"/>_container").data("openolat") || {};
				if(data.listOfPairs == undefined) {
					data.currentSpot = '';
					data.listOfPairs = [];
					jQuery("#<xsl:value-of select="$appletContainerId"/>_container").data('openolat', data);
				}

				var areaId = jQuery(this).attr('id');
				
				if(data.currentSpot == '' || data.currentSpot == areaId) {
					data.currentSpot = areaId;
				} else {
					var newPair = [data.currentSpot, areaId];
					data.listOfPairs.push(newPair);
					data.currentSpot = '';
				}

				var canvas = document.getElementById('<xsl:value-of select="$appletContainerId"/>_canvas');
				var c = canvas.getContext('2d');
				c.clearRect(0,0,jQuery(canvas).width(),jQuery(canvas).height());
				
				var divContainer = jQuery('#<xsl:value-of select="$appletContainerId"/>_container');
				divContainer.find("input[type='hidden']").remove();
				
				var drawedSpots = [];
				if(data.currentSpot != '') {
					drawArea(c, data.currentSpot);
					drawedSpots.push(data.currentSpot);
				}

				for(var i=data.listOfPairs.length; i-->0; ) {
					var pair = data.listOfPairs[i];
					for(var j=pair.length; j-->0; ) {
						if(0 > drawedSpots.indexOf(pair[j])) {
							drawArea(c, pair[j]);
							drawedSpots.push(pair[j]);
						}
					}
					
					var coords1 = toCoords(jQuery('#' + pair[1]));
					var coords2 = toCoords(jQuery('#' + pair[0]));
					
					c.beginPath();
					c.moveTo(coords1[0], coords1[1]);
					c.lineTo(coords2[0], coords2[1]);
					c.lineWidth = 3;
					c.stroke();
					
					var inputElement = jQuery('<input type="hidden"/>')
						.attr('name', 'qtiworks_response_RESPONSE')
						.attr('value', pair[0] + " " + pair[1]);
					divContainer.prepend(inputElement);
				}
			});
		});
			
			</xsl:otherwise>
		</xsl:choose>
		
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
