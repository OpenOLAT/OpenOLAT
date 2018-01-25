(function ($) {
    $.fn.associateInteraction = function(options) {
    	var settings = $.extend({
    		responseIdentifier: null,
    		formDispatchFieldId: null,
    		responseValue: null,
    		opened: false,
    		unrestricted: false
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
    
    function drawAssociations($obj, settings) {
		var containerId = $obj.attr('id');
		var associationPairs = settings.responseValue.split(',');
		var associationEls = jQuery('#' + containerId + '_panel .association');
		for (var i = 0; i < associationPairs.length; i++) {
			var associationPair = associationPairs[i].split(' ');
			var associationEl = jQuery(associationEls.get(i));
			if (associationEl.size() == 0) {
				var boxId = newAssociationBox(containerId, settings);
				associationEl = jQuery('#' + boxId);
			}
			var association1 = jQuery("#" + containerId + "_items div[data-qti-id='" + associationPair[0] + "']");
			if (needToBeAvailable(association1, containerId)) {
				association1 = jQuery(association1).clone();
			}
			var association2 = jQuery("#" + containerId + "_items div[data-qti-id='" + associationPair[1] + "']");
			if (needToBeAvailable(association2, containerId)) {
				association2 = jQuery(association2).clone();
			}
			jQuery(association1).addClass('oo-choosed');
			jQuery(association2).addClass('oo-choosed');

			jQuery(associationEl.find('.association_box.left')).addClass('oo-filled').append(association1);
			jQuery(associationEl.find('.association_box.right')).addClass('oo-filled').append(association2);
		}

		recalculate(containerId, settings);

		if (settings.unrestricted && settings.opened) {
			addNewAssociationBoxAndEvents(containerId, settings);
		}
	};

	function associate($obj, settings) {
		var containerId = $obj.attr('id');
		var items = jQuery("#" + containerId + " .o_associate_item");
		initializeGapEvents(items, containerId, settings);
		var associationBox = jQuery("#" + containerId + "_panel .association_box");
		initializeAssociationBoxEvents(associationBox, containerId, settings);
    };

	function initializeGapEvents(jElements, containerId, settings) {
		jElements.on('click', function(e, el) {
			var itemEl = jQuery(this);
			if(!itemEl.hasClass('oo-choosed') && !itemEl.hasClass('oo-selected')) {
				itemEl.addClass('oo-selected');
    			}
		}).draggable({
			containment: "#" + containerId,
			scroll: false,
			revert: "invalid",
			stop: function(event, ui) {
				jQuery(this).css({'left': '0px', 'top': '0px' });
				jQuery(ui.helper).removeClass('oo-drag');
			},
			helper: function() {
				var choiceEl = jQuery(this);
				var boxed = choiceEl.parent('.association_box').size() > 0;
				if(!boxed && needToBeAvailable(this, containerId)) {
					choiceEl.removeClass('oo-selected');
					var cloned =  choiceEl.clone();// need some click / drag listeners
					jQuery(cloned)
						.attr('id', 'n' + guid())
						.data('qti-cloned','true')
						.addClass('oo-drag');
					return cloned;
				}
				choiceEl.addClass('oo-drag');
				return choiceEl;
			}
		}).on('click', {formId: settings.formDispatchFieldId}, setFlexiFormDirtyByListener);
    }

	function needToBeAvailable(selectedEl, containerId) {
		var choiceEl = jQuery(selectedEl);
		var matchMax = choiceEl.data("qti-match-max");
		var gapId = choiceEl.data("qti-id");
		var currentUsedGap = jQuery(
				"#" + containerId + "_panel div[data-qti-id='" + gapId + "']")
				.size();
		return (matchMax == 0 || currentUsedGap + 1 < matchMax);
	}

	function initializeAssociationBoxEvents(jElements, containerId, settings) {
		jElements.on('click', function(e, el) {
			var box = jQuery(this);
    		
			var hasItems = jQuery(".o_associate_item", this).size();
			if(hasItems == 1) {
				jQuery(".o_associate_item", this).each(function(index, selectedEl) {
					removeGap(selectedEl, box, containerId);
				});
			} else {
				jQuery("#" + containerId + "_items .o_associate_item.oo-selected").each(function(index, selectedEl) {
					var choiceEl = jQuery(selectedEl);
					if(needToBeAvailable(selectedEl, containerId)) {
						choiceEl.removeClass('oo-selected');
						moveGap(choiceEl.clone(), box, containerId);
					} else {
						moveGap(choiceEl, box, containerId);
					}
				});
			}
			recalculate(containerId, settings);
			setFlexiFormDirty(settings.formDispatchFieldId, false);
		}).droppable({
			drop: function(event, ui) {
				var box = jQuery(this);
				var hasItems = jQuery(".o_associate_item", this).size();
				if(hasItems  > 0) {
					jQuery(".o_associate_item", this).each(function(index, selectedEl) {
						removeGap(selectedEl, box, containerId);
					});
				}
    			
				var choiceEl;
				if(ui.helper != null && jQuery(ui.helper).data('qti-cloned') == 'true') {
					var choiceEl = jQuery(ui.draggable);
					choiceEl
						.removeClass('oo-selected')
						.removeClass('oo-drag');
					choiceEl = choiceEl.clone();
					initializeGapEvents(choiceEl, containerId, settings);
					moveGap(choiceEl, box, containerId);
				} else {
					var choiceEl = jQuery(ui.draggable);
					choiceEl
						.removeClass('oo-selected')
						.removeClass('oo-drag');
					moveGap(choiceEl, box, containerId);
				}
    			
				recalculate(containerId, settings);
				setFlexiFormDirty(settings.formDispatchFieldId, false);
			}
		}).on('click', {formId: settings.formDispatchFieldId}, setFlexiFormDirtyByListener);
    };
    
    function moveGap(choiceEl, box, containerId) {
		choiceEl
			.removeClass('oo-selected')
			.css({'left': '0px', 'top': '0px' })
			.addClass('oo-choosed')
			.appendTo(box);
		box.addClass('oo-filled');
    };
    
	function removeGap(selectedEl, box, containerId) {
		box.removeClass('oo-filled');
		var jSelectedEl = jQuery(selectedEl);
		jSelectedEl.removeClass('oo-choosed');
    	
		var gapId = jSelectedEl.data('qti-id');
		var availableGaps = jQuery("#" + containerId + "_items div[data-qti-id='" + gapId + "']").size();
		if(availableGaps == 0) {
			jSelectedEl.appendTo(jQuery('#' + containerId +'_items'));
		} else {
			jSelectedEl.remove();
		}
    };
    
    function guid() {
		function s4() {
		    return Math.floor((1 + Math.random()) * 0x10000).toString(16).substring(1);
		}
		return s4() + s4() + s4() + s4() + s4() + s4() + s4();
	};

	function recalculate(containerId, settings) {
		var divContainer = jQuery('#' + containerId + '_panel');
		divContainer.find("input[type='hidden']").remove();

		jQuery("#" + containerId + "_panel .association").each(function(index, associationEl) {
			var associations = jQuery(associationEl).find('.o_associate_item');
			if (associations.length == 2) {
				var id1 = jQuery(associations.get(0)).data('qti-id');
				var id2 = jQuery(associations.get(1)).data('qti-id');
				var inputElement = jQuery('<input type="hidden"/>')
					.attr('name', 'qtiworks_response_' + settings.responseIdentifier)
					.attr('value', id1 + " " + id2);
				divContainer.prepend(inputElement);
			} else {
				if (associations.length == 0 && settings.unrestricted) {
					associationEl.remove();// remove empty slots
				} else {
					jQuery(associationEl).find('.association_box').each(function(index, associationBoxEl) {
						var numOfItems = jQuery(associationBoxEl).find('.o_associate_item').size();
						if (numOfItems == 0 && jQuery(associationBoxEl).hasClass('oo-filled')) {
							jQuery(associationBoxEl).removeClass('oo-filled');
						}
					});
				}
			}
		});

		addNewAssociationBoxAndEvents(containerId, settings);
	};
    
	function addNewAssociationBoxAndEvents(containerId, settings) {
		if (!settings.unrestricted || !settings.opened)
			return;

		// all slots are full -> add a new one
		var divContainer = jQuery('#' + containerId + '_panel');
		var full = true;
		jQuery("#" + containerId + "_panel .association").each(function(index, associationEl) {
			var associations = jQuery(associationEl).find('.o_associate_item');
			if (associations.length != 2) {
				full = false;
			}
		});

		if (full) {
			var boxId = newAssociationBox(containerId, settings);
			var lastAssociationBox = jQuery("#" + boxId + " .association_box");
			initializeAssociationBoxEvents(lastAssociationBox, containerId, settings);
		}
	};
    
	function newAssociationBox(containerId, settings) {
		var boxId = guid();
			var slot = '<div id="' + boxId + '" class="association" style="">\n'
			     + '  <div class="association_box left" style="width: 100px; height:50px; float:left;"></div>\n'
			     + '  <div class="association_box right" style="width: 100px; height:50px; float:right;"></div>\n'
			     + '  <div style="clear:both; "></div>\n'
			     + '</div>\n';
		jQuery("#" + containerId + "_panel").append(slot);
		jQuery("#" + containerId + "_panel").append('<div style="clear:both; "></div>\n');
		return boxId;
    }
}( jQuery ));
