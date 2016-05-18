(function ($) {
    $.fn.orderInteraction = function(options) {
    	var settings = $.extend({
    		responseIdentifier: null,
    		formDispatchFieldId: null,
    		initialSourceOrder: null,
    		initialTargetOrder: null,
    		responseValue: null,
    		minChoices: null,
    		maxChoices: null
        }, options );
    	
    	try {
    		order(this, settings);
    	} catch(e) {
    		if(window.console) console.log(e);
    	}
        return this;
    };

    function order($obj, settings) {
    	var containerQuery = jQuery('#qtiworks_response_' + settings.responseIdentifier);
    	var targetBox = jQuery('#qtiworks_response_' + settings.responseIdentifier + ' div.target');
    	var sourceList = jQuery('#qtiworks_response_' + settings.responseIdentifier + ' div.source ul');
    	var targetList = jQuery('#qtiworks_response_' + settings.responseIdentifier + ' div.target ul');
    	var hiddenInputContainer = jQuery('#qtiworks_response_' + settings.responseIdentifier + ' div.hiddenInputContainer');

        /* Add jQuery UI Sortable effect to sourceList */
        var listSelector = '#qtiworks_response_' + settings.responseIdentifier + ' ul';
        sourceList.sortable({
            connectWith: listSelector,
            stop: function() {
            	_sorted(settings);
            	setFlexiFormDirty(settings.formDispatchFieldId);
            }
        });
        sourceList.disableSelection();
        targetList.sortable({
            connectWith: listSelector,
            stop: function() {
            	_sorted(settings);
            	setFlexiFormDirty(settings.formDispatchFieldId);
            }
        });
        targetList.disableSelection();
    }
    
    function _sorted(settings) {
    	var sourceList = jQuery('#qtiworks_response_' + settings.responseIdentifier + ' div.source ul');
    	var targetList = jQuery('#qtiworks_response_' + settings.responseIdentifier + ' div.target ul');

    	var selectedCount = targetList.children('li').size();
        if (settings.minChoices != null && settings.maxChoices != null) {
            if (selectedCount < settings.minChoices || selectedCount > settings.maxChoices) {
                if (settings.minChoices != settings.maxChoices) {
                    alert("You must select and order between " + settings.minChoices + " and " + settings.maxChoices + " items");
                } else {
                    alert("You must select and order exactly " + settings.minChoices + " item" + (minChoices>1 ? "s" : ""));
                }
                targetBox.toggleClass('highlight', true);
                return false;
            }
            else {
                targetBox.toggleClass('highlight', false);
            }
        }
        
        var hiddenInputContainer = jQuery('#qtiworks_response_' + settings.responseIdentifier + ' div.hiddenInputContainer');

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
