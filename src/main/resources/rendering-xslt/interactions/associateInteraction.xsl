<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="2.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:qti="http://www.imsglobal.org/xsd/imsqti_v2p1" 
  xmlns:qw="http://www.ph.ed.ac.uk/qtiworks"
  xmlns="http://www.w3.org/1999/xhtml"
  exclude-result-prefixes="qti xs qw">

  <xsl:template match="qti:associateInteraction">
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
      <xsl:variable name="appletContainerId" select="concat('qtiworks_id_appletContainer_', @responseIdentifier)" as="xs:string"/>
      <xsl:variable name="responseValue" select="qw:get-response-value(/, @responseIdentifier)" as="element(qw:responseVariable)?"/>
          
      <div id="{$appletContainerId}" class="appletContainer">
        <div id="{$appletContainerId}_items" style="padding:15px;">
        	<xsl:variable name="choices" as="element(qti:simpleAssociableChoice)*" select="qw:filter-visible(qti:simpleAssociableChoice)"/>
  
        	<xsl:for-each select="$choices">
            	<div id="{@identifier}" class="o_associate_item" style="width: 100px; float:left; margin-right:15px; border:2px solid grey;">
              		<xsl:apply-templates/>
            	</div>
			</xsl:for-each>
			<div style="clear:both; "></div>
		</div>
       <div id="{$appletContainerId}_panel" style="max-width:500px;">
			<xsl:for-each select="1 to @maxAssociations">
              	<div class="association" style="">
					<div class="association_box left" style="width: 100px; height:50px; float:left; border: 3px dotted grey;"></div>
					<div class="association_box right" style="width: 100px; height:50px; float:right; border: 3px dotted grey;"></div>
					<div style="clear:both; "></div>
				</div>
            </xsl:for-each>
			<div style="clear:both; "></div>
		</div>
		
		<script type="text/javascript">
		<xsl:choose>
			<xsl:when test="qw:is-not-null-value($responseValue)">
			
	jQuery(function() {
		var associationPairs = '<xsl:value-of select="$responseValue/qw:value" separator=","/>'.split(',');
		var associationEls = jQuery('#<xsl:value-of select="$appletContainerId"/>_panel .association');
		for(var i=associationPairs.length; i-->0; ) {
			var associationPair = associationPairs[i].split(' ');
			var associationEl = jQuery(associationEls.get(i));
			var association1 = jQuery('#' + associationPair[0]);
			var association2 = jQuery('#' + associationPair[1]);
			jQuery(association1).css('border','none');
			jQuery(association2).css('border','none');
			
			jQuery(associationEl.find('.association_box.left'))
				.css('border','3px solid grey')
				.append(association1);
			jQuery(associationEl.find('.association_box.right'))
				.css('border','3px solid grey')
				.append(association2);
		}
	});	
			
			</xsl:when>
			<xsl:otherwise>
			
	function recalculateAssociations() {
		var divContainer = jQuery('#<xsl:value-of select="$appletContainerId"/>_panel');
		divContainer.find("input[type='hidden']").remove();
		
		jQuery("#<xsl:value-of select="$appletContainerId"/>_panel .association").each(function(index, associationEl) {
			var associations = jQuery(associationEl).find('.o_associate_item');
			if(associations.length == 2) {
				var id1 = jQuery(associations.get(0)).attr('id');
				var id2 = jQuery(associations.get(1)).attr('id');			
				var inputElement = jQuery('<input type="hidden"/>')
						.attr('name', 'qtiworks_response_RESPONSE')
						.attr('value', id1 + " " + id2);
				divContainer.prepend(inputElement);
			}
		});
	};
	
	jQuery(function() {
    	jQuery("#<xsl:value-of select="$appletContainerId"/>_items .o_associate_item").on('click', function(e, el) {
    		var itemEl = jQuery(this);
    		if(itemEl.hasClass('oo-choosed')) {
    			itemEl.removeClass('oo-choosed');
    			itemEl.parent('.association_box').each(function(index, el) {
    				jQuery(el).css('border', '3px dotted grey');
    			});
    			itemEl.css('border', '2px solid grey');
    			jQuery('#<xsl:value-of select="$appletContainerId"/>_items').prepend(itemEl);
    		} else {
    			itemEl.css('border', '2px solid red');
    			itemEl.addClass('oo-selected');
    		}
    	});
    	
    	jQuery("#<xsl:value-of select="$appletContainerId"/>_panel .association_box").on('click', function(e, el) {
    		var box = jQuery(this);
    		jQuery("#<xsl:value-of select="$appletContainerId"/>_items .o_associate_item.oo-selected").each(function(index, selectedEl) {
    			jQuery(selectedEl)
    				.css('border', 'none')
    				.removeClass('oo-selected')
    				.addClass('oo-choosed');
    			box.append(selectedEl);
    			box.css('border', '3px solid grey');
				recalculateAssociations();
    		});
    	});
    });
			
			</xsl:otherwise>
		</xsl:choose>
		</script>
        
        <!--
        <object type="application/x-java-applet" height="360" width="360">
          <param name="code" value="BoundedGraphicalApplet"/>
          <param name="codebase" value="{$appletCodebase}"/>
          <param name="identifier" value="{@responseIdentifier}"/>
          <param name="baseType" value="pair" />
          <param name="operation_mode" value="graphic_associate_interaction" />
          <- (BoundedGraphicalApplet uses -1 to represent 'unlimited') ->
          <param name="number_of_responses" value="{if (@maxAssociations &gt; 0) then @maxAssocations else -1}"/>

          <xsl:variable name="choices" as="element(qti:simpleAssociableChoice)*" select="qw:filter-visible(qti:simpleAssociableChoice)"/>
          <param name="hotspot_count" value="{count($choices)}"/>
          <xsl:for-each select="$choices">
            <!- (Content is flowStatic, but we can only show strings in labels) ->
            <xsl:variable name="content" as="node()*">
              <xsl:apply-templates/>
            </xsl:variable>
            <param name="hotspot{position()-1}" value="{@identifier}::{normalize-space(string-join(for $n in $content return string($n), ''))}"/>
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
