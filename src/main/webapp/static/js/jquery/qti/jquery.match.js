(function ($) {
	$.fn.matchInteraction = function(options) {
		"use strict";
		var settings = $.extend({
			responseIdentifier: null,
			formDispatchFieldId: null,
			maxAssociations: 1,
			unansweredColumn: null,
			leftData: {},
			rightData: {},
			leftMap: {},
			rightMap: {},
			matched: []
        }, options );

		for(var key in settings.leftData){
			settings.leftMap[key] = {
				matchMax: settings.leftData[key],
				matchCount: 0
			};
		}
		for(var key in settings.rightData){
			settings.rightMap[key] = {
				matchMax: settings.rightData[key],
				matchCount: 0
			};
		}

		try {
			match(this, settings);
		} catch(e) {
			if(window.console) console.log(e);
		}
		return this;
	};
    
    function match($obj, settings) {
        queryInputElements(settings.responseIdentifier).on('click', function() {
            checkMatch(settings, this);
        });
        recalculate(settings);
        updateDisabledStates(settings);
    }
    
    function queryInputElements(responseIdentifier) {
        return jQuery('input[name=qtiworks_response_' + responseIdentifier + ']');
    }

	function withCheckbox(settings, inputElement, callback) {
		var directedPair = inputElement.value;
		var splitPair = directedPair.split(" ");
		var left = settings.leftMap[splitPair[0]];
		var right = settings.rightMap[splitPair[1]];
        callback(inputElement, directedPair, left, right);
    }

    /**
     * Left -> source (set 0)
     * Right -> target (set 1)
     */
	function recalculate(settings) {
		settings.matchCount = 0;
		settings.matched = {};
		for(var key in settings.leftMap) {
			settings.leftMap[key].matchCount = 0;
		}
		for(var key in settings.rightMap) {
			settings.rightMap[key].matchCount = 0;
        }

        queryInputElements(settings.responseIdentifier).each(function() {
            withCheckbox(settings, this, function(inputElement, directedPair, left, right) {
                if (inputElement.checked) {
                    settings.matchCount++;
                    left.matchCount++;
                    right.matchCount++;
                    settings.matched[directedPair] = true;
                }
            });
        });
    }

    /**
     * Left -> source (set 0)
     * Right -> target (set 1)
     */
	function updateDisabledStates(settings) {
		queryInputElements(settings.responseIdentifier).each(function() {
			withCheckbox(settings, this, function(inputElement, directedPair, left, right) {
				if (inputElement.checked) {
					inputElement.disabled = false;
				} else if (settings.maxAssociations != 0 && settings.matchCount >= settings.maxAssociations) {
                    inputElement.disabled = true;
				} else if (left.matchMax != 0 && left.matchCount >= left.matchMax) {
                    if(left.matchMax != 1 && left.matchCount != 1) {
                    		inputElement.disabled = true;
                    } else {
                        inputElement.disabled = false;
                    }
				} else if (right.matchMax != 0 && right.matchCount >= right.matchMax) {
                    inputElement.disabled = true;
				} else {
                    inputElement.disabled = false;
                }
            });
        });
    }

	function checkMatch(settings, inputElement) {
		withCheckbox(settings, inputElement, function(inputElement, directedPair, left, right) {
			if(inputElement.checked){
				var incremented = false;
				// left -> target
				if(left.matchMax == 1) {
					// deselect the other check
					var jInputElement = jQuery(inputElement);
					var inputId = jInputElement.attr('id');
					var inputRightId = jInputElement.attr('value').split(" ")[0];
					queryInputElements(settings.responseIdentifier).each(function() {
						withCheckbox(settings, this, function(element, directedPair, left, right) {
							var jElement = jQuery(element);
							if(inputId !== jElement.attr('id') && element.checked) {
								var elementRightId = jElement.attr('value').split(" ")[0];
								if(inputRightId === elementRightId) {
									element.checked = false;
									left.matchCount--;
					                right.matchCount--;
									settings.matchCount--;
								}
							}
						});
					});
                	
					left.matchCount++;
					settings.matchCount++;
					incremented = true;
                } else if (left.matchMax != 0 && left.matchMax <= left.matchCount) {
                    inputElement.checked = false;
                } else {
                    left.matchCount++;
                    settings.matchCount++;
                    incremented = true;
                }
                // right -> source
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
            if(!inputElement.checked && settings.unansweredColumn != null) {
            	checkUnanswered(settings, inputElement);
            }
        });
    }
	
	function checkUnanswered(settings, inputElement) {
		var jInputElement = jQuery(inputElement);
		var inputRightId = jInputElement.attr('value').split(" ")[0];
		
		var numOfSelectedBox = 0;
		var unansweredCheckbox = null;
		queryInputElements(settings.responseIdentifier).each(function() {
			withCheckbox(settings, this, function(element, directedPair, left, right) {
				var jElement = jQuery(element);
				var elementRightId = jElement.attr('value').split(" ")[0];
				if(inputRightId === elementRightId) {
					if(element.checked) {
						numOfSelectedBox++;
					} else if(jElement.closest("." + settings.unansweredColumn).length == 1) {
						unansweredCheckbox = element;
					}
				} 
			});
		});
		
		if(numOfSelectedBox == 0 && unansweredCheckbox != null) {
			unansweredCheckbox.checked = true;
		}
	}
	
}( jQuery ));