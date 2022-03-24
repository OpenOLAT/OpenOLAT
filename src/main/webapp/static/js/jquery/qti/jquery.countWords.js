(function ($) {
    "use strict";
    
    $.fn.qtiCountWord = function(options) {
    	var settings = $.extend({
    		responseUniqueId: null,
    		labelSingular: 'xxx word',
    		labelPlural: 'xxx words'
        }, options );
        
        var countHtmlRec = function(element, collector) {
       		if(element.nodeType == 1) {
        		// ignore attribute
        		if((element.nodeName === "SPAN" || element.nodeName === "DIV") && element.classList.contains("math")) {
        			collector.formulas++;
        		} else if(element.hasChildNodes()) {
        			var children = element.childNodes;
        			for(var i=children.length; i-->0; ) {
        				countHtmlRec(children[i], collector);
        			}
        		}
        	} else if(element.nodeType == 3) {
        		var text = element.nodeValue.replace(/\u00a0/g, " ");
        		collector.text += " " + text;
        	}
        }
        
        this.countHtml = function(html) {
        	var root = document.createElement("div");
        	root.innerHTML = html;
        	var collector = {
        		text : '',
        		formulas: 0
        	}
        	countHtmlRec(root, collector);
        	var numOfWords = countWords(collector.text);
        	printCount(numOfWords + collector.formulas);
        }
        
        var countText = function(text) {
        	var numOfWords = countWords(text);
        	printCount(numOfWords);
        }
        this.countText = countText;
        
        var countWords = function(text) {
        	var words = text.match(/[\w\u00C0-\u00ff]+/g);// use a similar regex as QtiWorks
        	var numOfWords = 0;
        	if(words != null) {
	        	for(var i=words.length; i-->0; ) {
	        		if(words[i].length > 1) {
	        			numOfWords++;
	        		}
	        	}
        	}
        	return numOfWords;
        }
        var printCount = function(numOfWords) {
        	var containerEl = jQuery(textareaId).parent().get(0);
        	var plural = numOfWords > 1;
        	var label = plural ? settings.labelPlural : settings.labelSingular;
        	label = label.replace("xxx", "" + numOfWords);
        	jQuery('.o_qti_essay_num_of_words', containerEl).text(label);
        }
       
       	var textareaId = '#oo_' + settings.responseUniqueId;
        if(!jQuery(textareaId).hasClass('o_richtext_mce')) {
	        jQuery('#oo_' + settings.responseUniqueId).on('keyup', function(e) {
	        	var jTextarea = jQuery(textareaId);
	        	countText(jTextarea.val());
	        });
        }
        return this;
    };
}( jQuery ));