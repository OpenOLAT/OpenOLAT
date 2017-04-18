(function ($) {
    $.fn.hotspotInteraction = function(options) {
    	var settings = $.extend({
    		responseIdentifier: null,
    		formDispatchFieldId: null,
    		maxChoices: 1,
    		responseValue: null,
    		opened: false
        }, options );
    	
    	try {
    		if(!(typeof settings.responseValue === "undefined") && settings.responseValue.length > 0) {
    			drawHotspotAreas(this, settings);
    		} 
    		if(settings.opened) {
    			hotspots(this, settings);
    		}
    	} catch(e) {
    		if(window.console) console.log(e);
    	}
        return this;
    };
    
    function drawHotspotAreas($obj, settings) {
    	var containerId = $obj.attr('id');
    	var divContainer = jQuery('#' + containerId);
    	
    	var areaIds = settings.responseValue.split(',');
    	for(i=areaIds.length; i-->0; ) {
    		var areaEl = jQuery('#ac_' + settings.responseIdentifier + '_' + areaIds[i]);
    		var data = areaEl.data('maphilight') || {};
    		data.selectedOn = true;
    		colorData(data);
    		areaEl.data('maphilight', data).trigger('alwaysOn.maphilight');
    		
    		var inputElement = jQuery('<input type="hidden"/>')
				.attr('name', 'qtiworks_response_' + settings.responseIdentifier)
				.attr('value', areaEl.data('qti-id'));
    		divContainer.append(inputElement);
    	}
    }
    
    function hotspots($obj, settings) {
    	var containerId = $obj.attr('id');
    	jQuery('#' + containerId + " map area").each(function(index, el) {
    		jQuery(el).on('click', function() {
    			clickHotspotArea(this, containerId, settings.responseIdentifier, settings.maxChoices);
    		});
    	})
    };

    function clickHotspotArea(spot, containerId, responseIdentifier, maxChoices) {
    	var areaEl = jQuery(spot);
    	var data = areaEl.data('maphilight') || {};
    	if((typeof data.selectedOn === "undefined") || !data.selectedOn) {
    		var numOfChoices = maxChoices;
    		if(numOfChoices > 0) {
    			var countChoices = 0;
    			jQuery("area", "map[name='" + containerId + "_map']").each(function(index, el) {
    				var cData = jQuery(el).data('maphilight') || {};
    				if(cData.selectedOn) {
    					countChoices++;
    				}
    			});
    			if(countChoices >= numOfChoices) {
    				return false;
    			}
    		}
    	}
    	
    	if(typeof data.selectedOn === "undefined") {
    		data.selectedOn = true;
    	} else {
    		data.selectedOn = !data.selectedOn;
    	}
    	colorData(data);
    	areaEl.data('maphilight', data).trigger('alwaysOn.maphilight');

    	var divContainer = jQuery('#' + containerId);
    	divContainer.find("input[type='hidden']").remove();
    	jQuery("area", "map[name='" + containerId + "_map']").each(function(index, el) {
    		var cAreaEl = jQuery(el);
    		var cData = cAreaEl.data('maphilight') || {};
    		if(cData.selectedOn) {
    			var inputElement = jQuery('<input type="hidden"/>')
    				.attr('name', 'qtiworks_response_' + responseIdentifier)
    				.attr('value', cAreaEl.data('qti-id'));
    			divContainer.append(inputElement);
    		}
    	});
    };
    
    /*
     * Color the data based on the selectedOn flag
     */
    function colorData(data) {
    	if(data.selectedOn) {
        	data.fillColor = '0000ff';
        	data.fillOpacity = 0.5;
    		data.strokeColor = '0000ff';
    		data.strokeOpacity = 1;
    		data.shadow = true;
    		data.shadowX = 0;
    		data.shadowY = 0;
    		data.shadowRadius = 7;
    		data.shadowColor = '000000';
    		data.shadowOpacity = 0.8;
    		data.shadowPosition = 'outside';
    	} else {
			data.fillColor = 'bbbbbb';
        	data.fillOpacity = 0.5;
    		data.strokeColor = '666666';
    		data.strokeOpacity = 0.8;
    		data.shadow = false;
    	}
    }
}( jQuery ));
