(function ($) {
    "use strict";
    
    $.fn.qtiCopyPaste = function(options) {
    	var settings = $.extend({
    		//nothing
        }, options);
        
        function normalizeText(text) {
        	text = text.replace(/\t/g, "");
			text = text.replace(/(\r\n|\n|\r)/g, "");
			return text;
        }
        
        var getTextSelection = function() {
        	var text = "";
			var activeEl = document.activeElement;
			var activeElTagName = activeEl ? activeEl.tagName.toLowerCase() : null;
    		if (activeElTagName == "textarea" && (typeof activeEl.selectionStart == "number")) {
        		text = activeEl.value.slice(activeEl.selectionStart, activeEl.selectionEnd);
    		} else if (window.getSelection) {
        		text = document.getSelection().toString();
    		}
    		allowedText = text;
    		return true;
        }
    	
    	var allowedText = "";
    	var textarea = document.getElementById(this.attr('id'));
    	textarea.addEventListener('copy', getTextSelection, false);
    	textarea.addEventListener('cut', getTextSelection, false);
    	textarea.addEventListener('paste', function(e) {
	    	var text = e.clipboardData.getData('text');
	    	if(normalizeText(text) !== normalizeText(allowedText)) {
	    		e.preventDefault();
	    	}
    		return true;
    	});
        return this;
    };
}( jQuery ));