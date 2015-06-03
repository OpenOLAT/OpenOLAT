<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="2.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:qti="http://www.imsglobal.org/xsd/imsqti_v2p1"
  xmlns:qw="http://www.ph.ed.ac.uk/qtiworks"
  xmlns="http://www.w3.org/1999/xhtml"
  exclude-result-prefixes="qti qw xs">

  <xsl:template match="qti:graphicGapMatchInteraction">
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
      <xsl:variable name="gapImgs" select="qw:filter-visible(qti:gapImg)" as="element(qti:gapImg)*"/>
      <xsl:variable name="responseValue" select="qw:get-response-value(/, @responseIdentifier)" as="element(qw:responseVariable)?"/>
          
      <div id="{$appletContainerId}" class="appletContainer" style="position:relative;">
      	<img id="{$appletContainerId}_img" width="{$object/@width}" height="{$object/@height}" src="{qw:convert-link-full($object/@data)}" usemap="#{$appletContainerId}_map"></img>
		<map name="{$appletContainerId}_map">
			<xsl:for-each select="$hotspots">
				<!-- @matchGroup, @matchMax -->
				<area id="{@identifier}" shape="{@shape}" coords="{@coords}" href="#" title="{@label}" class="area-droppable"></area>
			</xsl:for-each>
		</map>
		<div class="gap_container" style="width:{$object/@width}px; height:50px; background-color:#cccccc;">
			<xsl:for-each select="$gapImgs">
				<!-- @matchGroup, @matchMax, @label -->
            	<div id="{@identifier}" class="gap_item" style="width:{qti:object/@width}px; height:{qti:object/@height}px; margin:5px; background-image:url('{qw:convert-link-full(qti:object/@data)}');"> </div>
          </xsl:for-each>
		</div>
		
		<script type="text/javascript">
		<![CDATA[
      	toCoords = function(area) {
			var coords = area.attr('coords').split(',');
			for (i=0; i < coords.length; i++) {
				coords[i] = parseFloat(coords[i]);
			}
			return coords;
		};
      		]]>
		<xsl:choose>
			<xsl:when test="qw:is-not-null-value($responseValue)">
			
		jQuery(function() {
			var pairs = '<xsl:value-of select="$responseValue/qw:value" separator=","/>'.split(',');
			for(var i=pairs.length; i-->0; ) {
				var ids = pairs[i].split(' ');
				
				var item1 = jQuery('#' + ids[0]);
				var item2 = jQuery('#' + ids[1]);
				
				var gapitem, areaEl;
				if(item1.hasClass('gap_item')) {
					gapitem = item1;
					areaEl = item2;
				} else {
					gapitem = item2;
					areaEl = item1;
				}
				
				var coords = toCoords(areaEl);
				gapitem.css('position','absolute');
	    		gapitem.css('left', coords[0] + 'px');
	    		gapitem.css('top', coords[1] + 'px');
	    		gapitem.addClass('oo-choosed');
			}
		});
			
			</xsl:when>
			<xsl:otherwise>
			
		jQuery(function() {
	    	
	    	jQuery(".gap_item").on('click', function(e, el) {
	    		var gapitem = jQuery(this);
	    		
	    		if(gapitem.hasClass('oo-choosed')) {
	    			gapitem.removeClass('oo-choosed');
	    			gapitem.css('position','relative');
	    			gapitem.css('left','auto');
	    			gapitem.css('top','auto');
	    			
	    			var gapitemId = gapitem.attr('id');
	    			//remove
	    			jQuery('#<xsl:value-of select="$appletContainerId"/>').find("input[type='hidden']").each(function(index, el) {
	    				var value = jQuery(el).val();
	    				if(value.indexOf(gapitemId + ' ') == 0) {
	    					jQuery(el).remove();
	    				}
	    			});
	    		} else {
	    			gapitem.css('border','3px solid black');
	    			gapitem.addClass('oo-selected');
	    		}
	    	});
	    	
	    	jQuery("#<xsl:value-of select="$appletContainerId"/> area").on('click', function(e, el) {
	    		var areaEl = jQuery(this);
	    		jQuery(".gap_item.oo-selected").each(function(index, el){
	    			var gapitem = jQuery(el);
	    			var coords = toCoords(areaEl);
	    			var areaId = areaEl.attr('id');
	    			var gapitemId = gapitem.attr('id');
	    			
	    			gapitem.css('position','absolute');
	    			gapitem.css('left', coords[0] + 'px');
	    			gapitem.css('top', coords[1] + 'px');
	    		
	    			gapitem.css('border', 'none');
	    			gapitem.removeClass('oo-selected');
	    			gapitem.addClass('oo-choosed');
	    			
	    			//add
	    			var divContainer = jQuery('#<xsl:value-of select="$appletContainerId"/>');
					var inputElement = jQuery('<input type="hidden"/>')
						.attr('name', 'qtiworks_response_RESPONSE')
						.attr('value', gapitemId + " " + areaId);
					divContainer.prepend(inputElement);
	    		});
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
          <param name="baseType" value="directedPair"/>
          <param name="operation_mode" value="gap_match_interaction"/>
          <param name="number_of_responses" value="{count(qti:associableHotspot)}"/>
          <param name="background_image" value="{qw:convert-link-full($object/@data)}"/>
          <xsl:variable name="hotspots" select="qw:filter-visible(qti:associableHotspot)" as="element(qti:associableHotspot)*"/>
          <param name="hotspot_count" value="{count($hotspots)}"/>
          <xsl:for-each select="$hotspots">
            <param name="hotspot{position()-1}">
              <xsl:attribute name="value"><xsl:value-of select="@identifier"/>::::<xsl:value-of select="@shape"/>::<xsl:value-of select="@coords"/><xsl:if test="@label">::hotSpotLabel:<xsl:value-of select="@label"/></xsl:if><xsl:if test="@matchGroup">::<xsl:value-of select="translate(normalize-space(@matchGroup), ' ', '::')"/></xsl:if><xsl:if test="@matchMax">::maxAssociations:<xsl:value-of select="@matchMax"/></xsl:if></xsl:attribute>
            </param>
          </xsl:for-each>
          <xsl:variable name="gapImgs" select="qw:filter-visible(qti:gapImg)" as="element(qti:gapImg)*"/>
          <param name="movable_element_count" value="{count($gapImgs)}"/>
          <xsl:for-each select="$gapImgs">
            <param name="movable_object{position()-1}">
              <xsl:attribute name="value"><xsl:value-of select="@identifier"/>::<xsl:value-of select="qw:convert-link(qti:object/@data)"/>::<xsl:if test="@label">::hotSpotLabel:<xsl:value-of select="@label"/></xsl:if><xsl:if test="@matchGroup">::<xsl:value-of select="translate(normalize-space(@matchGroup), ' ', '::')"/></xsl:if><xsl:if test="@matchMax">::maxAssociations:<xsl:value-of select="@matchMax"/></xsl:if></xsl:attribute>
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
