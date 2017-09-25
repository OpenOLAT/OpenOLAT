(function ($) {
	$.fn.choiceInteraction = function(options) {
		var settings = $.extend({
			responseIdentifier: null,
			formDispatchFieldId: null,
			maxChoices: 0,
		}, options );

		try {
			if(settings.maxChoices > 0) {
				choice(this, settings);
				updateDisabledStates(settings);
			}
		} catch(e) {
			if(window.console) console.log(e);
		}
        return this;
    };
    
    function choice($obj, settings) {
    		var choices = jQuery('#qti_container_' + settings.responseIdentifier + " input[type=checkbox]");
    		choices.on('click', function(index, el) {
    			updateDisabledStates(settings);
    		});
    };

    function updateDisabledStates(settings) {
		var choices = jQuery('#qti_container_' + settings.responseIdentifier + " input[type=checkbox]");
		var numChecked = 0;
		choices.each(function(index, inputElement) {
            if (inputElement.checked) {
                numChecked++;
            }
		});
		
		if(numChecked >= settings.maxChoices) {
			choices.each(function(index, inputElement) {
	            if (inputElement.checked) {
	                inputElement.disabled = false;
	            } else {
	                inputElement.disabled = true;
	            }
			});
		} else {
			choices.each(function(index, inputElement) {
				inputElement.disabled = false;
			});
		}
    }
}( jQuery ));