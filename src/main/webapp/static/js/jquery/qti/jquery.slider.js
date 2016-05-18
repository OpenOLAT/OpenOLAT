(function ($) {
    $.fn.sliderInteraction = function(options) {
    	var settings = $.extend({
    		responseIdentifier: null,
    		formDispatchFieldId: null,
    		responseValue: null,
    		min: 1,
    		max: 1,
    		step: 1,
    		orientation: null,
    		isReversed: false,
    		isDiscrete: false,
    		opened: false,
    		initialValue: null
        }, options);
    	
    	try {
    		slide(this, settings);
    	} catch(e) {
    		if(window.console) console.log(e);
    	}
        return this;
    };
    
    function slide($obj, settings) {
    	var sliderQuery = jQuery('#qtiworks_id_slider_' + settings.responseIdentifier);
    	var inputElementQuery = jQuery('input[name="qtiworks_response_' + settings.responseIdentifier + '"]');
    	var initialValue = inputElementQuery.get(0).value || settings.min;
    	
    	sliderQuery.slider({
            value: initialValue,
            step: settings.step,
            disabled: !settings.opened,
            orientation: settings.orientation,
            /* (To handle 'reverse', we simply negate and swap min/max when mapping to/from the slider itself) */
            min: settings.isReversed ? -settings.max : settings.min,
            max: settings.isReversed ? -settings.min : settings.max,
            slide: function(event, ui) {
                var value = settings.isReversed ? -ui.value : ui.value;
                var feedbackQuery = jQuery('#qtiworks_id_slidervalue_' + settings.responseIdentifier);
                var inputElementQuery = jQuery('input[name="qtiworks_response_' + settings.responseIdentifier + '"]');
                
                inputElementQuery.get(0).value = value;
                feedbackQuery.text(value);
                sliderQuery.slider('value', settings.isReversed ? -value : value);
                
                setFlexiFormDirty(settings.formDispatchFieldId);
            }
        });
    }
}( jQuery ));
