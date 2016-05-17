(function ($) {
    $.fn.positionObjectStage = function(options) {
    	var settings = $.extend({
    		responseIdentifier: null,
    		formDispatchFieldId: null,
    		responseValue: null,
    		maxChoices: 1
        }, options );
    	
    	try {
    		if(settings.responseValue == "") {
    			selecObjects(this, settings);
    		} else {
    			drawObjects(this, settings);
    			selecObjects(this, settings);
    		}
    	} catch(e) {
    		if(window.console) console.log(e);
    	}
        return this;
    };
    
    function drawObjects( $obj, settings) {
    	var containerId = $obj.attr('id');
    	var positions = settings.responseValue.split(':');
    	var items = jQuery('#' + containerId + ' .items_container .o_item.o_' + settings.responseIdentifier);
    	for(var i=positions.length; i-->0; ) {
    		var pos = positions[i].split(' ');
    		console.log(pos);
    		var item = jQuery(items.get(i));
    		item.css('position', 'absolute');
    		item.css('top', pos[1] + 'px');
    		item.css('left', pos[0] + 'px');
    	}
    };

    function selecObjects($obj, settings) {
    	var containerId = $obj.attr('id');
    	jQuery('#' + containerId + ' .items_container .o_item.o_' + settings.responseIdentifier).each(function(index, el) {
    		jQuery(el).attr('id','object-item-' + index);
    	}).draggable({
    		containment: "#" + containerId,
    		scroll: false,
    		stop: function( event, ui ) {
    			var imgEl = jQuery('#' + containerId + '_img');
    			var img_offset_t = jQuery(imgEl).offset().top - jQuery(window).scrollTop();
    			var img_offset_l = jQuery(imgEl).offset().left - jQuery(window).scrollLeft();
    			
    			var offset_t = jQuery(this).offset().top - jQuery(window).scrollTop();
    			var offset_l = jQuery(this).offset().left - jQuery(window).scrollLeft();

    			var cx = Math.round( (offset_l - img_offset_l) );
    			var cy = Math.round( (offset_t - img_offset_t) );
    			
    			var itemId = jQuery(this).attr('id');
    			var inputId = 'in-' + itemId + '-' + settings.responseIdentifier;
    			var divContainer = jQuery('#' + containerId);
    			var inputEl = divContainer.find(inputId);
    			if(inputEl.length == 0) {
    				var inputElement = jQuery('<input type="hidden"/>')
    					.attr('id', inputId)
    					.attr('name', 'qtiworks_response_' + settings.responseIdentifier)
    					.attr('value', cx + " " + cy);
    				divContainer.prepend(inputElement);
    			} else {
    				inputEl.val(cx + " " + cy);
    			}
                setFlexiFormDirty(settings.formDispatchFieldId);
    		}
    	});
    }  
}( jQuery ));
