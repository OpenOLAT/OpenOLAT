(function ($) {
    $.fn.gapMatchInteraction = function(options) {
    	var settings = $.extend({
    		responseIdentifier: null,
    		formDispatchFieldId: null,
    		gapChoiceData: {},
    		gapData: {},
    		opened: false,
    		gapChoiceMap: {},
    		gapMap: {},
    		matched: {}
        }, options );

        for(var key in settings.gapChoiceData){
            var query = jQuery('#qtiworks_id_' + settings.responseIdentifier + '_' + key);
            settings.gapChoiceMap[key] = {
                matchMax: settings.gapChoiceData[key],
                matchCount: 0,
                query: query,
                text: query.text()
            };
        }
        for(var key in settings.gapData){
            var query = jQuery('#qtiworks_id_' + settings.responseIdentifier + '_' + key);
            settings.gapMap[key] = {
                required: settings.gapData[key], /* NB: This is not currently used in the JS */
                matched: false,
                matchedGapChoice: null,
                query: query,
                label: query.text()
            };
        }
    	
    	try {
    		gapMatch(this, settings);
    	} catch(e) {
    		if(window.console) console.log(e);
    	}
        return this;
    };

    function withCheckbox(settings, inputElement, callback) {
        var directedPair = inputElement.value;
        var splitPair = directedPair.split(" ");
        var gapChoice = settings.gapChoiceMap[splitPair[0]];
        var gap = settings.gapMap[splitPair[1]];
        callback(settings, inputElement, directedPair, gapChoice, gap);
    };

    function gapMatch($obj, settings) {
        if(settings.opened) {
        	queryInputElements(settings.responseIdentifier).on('click', function() {
	            checkMatch(settings, this);
	            setFlexiFormDirty(settings.formDispatchFieldId);
	        });
        }
        recalculate(settings);
        updateDisabledStates(settings);
    };
    
    function queryInputElements(responseIdentifier) {
        return jQuery('input[name=qtiworks_response_' + responseIdentifier + ']');
    };

    function recalculate(settings) {
    	settings.matchCount = 0;
        for (var key in settings.gapChoiceMap) {
        	settings.gapChoiceMap[key].matchCount = 0;
        }
        for (var key in settings.gapMap) {
        	settings.gapMap[key].matched = false;
        	settings.gapMap[key].matchedGapChoice = null;
        }

        queryInputElements(settings.responseIdentifier).each(function(index, el) {
            withCheckbox(settings, this, function(settings, inputElement, directedPair, gapChoice, gap) {
                if (inputElement.checked) {
                    gapChoice.matchCount++;
                    gap.matched = true;
                    gap.matchedGapChoice = gapChoice;
                    settings.matched[directedPair] = true;
                }
            });
        });

        for (var key in settings.gapMap) {
            var gap = settings.gapMap[key];
            var gapText;
            if (gap.matched) {
                gapText = gap.matchedGapChoice.text;
            }
            else {
                gapText = gap.label;
            }
            gap.query.text(gapText);
        }
    };

    function updateDisabledStates(settings) {
        queryInputElements(settings.responseIdentifier).each(function() {
            withCheckbox(settings, this, function(settings, inputElement, directedPair, gapChoice, gap) {
            	if(!settings.opened) {
            		inputElement.disabled = true;
            	} else if (inputElement.checked) {
                    inputElement.disabled = false;
                } else if (gap.matched || (gapChoice.matchMax!=0 && gapChoice.matchCount >= gapChoice.matchMax)) {
                    inputElement.disabled = true;
                } else {
                    inputElement.disabled = false;
                }
            });
        });
    };

    function checkMatch(settings, inputElement) {
        withCheckbox(settings, inputElement, function(settings, inputElement, directedPair, gapChoice, gap) {
            if (inputElement.checked) {
                if (gap.matched || (gapChoice.matchMax != 0 && gapChoice.matchMax <= gapChoice.matchCount)) {
                    inputElement.checked = false;
                } else {
                    gapChoice.matchCount++;
                    gap.matched = true;
                    gap.matchedGapChoice = gapChoice;
                }
                gap.query.text(gapChoice.text);
            } else {
                gapChoice.matchCount--;
                gap.matched = false;
                gap.matchedGapChoice = null;
                gap.query.text(gap.label);
            }
            updateDisabledStates(settings);
        });
    }
}( jQuery ));