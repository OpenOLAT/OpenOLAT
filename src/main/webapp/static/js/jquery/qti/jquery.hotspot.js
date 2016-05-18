(function ($) {
    $.fn.hotspotInteraction = function(options) {
    	var settings = $.extend({
    		responseIdentifier: null,
    		formDispatchFieldId: null,
    		maxChoices: 1,
    		responseValue: null
        }, options );
    	
    	try {
    		if(settings.responseValue == "") {
    			hotspots(this, settings);
    		} else {
    			drawHotspotAreas(settings);
    			hotspots(this, settings);
    		}
    	} catch(e) {
    		if(window.console) console.log(e);
    	}
        return this;
    };
    
    function drawHotspotAreas(settings) {
    	var areaIds = settings.responseValue.split(',');
    	for(i=areaIds.length; i-->0; ) {
    		var areaEl = jQuery('#ac_' + settings.responseIdentifier + '_' + areaIds[i]);
    		var data = areaEl.data('maphilight') || {};
    		data.alwaysOn = true;
    		areaEl.data('maphilight', data).trigger('alwaysOn.maphilight');
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
    	if(!data.alwaysOn) {
    		var numOfChoices = maxChoices;
    		if(numOfChoices > 0) {
    			var countChoices = 0;
    			jQuery("area", "map[name='" + containerId + "_map']").each(function(index, el) {
    				var cData = jQuery(el).data('maphilight') || {};
    				if(cData.alwaysOn) {
    					countChoices++;
    				}
    			});
    			if(countChoices >= numOfChoices) {
    				return false;
    			}
    		}
    	}
    	data.alwaysOn = !data.alwaysOn;
    	areaEl.data('maphilight', data).trigger('alwaysOn.maphilight');

    	var divContainer = jQuery('#' + containerId);
    	divContainer.find("input[type='hidden']").remove();
    	jQuery("area", "map[name='" + containerId + "_map']").each(function(index, el) {
    		var cAreaEl = jQuery(el);
    		var cData = cAreaEl.data('maphilight') || {};
    		if(cData.alwaysOn) {
    			var inputElement = jQuery('<input type="hidden"/>')
    				.attr('name', 'qtiworks_response_' + responseIdentifier)
    				.attr('value', cAreaEl.data('qti-id'));
    			divContainer.append(inputElement);
    		}
    	});
    }
}( jQuery ));
