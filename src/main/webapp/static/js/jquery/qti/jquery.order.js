(function ($) {
	$.fn.orderInteraction = function(options) {
		var settings = $.extend({
			responseIdentifier: null,
			formDispatchFieldId: null,
			minChoices: null,
			maxChoices: null,
			responseValue: null,
			opened: false
		}, options );

		try {
			if(typeof settings.responseValue != "undefined" && settings.responseValue.length > 0) {
				recalculate(settings);
			}
			if(settings.opened) {
				order(this, settings);
			}
    		} catch(e) {
    			if(window.console) console.log(e);
    		}
        return this;
	};

	function order($obj, settings) {
		var containerId = '#qtiworks_response_' + settings.responseIdentifier;
		var sourceList = jQuery(containerId + ' div.source > ul');
		var targetList = jQuery(containerId + ' div.target > ul');
		var sourceContainer = jQuery(containerId + ' div.source > ul').get(0);
		var targetContainer = jQuery(containerId + ' div.target > ul').get(0);

		var drake = dragula([sourceContainer, targetContainer], {
			copy: function (el, source) {
			    return false;
			},
			accepts: function (el, target) {
				return target !== sourceContainer;
			}
		});

		drake.on('drag', function(el, container) {
	        //
		}).on('over', function(el, container, source){
	        if(container === targetContainer) {
	        		jQuery(container).addClass('oo-accepted');
	        }
		}).on('drop', function(el, target, source, sibling) {
			jQuery(el).removeClass('oo-selected');
			recalculate(settings);
			setFlexiFormDirty(settings.formDispatchFieldId);
		}).on('dragend', function(el) {
    		jQuery(targetContainer).removeClass('oo-accepted');
		}).on('out', function(el) {
    		jQuery(targetContainer).removeClass('oo-accepted');
		});
		
		initializeSourceClick(sourceList, targetList);
		initializeTargetClick(sourceList, targetList, settings);
	}
	
	function initializeSourceClick(sourceList, targetList) {
		var sources = sourceList.find('li');
		sources.on('click', function(e, el) {
			handleSourceClick(jQuery(this), sourceList, targetList);
		});
	}

	function handleSourceClick(listItem, sourceList, targetList) {
		if (!listItem.hasClass('oo-selected')) {
			unselectAllItems(sourceList, targetList);
			listItem.addClass('oo-selected');
			targetList.addClass('oo-selected');
		} else {
			unselectAllItems(sourceList, targetList);
			targetList.removeClass('oo-selected');
		}
	}

	function initializeTargetClick(sourceList, targetList, settings) {
		targetList.on('click', function(e, el) { 
			if (e.target === targetList.get(0)) {
				handleTargetBackgroundClick(e, sourceList, targetList, settings);
			} else {
				handleTargetClick(e.target, sourceList, targetList);
			}
		});
	}
	
	function handleTargetClick(clickedEl, sourceList, targetList) {
		var listItem = jQuery(clickedEl).closest('li.o_assessmentitem_order_item');
		if (listItem.length > 0) {
			unselectAllItems(sourceList, targetList);
			targetList.removeClass('oo-selected');

			var clone = listItem.clone();
			clone.on('click', function(e, el) {
				handleSourceClick(jQuery(this), sourceList, targetList);
			});
			sourceList.append(clone);
			listItem.remove();
			
			recalculate(settings);
		}
	}
	
	function unselectAllItems(sourceList, targetList) {
		var targets = targetList.find('li');
		targets.each(function(index, el) {
			jQuery(el).removeClass('oo-selected');
		});
		var sources = sourceList.find('li');
		sources.each(function(index, el) {
			jQuery(el).removeClass('oo-selected');
		});
	}
	
	function handleTargetBackgroundClick(e, sourceList, targetList, settings) {
		var selectedSourceEl = sourceList.find('li.oo-selected');
		if (selectedSourceEl.length === 0) {
			return;
		}
		selectedSourceEl.removeClass('oo-selected');

		var clone = selectedSourceEl.clone();
		targetList.append(clone);
		targetList.removeClass('oo-selected');
		selectedSourceEl.remove();
		
		recalculate(settings);
	}

	function recalculate(settings) {
		var containerId = '#qtiworks_response_' + settings.responseIdentifier;
		var sourceList = jQuery(containerId + ' div.source > ul');
		var targetList = jQuery(containerId + ' div.target > ul');

		var targetBox = jQuery(containerId + ' div.target');
		var hiddenInputContainer = jQuery(containerId + ' div.hiddenInputContainer');

		var selectedCount = targetList.children('li').length;
		if (settings.minChoices != null && settings.maxChoices != null) {
			if (selectedCount < settings.minChoices || selectedCount > settings.maxChoices) {
				if (settings.minChoices != settings.maxChoices) {
					alert("You must select and order between "
							+ settings.minChoices + " and "
							+ settings.maxChoices + " items");
				} else {
					alert("You must select and order exactly "
							+ settings.minChoices + " item"
							+ (minChoices > 1 ? "s" : ""));
				}
				targetBox.toggleClass('highlight', true);
				return false;
			} else {
				targetBox.toggleClass('highlight', false);
			}
		}

		var hiddenInputContainer = jQuery(containerId + ' div.hiddenInputContainer');

		hiddenInputContainer.empty();
		targetList.children('li').each(function(index) {
			var choiceId = this.id.substring('qtiworks_response_'.length); // Trim leading 'qtiworks_response_'
			var inputElement = jQuery('<input type="hidden">');
			inputElement.attr('name', 'qtiworks_response_' + settings.responseIdentifier);
			inputElement.attr('value', choiceId);
			hiddenInputContainer.append(inputElement);
		});
	}
}( jQuery ));
