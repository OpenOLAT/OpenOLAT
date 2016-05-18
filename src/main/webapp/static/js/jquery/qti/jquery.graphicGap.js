(function ($) {
    $.fn.graphicGapInteraction = function(options) {
    	var settings = $.extend({
    		responseIdentifier: null,
    		formDispatchFieldId: null,
    		responseValue: null,
    		opened: false
        }, options );
    	
    	try {
    		if(!(typeof settings.responseValue === "undefined") && settings.responseValue.length > 0) {
    			drawGaps(this, settings);
    		}
    		if(settings.opened) {
    			associateGaps(this, settings);
    		}
    	} catch(e) {
    		if(window.console) console.log(e);
    	}
        return this;
    };
    
    function drawGaps($obj, settings) {
    	var containerId = $obj.attr('id');
    	var divContainer = jQuery('#' + containerId);
    	
    	var pairs = settings.responseValue.split(',');
    	for(var i=pairs.length; i-->0; ) {
    		var ids = pairs[i].split(' ');
    		
    		var item1 = jQuery('#ac_' + settings.responseIdentifier + '_' + ids[0]);
    		var item2 = jQuery('#ac_' + settings.responseIdentifier + '_' + ids[1]);

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

			var inputElement = jQuery('<input type="hidden"/>')
				.attr('name', 'qtiworks_response_' + settings.responseIdentifier)
				.attr('value', gapitem.data('qti-id') + " " + areaEl.data('qti-id'));
			divContainer.prepend(inputElement);
    	}
    };

    function associateGaps($obj, settings) {
    	var containerId = $obj.attr('id');
    	jQuery(".gap_item").on('click', function(e, el) {
    		var gapitem = jQuery(this);
    		
    		if(gapitem.hasClass('oo-choosed')) {
    			gapitem.removeClass('oo-choosed');
    			//gapitem.css('position','relative');
    			//gapitem.css('left','auto');
    			//gapitem.css('top','auto');
    			
    			var gapitemId = gapitem.attr('id');
    			//remove
    			jQuery('#' + containerId).find("input[type='hidden']").each(function(index, el) {
    				var value = jQuery(el).val();
    				if(value.indexOf(gapitemId + ' ') == 0) {
    					jQuery(el).remove();
    				}
    			});
    		} else {
    			//gapitem.css('border','3px solid grey');
    			gapitem.addClass('oo-selected');
    		}
    	});
    	
    	jQuery("#" + containerId + " area").on('click', function(e, el) {
    		var areaEl = jQuery(this);
    		jQuery(".gap_item.oo-selected").each(function(index, el){
    			var gapitem = jQuery(el);
    			var coords = toCoords(areaEl);
    			var areaId = areaEl.data('qti-id');
    			var gapitemId = gapitem.data('qti-id');
    			
    			gapitem.css('position','absolute');
    			gapitem.css('left', coords[0] + 'px');
    			gapitem.css('top', coords[1] + 'px');
    		
    			gapitem.css('border', 'none');
    			gapitem.removeClass('oo-selected');
    			gapitem.addClass('oo-choosed');
    			
    			//add
    			var divContainer = jQuery('#' + containerId);
    			var inputElement = jQuery('<input type="hidden"/>')
    				.attr('name', 'qtiworks_response_' + settings.responseIdentifier)
    				.attr('value', gapitemId + " " + areaId);
    			divContainer.prepend(inputElement);
    		});
    	});
    };
    
    function toCoords(area) {
    	var coords = area.attr('coords').split(',');
    	for (i=coords.length; i-->0; ) {
    		coords[i] = parseFloat(coords[i]);
    	}
    	return coords;
    }
}( jQuery ));
