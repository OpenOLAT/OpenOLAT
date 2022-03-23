(function ($) {
    "use strict";
    
    $.fn.qtiCountWord = function(options) {
    	var settings = $.extend({
    		responseUniqueId: null,
    		labelSingular: 'xxx word',
    		labelPlural: 'xxx words'
        }, options );
        
        var count = function(text) {
        	var words = text.match(/[\w\u00C0-\u00ff]+/g);// use a similar regex as QtiWorks
        	var numOfWords = 0;
        	if(words != null) {
	        	for(var i=0; i<words.length; i++) {
	        		if(words[i].length > 1) {
	        			numOfWords++;
	        		}
	        	}
        	}
        	var containerEl = jQuery(textareaId).parent().get(0);
        	var plural = numOfWords > 1;
        	var label = plural ? settings.labelPlural : settings.labelSingular;
        	label = label.replace("xxx", "" + numOfWords);
        	jQuery('.o_qti_essay_num_of_words', containerEl).text(label);
        }
        this.count = count;
       
       	var textareaId = '#oo_' + settings.responseUniqueId;
        if(!jQuery(textareaId).hasClass('o_richtext_mce')) {
	        jQuery('#oo_' + settings.responseUniqueId).on('keyup', function(e) {
	        	var jTextarea = jQuery(textareaId);
	        	count(jTextarea.val());
	        });
        }
        return this;
    };
}( jQuery ));