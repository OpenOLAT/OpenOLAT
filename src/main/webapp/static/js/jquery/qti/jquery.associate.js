(function ($) {
    $.fn.associateInteraction = function(options) {
    	var settings = $.extend({
    		responseIdentifier: null,
    		formDispatchFieldId: null,
    		responseValue: null,
    		opened: false
        }, options);
    	
    	try {
    		if(typeof settings.responseValue != "undefined" && settings.responseValue.length > 0) {
    			drawAssociations(this, settings);
    		}
    		if(settings.opened) {
    			associate(this, settings);
    		}
    	} catch(e) {
    		if(window.console) console.log(e);
    	}
        return this;
    };
    
    drawAssociations = function($obj, settings) {
    	var containerId = $obj.attr('id');
    	var associationPairs = settings.responseValue.split(',');
    	var associationEls = jQuery('#' + containerId + '_panel .association');
    	for(var i=associationPairs.length; i-->0; ) {
    		var associationPair = associationPairs[i].split(' ');
    		var associationEl = jQuery(associationEls.get(i));
    		var association1 = jQuery('#ac_' + settings.responseIdentifier + '_' + associationPair[0]);
    		var association2 = jQuery('#ac_' + settings.responseIdentifier + '_' + associationPair[1]);
    		jQuery(association1).css('border','none').addClass('oo-choosed');
    		jQuery(association2).css('border','none').addClass('oo-choosed');
    		
    		jQuery(associationEl.find('.association_box.left'))
    			.css('border','3px solid grey')
    			.append(association1);
    		jQuery(associationEl.find('.association_box.right'))
    			.css('border','3px solid grey')
    			.append(association2);
    	}
    };

    associate = function($obj, settings) {
    	var containerId = $obj.attr('id');
    	jQuery("#" + containerId + " .o_associate_item").on('click', function(e, el) {
    		var itemEl = jQuery(this);
    		if(itemEl.hasClass('oo-choosed')) {
    			itemEl.removeClass('oo-choosed');
    			itemEl.parent('.association_box').each(function(index, el) {
    				jQuery(el).css('border', '3px dotted grey');
    			});
    			itemEl.css('border', '2px solid grey');
    			itemEl.appendTo(jQuery('#' + containerId +'_items'));
    		} else {
    			itemEl.css('border', '2px solid red');
    			itemEl.addClass('oo-selected');
    		}
    	}).draggable({
    		containment: "#" + containerId,
    		scroll: false,
    		revert: "invalid",
    		stop: function(event, ui) {
    			jQuery(this).css({'left': '0px', 'top': '0px' });
    		}
    	}).on('click', {formId: settings.formDispatchFieldId}, setFlexiFormDirtyByListener);
    	
    	jQuery("#" + containerId + "_panel .association_box").on('click', function(e, el) {
    		var box = jQuery(this);
    		jQuery("#" + containerId + "_items .o_associate_item.oo-selected").each(function(index, selectedEl) {
    			jQuery(selectedEl)
    				.css('border', 'none')
    				.removeClass('oo-selected')
    				.addClass('oo-choosed');
    			box.append(selectedEl);
    			box.css('border', '3px solid grey');
    			recalculate(containerId, settings);
    		});
    	}).droppable({
    		drop: function(event, ui) {
    			var itemEl = ui.draggable;
    			var box = jQuery(this);
    			itemEl
					.css('border', 'none') 
					.removeClass('oo-selected')
					.addClass('oo-choosed');
    			box.append(itemEl);
    			box.css('border', '3px solid grey');
    		}
    	}).on('click', {formId: settings.formDispatchFieldId}, setFlexiFormDirtyByListener);
    };
    
    recalculate = function (containerId, settings) {
    	var divContainer = jQuery('#' + containerId + '_panel');
    	divContainer.find("input[type='hidden']").remove();
    	
    	jQuery("#" + containerId + "_panel .association").each(function(index, associationEl) {
    		var associations = jQuery(associationEl).find('.o_associate_item');
    		if(associations.length == 2) {
    			var id1 = jQuery(associations.get(0)).data('qti-id');
    			var id2 = jQuery(associations.get(1)).data('qti-id');			
    			var inputElement = jQuery('<input type="hidden"/>')
    					.attr('name', 'qtiworks_response_' + settings.responseIdentifier)
    					.attr('value', id1 + " " + id2);
    			divContainer.prepend(inputElement);
    		}
    	});
    }   
}( jQuery ));
