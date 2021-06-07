(function ($) {
    "use strict";
    
    $.fn.qtiCountWord = function(options) {
    	var settings = $.extend({
    		responseUniqueId: null,
    		labelSingular: 'xxx word',
    		labelPlural: 'xxx words'
        }, options );
        
        jQuery('#oo_' + settings.responseUniqueId).on('keyup', function(e) {
        	var textareaId = '#oo_' + settings.responseUniqueId;
        	var text = jQuery(textareaId).val();
        	var words = text.split(/\W+/);// use the same regex as QtiWorks
        	var numOfWords = 0;
        	for(var i=0; i<words.length; i++) {
        		if(words[i].length > 1) {
        			numOfWords++;
        		}
        	}
        	var containerEl = jQuery(textareaId).parent().get(0);
        	var plural = numOfWords > 1;
        	var label = plural ? settings.labelPlural : settings.labelSingular;
        	label = label.replace("xxx", "" + numOfWords);
        	jQuery('.o_qti_essay_num_of_words', containerEl).text(label);
        });
        return this;
    };
}( jQuery ));