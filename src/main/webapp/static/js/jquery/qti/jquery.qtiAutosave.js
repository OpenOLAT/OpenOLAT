(function ($) {
    $.fn.qtiAutosave = function(options) {
    	var settings = $.extend({
    		responseUniqueId: null,
    		formName: null,//form name
    		dispIdField: null,//form dispatch id
    		dispId: null,//item id
    		eventIdField: null // form eventFieldId
        }, options );
    	
    	var wrapperId = this.attr('id');
		var periodic = jQuery.periodic({period: 60000, decay:1.0, max_period: Number.MAX_VALUE }, function() {
			try {
				var currentMarker = jQuery('#' + wrapperId).data('auto-save-periodic');
	    		if(jQuery('#' + wrapperId).length > 0 &&
	    				(typeof periodic.dateNowMarker === "undefined" || currentMarker == periodic.dateNowMarker)) {
	    			o_ffXHRNFEvent(settings.formName, settings.dispIdField, settings.dispId, settings.eventIdField, '2',
	        			'cid', 'tmpResponse', 'tmpResponse', 'qtiworks_presented_' + settings.responseUniqueId, 'qtiworks_presented_' + settings.responseUniqueId, '1',
	        			'qtiworks_response_' + settings.responseUniqueId, jQuery('#oo_' + settings.responseUniqueId).val());
	    			
	    			var now = new Date();
	    			var lastSaved = now.getHours() + ":" + now.getMinutes();
	    			var containerEl = jQuery('#' + wrapperId).parent().get(0);
	    			jQuery('div.o_qti_essay_last_save', containerEl).css('display','block');
	    			jQuery('span.o_qti_essay_last_save-time', containerEl).html(lastSaved);
	    		} else {
	    			periodic.cancel();
	    		}
			} catch(e) {
				if(window.console) console.log(e);
			}
		});
		
		var dateNow = Date.now();
		periodic.dateNowMarker = dateNow;
		jQuery('#' + wrapperId).data('auto-save-periodic', dateNow);
        return this;
    };
}( jQuery ));