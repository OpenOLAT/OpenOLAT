(function ($) {
	"use strict";
	$.fn.matchInteractionDnd = function(options) {
		var settings = $.extend({
			responseIdentifier : null,
			formDispatchFieldId : null,
			maxAssociations : 1,
			responseValue : null,
			opened : false
		}, options);

		try {
			if (typeof settings.responseValue != "undefined" && settings.responseValue.length > 0) {
				drawMatch(this, settings);
			}
			if (settings.opened) {
				match(this, settings);
			}
		} catch (e) {
			if (window.console)
				console.log(e);
		}
		return this;
	};

	function drawMatch($obj, settings) {
		var containerId = $obj.attr('id');
		var associationPairs = settings.responseValue.split(',');
		for (var i=0; i<associationPairs.length; i++) {
			var associationArr = associationPairs[i].split(' ');
			var sourceId = associationArr[0];
			var targetId = associationArr[1];

			var sourceEl = jQuery("#" + containerId + " .o_match_dnd_sources li[data-qti-id='" + sourceId + "']");
			if (needToBeAvailable(sourceEl, containerId)) {
				sourceEl = jQuery(sourceEl).clone();
			}
			var targetEl = jQuery("#" + containerId + " .o_match_dnd_targets ul[data-qti-id='" + targetId + "']");

			jQuery(sourceEl).addClass('oo-choosed');
			jQuery(targetEl).addClass('oo-choosed');
			jQuery(targetEl).addClass('oo-filled').append(sourceEl);
			
			var scores = sourceEl.attr('data-qti-scores');
			if(scores != null && scores.length > 0) {
				var scoreArr = scores.split(';');
				for(var  j=0; j<scoreArr.length; j++) {
					var targetScore = scoreArr[j];
					if(targetScore.indexOf(targetId) == 0) {
						var score = targetScore.split('=')[1];
						sourceEl.append("<div style='text-align: right;' class='o_qti_score_infos'>" + score + "</div>");
					}
				}
			}
		}

		recalculate(containerId, settings);

		if (settings.unrestricted && settings.opened) {
			addNewAssociationBoxAndEvents(containerId, settings);
		}
	}

	function match($obj, settings) {
		var containerId = $obj.attr('id');
		initializeSourcePanelEvents(containerId, settings);
		var sources = jQuery("#" + containerId + " .o_match_dnd_source");
		initializeSourceEvents(sources, containerId, settings);
		var targets = jQuery("#" + containerId + " .o_match_dnd_target");
		initializeTargetEvents(targets, containerId, settings);
	}

	function initializeSourcePanelEvents(containerId, settings) {
		jQuery("#" + containerId + " .o_match_dnd_sources").droppable({
			tolerance : 'pointer',
			over : function(event, ui) {
				jQuery(this).addClass('oo-accepted');
			},
			out : function(event, ui) {
				jQuery(this).removeClass('oo-accepted');
			},
			drop : function(event, ui) {
				var box = jQuery(this);
				box.removeClass('oo-accepted');

				var choiceEl = jQuery(ui.draggable)
				var choiceQtiId = choiceEl.data('qti-id');
				var choiceInSources = box.find("li[data-qti-id='" + choiceQtiId + "']");
				if (choiceInSources.length > 0) {
					if (choiceEl.parents(".o_match_dnd_sources").length == 0) {
						choiceEl.remove();
					}
				} else {
					choiceEl.appendTo(box);
				}
				recalculate(containerId, settings);
				setFlexiFormDirty(settings.formDispatchFieldId, false);
			}
		}).on('click', {formId : settings.formDispatchFieldId}, setFlexiFormDirtyByListener);
	}

	function initializeSourceEvents(jElements, containerId, settings) {
		jElements.on('click', function(e, el) {
			var itemEl = jQuery(this);
			if(!itemEl.hasClass('oo-choosed') && !itemEl.hasClass('oo-selected')) {
				jQuery("#" + containerId + " .o_match_dnd_sources .o_match_dnd_source").each(function(index, el) {
					jQuery(el).removeClass('oo-selected');
				});
				itemEl.addClass('oo-selected');
			} else if(itemEl.parents(".o_match_dnd_targets").length > 0 && !itemEl.hasClass('oo-dropped-mrk')) {
				removeSourceFromTarget(itemEl, containerId);
				recalculate(containerId, settings);
				setFlexiFormDirty(settings.formDispatchFieldId, false);
			}
		}).draggable({
			containment: "#" + containerId,
			scroll: false,
			revert: "invalid",
			cursorAt: { left: 5, top: 5 },
			start: function(event, ui) {
				jQuery(ui.helper).removeClass('oo-dropped-mrk');
			},
			stop: function(event, ui) {
				jQuery(this).css({'left': '0px', 'top': '0px', 'z-index': '' });
				jQuery(ui.helper).removeClass('oo-drag');
			},
			helper: function() {
				var choiceEl = jQuery(this);
				var boxed = choiceEl.parent('.o_match_dnd_target').length > 0;
				if(!boxed && needToBeAvailable(this, containerId)) {
					choiceEl.removeClass('oo-selected');
					var cloned =  choiceEl.clone();// need some click / drag listeners
					jQuery(cloned)
						.attr('id', 'n' + guid())
 						.data('qti-cloned','true')
 						.addClass('oo-drag')
						.addClass('oo-drag-mrk')
						.css('z-index', 10)
 						.css('width', choiceEl.width())
						.css('height', choiceEl.height());
					return cloned;
				}
				choiceEl
					.addClass('oo-drag')
					.addClass('oo-drag-mrk')
					.css('z-index', 10);
				return choiceEl;
			}
		}).on('click', {formId: settings.formDispatchFieldId}, setFlexiFormDirtyByListener);
    }
    
    function guid() {
		function s4() {
		    return Math.floor((1 + Math.random()) * 0x10000).toString(16).substring(1);
		}
		return s4() + s4() + s4() + s4() + s4() + s4() + s4();
	}

    function needToBeAvailable(selectedEl, containerId) {
		var choiceEl = jQuery(selectedEl);
		if (choiceEl.parents(".o_match_dnd_target").length > 0) {
			return false;
		}
		var matchMax = choiceEl.data("qti-match-max");
		var gapId = choiceEl.data("qti-id");
		var currentUsed = jQuery("#" + containerId + " .o_match_dnd_targets li[data-qti-id='" + gapId + "']").length;
		return (matchMax == 0 || currentUsed + 1 < matchMax);
	}

    function initializeTargetEvents(jElements, containerId, settings) {
		jElements.on('click', function(e, el) {
			var box = this;
			var boxEl = jQuery(box);
			var hasItems = jQuery(".o_associate_item", this).length;
			if(hasItems == 0) {
				jQuery("#" + containerId + " .o_match_dnd_sources .oo-selected").each(function(index, selectedEl) {
					var choiceEl = jQuery(selectedEl);
					var choiceQtiId = choiceEl.data('qti-id');
					var currentItems = jQuery(".o_match_dnd_source[data-qti-id='" + choiceQtiId + "']", box).length;
					
					var moveAllowed = currentItems == 0;
					var targetMatchMax = jQuery(box).data("qti-match-max");
					if(targetMatchMax > 0) {
						var filled = jQuery(".o_match_dnd_source", box).length;
						if(filled >= targetMatchMax) {
							moveAllowed &= false;
						}
					}
					
					if(moveAllowed) {
						if(needToBeAvailable(selectedEl, containerId)) {
							choiceEl.removeClass('oo-selected');
							choiceEl = choiceEl.clone();
							moveSourceToTarget(choiceEl, boxEl, containerId);
							initializeSourceEvents(choiceEl, containerId, settings);
						} else {
							moveSourceToTarget(choiceEl, boxEl, containerId);
						}
					}
				});
			}
			recalculate(containerId, settings);
			setFlexiFormDirty(settings.formDispatchFieldId, false);
		}).droppable({
			tolerance: "pointer",
			accept: function(el) {
				var choiceQtiId = jQuery(el).data('qti-id');
				//check if the source is already in the target
				var dropAllowed = jQuery(".o_match_dnd_source[data-qti-id='" + choiceQtiId + "']", this).length == 0;
				if(dropAllowed) {
					var targetMatchMax = jQuery(this).data("qti-match-max");
					if(targetMatchMax > 0) {
						var filled = jQuery(".o_match_dnd_source", this).length;
						if(filled >= targetMatchMax) {
							dropAllowed = false;
						}
					}
				}
				return dropAllowed;
			},
			over: function(event, ui) {
				jQuery(this).addClass('oo-accepted');
			},
			out: function(event, ui) {
				jQuery(this).removeClass('oo-accepted');
			},
			drop: function(event, ui) {
				var box = jQuery(this);
				box.removeClass('oo-accepted');
    			
				var choiceEl= jQuery(ui.draggable);
				//prevent 2x the same source
				var choiceQtiId = choiceEl.data('qti-id');
				var currentItems = jQuery(".o_match_dnd_source[data-qti-id='" + choiceQtiId + "']", this).length;
				if(currentItems > 0) {
					return;
				}
    			
				if(ui.helper != null && jQuery(ui.helper).data('qti-cloned') == 'true') {
					choiceEl
						.removeClass('oo-selected')
						.removeClass('oo-drag');
					choiceEl = choiceEl.clone();
					initializeSourceEvents(choiceEl, containerId, settings);
					moveSourceToTarget(choiceEl, box, containerId);
				} else {
					choiceEl
						.removeClass('oo-selected')
						.removeClass('oo-drag');
					moveSourceToTarget(choiceEl, box, containerId);
				}
				//add (and remove later) drop marker to prevent click event with Firefox
				choiceEl.addClass('oo-dropped-mrk');
				setTimeout(function() {
					choiceEl.removeClass('oo-dropped-mrk');
				}, 100);
    			
				recalculate(containerId, settings);
				setFlexiFormDirty(settings.formDispatchFieldId, false);
			}
		}).on('click', {formId: settings.formDispatchFieldId}, setFlexiFormDirtyByListener);
    }

	function moveSourceToTarget(sourceEl, box, containerId) {
		var container = box.find("ul.o_match_dnd_target_drop_zone");
		sourceEl
			.removeClass('oo-selected')
			.css({'width' : 'auto', 'left' : '0px', 'top' : '0px', 'z-index' : ''})
			.addClass('oo-choosed').appendTo(container);
		box.addClass('oo-filled');
	}

	function removeSourceFromTarget(selectedEl, containerId) {
		var jSelectedEl = jQuery(selectedEl);
		jSelectedEl
			.removeClass('oo-choosed');

		var gapId = jSelectedEl.data('qti-id');
		var availableSources = jQuery("#" + containerId + " .o_match_dnd_sources li[data-qti-id='" + gapId + "']").length;
		if (availableSources == 0) {
			jSelectedEl
				.css({'width' : 'auto', 'left' : '0px', 'top' : '0px', 'z-index' : ''})
				.appendTo(jQuery('#' + containerId + ' .o_match_dnd_sources'));
		} else {
			jSelectedEl.remove();
		}
	}

	function recalculate(containerId, settings) {
		settings.matchCount = 0;
		settings.matched = {};
		for(var key in settings.leftMap) {
			settings.leftMap[key].matchCount = 0;
		}
		for(var key in settings.rightMap) {
			settings.rightMap[key].matchCount = 0;
		}

		var divContainer = jQuery('#' + containerId);
		divContainer.find("input[type='hidden']").remove();
		jQuery("#" + containerId + " .o_match_dnd_target_drop_zone").each(function(index, dropBoxEl) {
			jQuery(dropBoxEl).find('.o_match_dnd_source').each(function(jndex, droppedEl) {
				var sourceId = jQuery(droppedEl).data('qti-id');
				var targetId = jQuery(dropBoxEl).data('qti-id');
				var inputElement = jQuery('<input type="hidden"/>')
						.attr('name', 'qtiworks_response_' + settings.responseIdentifier)
						.attr('value', sourceId + " " + targetId);
				divContainer.prepend(inputElement);
			});
		});
	}

	function checkMatch(settings, inputElement) {
		withCheckbox(settings, inputElement, function(inputElement, directedPair, left, right) {
			if (inputElement.checked) {
				var incremented = false;
				if (left.matchMax != 0 && left.matchMax <= left.matchCount) {
					inputElement.checked = false;
				} else {
					left.matchCount++;
					settings.matchCount++;
					incremented = true;
				}

				if (right.matchMax != 0 && right.matchMax <= right.matchCount) {
					inputElement.checked = false;
				} else {
					right.matchCount++;
					if (!incremented) {
						settings.matchCount++;
					}
				}
			} else {
				settings.matchCount--;
				left.matchCount--;
				right.matchCount--;
			}
			updateDisabledStates(settings);
		});
	}
}( jQuery ));